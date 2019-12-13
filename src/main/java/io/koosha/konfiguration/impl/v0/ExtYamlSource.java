package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Q;
import io.koosha.konfiguration.ext.KfgSnakeYamlAssertionError;
import io.koosha.konfiguration.ext.KfgSnakeYamlError;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads konfig from a yaml source (supplied as string).
 *
 * <p>for {@link #custom(String, Q)} to work, the supplied yaml reader must be
 * configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe and immutable.
 */
@ApiStatus.Internal
@Immutable
@ThreadSafe
final class ExtYamlSource extends Source {

    private static final Pattern DOT = Pattern.compile(Pattern.quote("."));
    private final boolean unsafe;

    static final class ByConstructorConstructor<A extends Annotation> extends Constructor {

        private final Class<? extends A> marker;
        private final Function<? super A, String[]> markerExtractor;

        @SuppressWarnings("SameParameterValue")
        private static <A extends Annotation> java.lang.reflect.Constructor<?> find(
                final Class<? extends A> marker,
                final Function<? super A, String[]> markerExtractor,
                final Class<?> origin,
                final Map<String, ? extends Param> cArgsByName,
                final List<String> cArgNames) {
            final List<java.lang.reflect.Constructor<?>> constructors = Arrays
                    .stream(origin.getDeclaredConstructors())
                    .filter(it -> it.getAnnotation(marker) != null)
                    .filter(it -> asList(markerExtractor.apply(it.getAnnotation(marker))).containsAll(cArgNames)
                            && cArgNames.containsAll(asList(markerExtractor.apply(it.getAnnotation(marker)))))
                    .filter(it -> {
                        final Parameter[] ps = it.getParameters();
                        final String[] ns = markerExtractor.apply(it.getAnnotation(marker));
                        for (int i = 0; i < ns.length; i++)
                            if (!ps[i].getType().isAssignableFrom(cArgsByName.get(ns[i]).type))
                                return false;
                        return true;
                    })
                    .collect(toList());
            if (constructors.isEmpty())
                throw new KfgSnakeYamlError(null, "no constructor with ConstructorProperties is liable");
            if (constructors.size() > 1)
                throw new KfgSnakeYamlError(null, "multiple constructor with ConstructorProperties are liable");
            return constructors.get(0);
        }

        ByConstructorConstructor(@NonNull @NotNull final Class<? extends A> marker,
                                 @NotNull @NonNull final Function<? super A, String[]> markerExtractor) {
            this.marker = marker;
            this.markerExtractor = markerExtractor;
            this.yamlClassConstructors.put(NodeId.mapping, new KonstructMapping());
        }

        private class KonstructMapping extends ConstructMapping {

            @Override
            public Object construct(final Node node) {
                if (Map.class.isAssignableFrom(node.getType()) ||
                        Collection.class.isAssignableFrom(node.getType()) ||
                        typeDefinitions.containsKey(node.getType()))
                    return super.construct(node);

                if (node.isTwoStepsConstruction())
                    throw new YAMLException("encountered two step node: " + node);

                final MappingNode mnode = (MappingNode) node;
                flattenMapping(mnode);

                final List<ParamNode> consArgs = mnode
                        .getValue()
                        .stream()
                        .map(tuple -> {
                            if (!(tuple.getKeyNode() instanceof ScalarNode))
                                throw new YAMLException(
                                        "Keys must be scalars but found: " + tuple.getKeyNode());
                            final ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                            keyNode.setType(String.class);
                            return new ParamNode((String) constructObject(keyNode), tuple.getValueNode());
                        })
                        .peek(t -> {
                            final Tag tag = t.node.getTag();
                            Class<?> tp = null;
                            if (tag == Tag.INT)
                                tp = Integer.class;
                            else if (tag == Tag.FLOAT)
                                tp = Float.class;
                            else if (tag == Tag.STR)
                                tp = String.class;
                            else if (tag == Tag.MAP)
                                tp = Map.class;
                            else if (tag == Tag.SEQ)
                                tp = List.class;
                            else if (tag == Tag.SET)
                                tp = Set.class;
                            else if (tag == Tag.BOOL)
                                tp = Boolean.class;
                            else if (tag == Tag.NULL)
                                tp = Object.class;
                            t.type = tp;
                            if (tp != null)
                                t.node.setType(tp);
                        })
                        .peek(t -> {
                            if (t.node.getNodeId() != NodeId.scalar) {
                                // only if there is no explicit TypeDescription
                                final Class<?>[] args = t.getActualTypeArguments();
                                if (args != null && args.length > 0) {
                                    // type safe (generic) collection may contain the proper class
                                    if (t.node.getNodeId() == NodeId.sequence) {
                                        ((SequenceNode) t.node).setListType(args[0]);
                                    }
                                    else if (Set.class.isAssignableFrom(t.node.getType())) {
                                        ((MappingNode) t.node).setOnlyKeyType(args[0]);
                                        t.node.setUseClassConstructor(true);
                                    }
                                    else if (Map.class.isAssignableFrom(t.node.getType())) {
                                        ((MappingNode) t.node).setTypes(args[0], args[1]);
                                        t.node.setUseClassConstructor(true);
                                    }
                                }
                            }
                        })
                        .peek(t -> t.value = constructObject(t.node))
                        .peek(t -> {
                            if (t.value instanceof Double && t.typeIs(Float.TYPE, Float.class))
                                t.value = t.double_().floatValue();

                            else if (t.value instanceof byte[] &&
                                    Objects.equals(t.node.getTag(), Tag.BINARY) &&
                                    t.typeIs(String.class))
                                t.value = new String(t.byteArray());
                        })
                        .collect(toList());

                final Map<String, ParamNode> byName = consArgs
                        .stream()
                        .collect(Collectors.toMap(ca -> ca.name, Function.identity()));

                final List<String> names = consArgs
                        .stream()
                        .map(t -> t.name)
                        .collect(toList());

                final Class<?>[] types = consArgs
                        .stream()
                        .map(t -> t.type)
                        .toArray(Class<?>[]::new);

                final Object[] values = consArgs
                        .stream()
                        .map(t -> t.value)
                        .toArray();

                java.lang.reflect.Constructor<?> c0;
                try {
                    c0 = find(marker,
                            markerExtractor,
                            node.getType(),
                            byName, names);
                }
                catch (YAMLException y) {
                    c0 = null;
                }

                if (c0 == null)
                    try {
                        c0 = node.getType().getDeclaredConstructor(types);
                    }
                    catch (NoSuchMethodException e) {
                        // ignore
                    }


                if (c0 == null)
                    try {
                        final Class<?>[] types2 = consArgs
                                .stream()
                                .map(t -> t.type)
                                .map(ByConstructorConstructor::lower)
                                .toArray(Class<?>[]::new);
                        c0 = node.getType().getDeclaredConstructor(types2);
                    }
                    catch (NoSuchMethodException ex) {
                        c0 = null;
                    }

                requireNonNull(c0, "no constructor found for: " + node);

                try {
                    c0.setAccessible(true);
                    return c0.newInstance(values);
                }
                catch (Exception e) {
                    throw new YAMLException(e);
                }
            }

        }

        private static Class<?> lower(Class<?> c) {
            if (c == Boolean.class)
                return boolean.class;
            if (c == Integer.class)
                return int.class;
            if (c == Long.class)
                return long.class;
            if (c == Float.class)
                return float.class;
            if (c == Double.class)
                return double.class;
            return c;
        }


        private static class Param {
            String name;
            Class<?> type;
            Object value;

            Param(final String name) {
                this.name = name;
            }


            final Double double_() {
                return (Double) this.value;
            }

            final byte[] byteArray() {
                return (byte[]) this.value;
            }

            final boolean typeIs(Object... other) {
                for (final Object o : other)
                    if (o == this.type)
                        return true;
                return false;
            }

        }

        private static final class ParamNode extends Param {
            Node node;

            public ParamNode(String name, Node node) {
                super(name);
                this.node = node;
            }

            Class<?>[] getActualTypeArguments() {
                return null;
            }
        }

    }

    static final BaseConstructor defaultBaseConstructor = new ByConstructorConstructor<>(
            (Class<? extends ConstructorProperties>) ConstructorProperties.class,
            (Function<? super ConstructorProperties, String[]>) ConstructorProperties::value
    );

    static final ThreadLocal<Yaml> defaultYamlSupplier = ThreadLocal.withInitial(() -> new Yaml(defaultBaseConstructor));

    private final Supplier<Yaml> mapper;
    private final Supplier<String> yaml;

    private int lastHash;
    private final Map<String, ?> root;

    @Getter
    @Accessors(fluent = true)
    private final KonfigurationManager0 manager = new KonfigurationManager0() {

        /**
         * {@inheritDoc}
         */
        @Override
        @Contract(pure = true)
        public boolean hasUpdate() {
            final String newYaml = yaml.get();
            return newYaml != null && newYaml.hashCode() != lastHash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Contract(pure = true,
                value = "-> new")
        public @NotNull Konfiguration0 _update() {
            return this.hasUpdate()
                   ? new ExtYamlSource(name(), yaml, mapper, unsafe)
                   : ExtYamlSource.this;
        }
    };

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    /**
     * Creates an instance with the given Yaml parser.
     *
     * @param yaml   backing store provider. Must always return a non-null valid yaml
     *               string.
     * @param mapper {@link Yaml} provider. Must always return a valid non-null Yaml,
     *               and if required, it must be able to deserialize custom types, so
     *               that {@link #custom(String, Q)} works as well.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSnakeYamlError    if org.yaml.snakeyaml library is not in the classpath. it
     *                              specifically looks for the class: "org.yaml.snakeyaml"
     * @throws KfgSnakeYamlError    if the storage (yaml string) returned by yaml string is null.
     */
    ExtYamlSource(@NotNull @NonNull final String name,
                  @NotNull @NonNull final Supplier<String> yaml,
                  @NotNull @NonNull final Supplier<Yaml> mapper,
                  final boolean unsafe) {
        this.name = name;
        this.yaml = yaml;
        this.mapper = mapper;
        this.unsafe = unsafe;

        // Check early, so we 're not fooled with a dummy object reader.
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
        }
        catch (final ClassNotFoundException e) {
            throw new KfgSnakeYamlError(this.name(),
                    "org.yaml.snakeyaml library is required to be" +
                            " present in the class path, can not find the" +
                            "class: org.yaml.snakeyaml.Yaml", e);
        }

        final String newYaml = this.yaml.get();
        requireNonNull(newYaml, "supplied storage is null");
        this.lastHash = newYaml.hashCode();

        final Yaml newMapper = mapper.get();
        requireNonNull(newMapper, "supplied mapper is null");
        this.root = Collections.unmodifiableMap(newMapper.load(newYaml));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Object bool0(@NotNull @NonNull final String key) {
        return get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Object char0(@NotNull @NonNull final String key) {
        return get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Object string0(@NotNull @NonNull final String key) {
        return get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Number number0(@NotNull @NonNull final String key) {
        return (Number) get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Number numberDouble0(@NotNull @NonNull final String key) {
        return (Number) get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    List<?> list0(@NotNull @NonNull final String key,
                  @NotNull @NonNull final Q<? extends List<?>> type) {
        this.ensureSafe(type);

        final Object g = this.get(key);
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, type.klass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Set<?> set0(@NotNull @NonNull final String key,
                @NotNull @NonNull final Q<? extends Set<?>> type) {
        this.ensureSafe(type);

        final Object g = this.get(key);
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, type.klass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Map<?, ?> map0(@NotNull @NonNull final String key,
                   @NotNull @NonNull final Q<? extends Map<?, ?>> type) {
        this.ensureSafe(type);

        final Object g = this.get(key);
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, type.klass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Object custom0(@NotNull @NonNull final String key,
                   @NotNull @NonNull final Q<?> type) {
        this.ensureSafe(type);

        final Object g = this.get(key);
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, type.klass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isNull(@NonNull @NotNull String key) {
        try {
            return get(key) == null;
        }
        catch (final KfgSnakeYamlAssertionError e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final String key,
                       @Nullable final Q<?> type) {
        try {
            return Q.matchesValue(type, get(key));
        }
        catch (final KfgSnakeYamlAssertionError e) {
            return false;
        }
    }

    private Object get(@NotNull @NonNull final String key) {
        Map<?, ?> node = root;
        final String[] split = DOT.split(key);
        for (int i = 0; i < split.length; i++) {
            final String k = split[i];
            final Object n = node.get(k);
            final boolean isLast = i == split.length - 1;

            if (isLast)
                return n;
            if (!(n instanceof Map))
                throw new KfgSnakeYamlAssertionError(this.name(), "assertion error");
            node = (Map<?, ?>) n;
        }
        throw new KfgSnakeYamlAssertionError(this.name(), "assertion error");
    }

    private void ensureSafe(@Nullable final Q<?> type) {
        //        Constructor constructor = new Constructor(Customer.class);
        //        TypeDescription customTypeDescription = new TypeDescription(Customer.class);
        //        customTypeDescription.addPropertyParameters("contactDetails", Contact.class);
        //        constructor.addTypeDescription(customTypeDescription);
        //        Yaml yaml = new Yaml(constructor);
        if (this.unsafe)
            return;
        if (type == null || type.isParametrized())
            throw new UnsupportedOperationException("yaml does not support parameterized yet.");
    }

}
