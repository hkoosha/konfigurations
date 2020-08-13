package io.koosha.konfiguration.lite.v8;

import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.LiteKonfiguration;
import io.koosha.konfiguration.LiteSource;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.NoSuchElementException;
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
@ThreadSafe
final class ExtYamlLiteSource extends LiteSource {

    private static final Pattern DOT = Pattern.compile(Pattern.quote("."));

    private static Class<?> upper(@NotNull final Class<?> klass) {
        if (klass == boolean.class)
            return Boolean.class;
        if (klass == char.class)
            return Character.class;
        if (klass == byte.class)
            return Byte.class;
        if (klass == short.class)
            return Short.class;
        if (klass == int.class)
            return Integer.class;
        if (klass == long.class)
            return Long.class;
        if (klass == float.class)
            return Float.class;
        if (klass == double.class)
            return Double.class;
        if (klass == void.class)
            return Void.class;
        return klass;
    }

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
                .filter(it -> asList(markerExtractor.apply(it.getAnnotation(marker))).containsAll(cArgNames))
                .filter(it -> cArgNames.containsAll(asList(markerExtractor.apply(it.getAnnotation(marker)))))
                .filter(it -> {
                    final Parameter[] ps = it.getParameters();
                    final String[] ns = markerExtractor.apply(it.getAnnotation(marker));
                    for (int i = 0; i < ns.length; i++)
                        if (!upper(ps[i].getType()).isAssignableFrom(upper(cArgsByName.get(ns[i]).type)))
                            return false;
                    return true;
                })
                .collect(toList());
            if (constructors.isEmpty())
                throw new KfgSourceException(null, "no constructor with ConstructorProperties is liable for:" + cArgsByName);
            if (constructors.size() > 1)
                throw new KfgSourceException(null, "multiple constructor with ConstructorProperties are liable for: " + cArgsByName);
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

        private static Class<?> lower(@Nullable final Class<?> c) {
            if (c == Character.class)
                return char.class;
            if (c == Boolean.class)
                return boolean.class;
            if (c == Short.class)
                return short.class;
            if (c == Integer.class)
                return int.class;
            if (c == Long.class)
                return long.class;
            if (c == Float.class)
                return float.class;
            if (c == Double.class)
                return double.class;
            if (c == Void.class)
                return void.class;
            return c;
        }

        private class KonstructMapping extends ConstructMapping {

            @Override
            public Object construct(final Node node) {
                if (Map.class.isAssignableFrom(node.getType()) ||
                    Collection.class.isAssignableFrom(node.getType()) ||
                    typeDefinitions.containsKey(node.getType()))
                    return super.construct(node);

                if (node.isTwoStepsConstruction())
                    throw new YAMLException("encountered two step node (node is recursive): " + node);

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

                final String[] annotatedNames =
                    c0.getAnnotation(ConstructorProperties.class).value();

                final Object[] values = new Object[annotatedNames.length];
                for (final ParamNode pm : consArgs)
                    values[indexOf(annotatedNames, pm.name)] = pm.value;
                //                consArgs
                //                        .stream()
                //                        .map(t -> t.value)
                //                        .toArray();

                try {
                    c0.setAccessible(true);
                    return c0.newInstance(values);
                }
                catch (Exception e) {
                    throw new YAMLException(e);
                }
            }

            private int indexOf(@NotNull final String[] annotatedNames,
                                @NotNull final String name) {
                for (int i = 0; i < annotatedNames.length; i++)
                    if (Objects.equals(annotatedNames[i], name))
                        return i;
                throw new NoSuchElementException();
            }

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
        ConstructorProperties.class,
        ConstructorProperties::value
    );

    private static final ThreadLocal<Yaml> defaultYamlSupplier =
        ThreadLocal.withInitial(() -> new Yaml(defaultBaseConstructor));

    private final Supplier<Yaml> mapper;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String yaml;
    private final Map<String, ?> root;

    @NotNull
    private final String name;


    public ExtYamlLiteSource(@NotNull final String name,
                             @NotNull final String yaml) {
        this(name, yaml, ExtYamlLiteSource.defaultYamlSupplier::get);
    }

    /**
     * Creates an instance with the given Yaml parser.
     *
     * @param name   Name of this source.
     * @param yaml   backing store provider. Must always return a non-null valid yaml
     *               string.
     * @param mapper {@link Yaml} provider. Must always return a valid non-null Yaml,
     *               and if required, it must be able to deserialize custom types, so
     *               that {@link #custom(String, Kind)} works as well.
     * @throws NullPointerException if any of its arguments are null.
     */
    public ExtYamlLiteSource(@NotNull final String name,
                             @NotNull final String yaml,
                             @NotNull final Supplier<Yaml> mapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(mapper, "mapper");

        this.name = name;
        this.yaml = yaml;
        this.mapper = mapper;

        // Check early, so we 're not fooled with a dummy object reader.
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
        }
        catch (final ClassNotFoundException e) {
            throw new KfgSourceException(this.name(),
                "org.yaml.snakeyaml library is required to be" +
                    " present in the class path, can not find the" +
                    "class: org.yaml.snakeyaml.Yaml", e);
        }

        final Yaml newMapper = mapper.get();
        requireNonNull(newMapper, "supplied mapper is null");
        this.root = Collections.unmodifiableMap(newMapper.load(yaml));

        throw new UnsupportedOperationException("not implemented yet");
    }


    @NotNull
    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public boolean isReadonly() {
        return false;
    }

    @Override
    public LiteKonfiguration toReadonly() {
        return null;
    }


    @Override
    @NotNull
    protected Object bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return get(key);
    }

    @Override
    @NotNull
    protected Object char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return get(key);
    }

    @Override
    @NotNull
    protected Object string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return get(key);
    }

    @Override
    @NotNull
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return (Number) get(key);
    }

    @Override
    @NotNull
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return (Number) get(key);
    }

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

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Boolean value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Byte value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Character value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Short value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Integer value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Long value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Float value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Double value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final String value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final List<?> value) {
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key, final Set<?> value) {
        return this;
    }

    @Override
    public LiteKonfiguration putCustom(@NotNull final String key, final Object value) {
        return this;
    }

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


    private static final class KfgSnakeYamlAssertionError extends KfgAssertionException {

        public KfgSnakeYamlAssertionError(@Nullable final String source,
                                          @Nullable final String message) {
            super(source, null, null, null, message);
        }

        public KfgSnakeYamlAssertionError(@Nullable final String source,
                                          @Nullable final String key,
                                          @Nullable final Kind<?> neededType,
                                          @Nullable final Object actualValue,
                                          @Nullable final String message) {
            super(source, key, neededType, actualValue, message);
        }

    }

}
