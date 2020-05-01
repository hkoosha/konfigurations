package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.ext.KfgSnakeYamlAssertionError;
import io.koosha.konfiguration.ext.KfgSnakeYamlError;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
 * <p>for {@link #custom(String, Kind)} to work, the supplied yaml reader must be
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

        ByConstructorConstructor(@NotNull final Class<? extends A> marker,
                                 @NotNull final Function<? super A, String[]> markerExtractor) {
            Objects.requireNonNull(marker, "marker");
            Objects.requireNonNull(markerExtractor, "markerExtractor");
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

                final MappingNode mNode = (MappingNode) node;
                flattenMapping(mNode);

                final List<ParamNode> consArgs = mNode
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

    static final ThreadLocal<Yaml> defaultYamlSupplier =
            ThreadLocal.withInitial(() -> new Yaml(defaultBaseConstructor));

    private final Supplier<Yaml> mapper;
    private final Supplier<String> yaml;

    private final String lastYaml;
    private final Map<String, ?> root;

    @NotNull
    private final String name;

    /**
     * Creates an instance with the given Yaml parser.
     *
     * @param yamlSupplier backing store provider. Must always return a non-null valid yaml
     *                     string.
     * @param mapper       {@link Yaml} provider. Must always return a valid non-null Yaml,
     *                     and if required, it must be able to deserialize custom types, so
     *                     that {@link #custom(String, Kind)} works as well.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSnakeYamlError    if org.yaml.snakeyaml library is not in the classpath. it
     *                              specifically looks for the class: "org.yaml.snakeyaml"
     * @throws KfgSnakeYamlError    if the storage (yaml string) returned by yaml string is null.
     */
    ExtYamlSource(@NotNull final String name,
                  @NotNull final Supplier<String> yamlSupplier,
                  @NotNull final Supplier<Yaml> mapper,
                  final boolean unsafe) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yamlSupplier, "yamlSupplier");
        Objects.requireNonNull(mapper, "mapper");

        this.name = name;
        this.yaml = yamlSupplier;
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
        this.lastYaml = newYaml;

        final Yaml newMapper = mapper.get();
        requireNonNull(newMapper, "supplied mapper is null");
        this.root = Collections.unmodifiableMap(newMapper.load(newYaml));
    }


    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String name() {
        return this.name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return (Number) get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return (Number) get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected List<?> list0(@NotNull final String key,
                            @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final Object g = this.get(key);
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        final List<?> asList = (List<?>) mapper.loadAs(yamlAgain, type.klass());
        return Collections.unmodifiableList(asList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final List<?> asList = this.list0(key, type);
        final HashSet<?> asSet = new HashSet<>(asList);
        if (asSet.size() != asList.size())
            throw new KfgTypeException(this.name, key, type.asSet(), asList, "is a list, not a set");
        return Collections.unmodifiableSet(asSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (type.isParametrized())
            throw new KfgSnakeYamlAssertionError(
                    this.name, key, type, null,
                    "parametrized type are not supported by yaml source");

        final Object g = this.get(key);
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, type.klass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
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
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        if (type.isParametrized())
            return false;
        try {
            this.custom0(key, type);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean hasUpdate() {
        final String newYaml = yaml.get();
        return newYaml != null && !Objects.equals(newYaml, lastYaml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true, value = "-> new")
    @NotNull
    public Source updatedCopy() {
        return this.hasUpdate()
                ? new ExtYamlSource(name(), yaml, mapper, unsafe)
                : ExtYamlSource.this;
    }


    private Object get(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
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

}
