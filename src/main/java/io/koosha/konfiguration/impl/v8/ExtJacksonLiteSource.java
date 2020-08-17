package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.KfgException;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.LiteKonfiguration;
import io.koosha.konfiguration.LiteSource;
import io.koosha.konfiguration.LiteSubsetView;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Reads konfig from a json/yaml source (supplied as string).
 *
 * <p>for {@link #custom(String, Kind)} to work, the supplied mapper must
 * be configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe.
 */
@ThreadSafe
final class ExtJacksonLiteSource extends LiteSource {

    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final String DOT_PATTERN_QUOTED = Pattern.quote(".");

    private final Supplier<ObjectMapper> mapperSupplier;
    private final ObjectNode root;
    private final Object LOCK = new Object();

    @NotNull
    private final String name;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @NotNull
    private final String json;

    private JsonNode node_(@NotNull final String key) {
        requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        synchronized (LOCK) {
            JsonNode node = this.root;
            for (final String sub : key.split(DOT_PATTERN_QUOTED)) {
                if (node.isMissingNode())
                    return node;
                node = root.findPath(sub);
            }
            return node;
        }
    }

    @NotNull
    private JsonNode node(@NotNull final String key) {
        requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        synchronized (LOCK) {
            final JsonNode node = node_(key);
            if (node.isMissingNode())
                throw new KfgMissingKeyException(this.name(), key);
            return node;
        }
    }

    @NotNull
    private ObjectNode ensureIntermediateNodes(@NotNull final String key) {
        requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            ObjectNode from = this.root;
            for (int i = 0; i < split.length - 1; i++) {
                JsonNode next = from.findPath(split[i]);
                if (next.isMissingNode()) {
                    from.set(split[i], from.objectNode());
                    next = from.findPath(split[i]);
                }
                else if (!next.isObject())
                    throw new KfgTypeException(this.name, key, null, null, "expected all objects in path, found=" + next + " at=" + split[i]);
                from = (ObjectNode) next;
            }

            return from;
        }
    }

    @NotNull
    private JsonNode checkJsonType(final boolean condition,
                                   @NotNull final Kind<?> required,
                                   @NotNull final JsonNode node,
                                   @NotNull final String key) {
        if (!condition)
            throw new KfgTypeException(this.name(), key, required, node);
        if (node.isNull())
            throw new KfgAssertionException(this.name(), key, required, null, null);
        return node;
    }

    private boolean typeMatches(@NotNull final Kind<?> type,
                                @NotNull final JsonNode node) {

        return type.isNull() && node.isNull()
            || type.isBool() && node.isBoolean()
            || type.isChar() && node.isTextual() && node.asText().length() == 1
            || type.isString() && node.isTextual()
            || type.isByte() && node.isShort() && node.asInt() <= Byte.MAX_VALUE && Byte.MIN_VALUE <= node.asInt()
            || type.isShort() && node.isShort()
            || type.isInt() && node.isInt()
            || type.isLong() && node.isLong()
            || type.isFloat() && node.isFloat()
            || type.isDouble() && node.isDouble()
            || type.isList() && node.isArray()
            || type.isSet() && node.isArray();
    }


    /**
     * Creates an instance with a with the given json
     * provider and object mapper provider.
     *
     * @param name         name of this source
     * @param json         backing store provider. Must always return a non-null valid json
     *                     string.
     * @param objectMapper {@link ObjectMapper} provider. Must always return a valid
     *                     non-null ObjectMapper, and if required, it must be able to
     *                     deserialize custom types, so that {@link #custom(String, Kind)}
     *                     works as well.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public ExtJacksonLiteSource(@NotNull final String name,
                                @NotNull final String json,
                                @NotNull final Supplier<ObjectMapper> objectMapper) {
        requireNonNull(name, "name");
        requireNonNull(json, "json");
        requireNonNull(objectMapper, "objectMapper");
        requireNonNull(objectMapper.get(), "supplied mapper is null");

        this.name = name;
        this.json = json;
        this.mapperSupplier = objectMapper;

        final JsonNode update;
        try {
            update = this.mapperSupplier.get().readTree(json);
        }
        catch (final IOException e) {
            throw new KfgSourceException(this.name(), "error parsing json string", e);
        }
        requireNonNull(update, "root element is null");

        if (!(update instanceof ObjectNode))
            throw new KfgSourceException(this.name(), "root node is not object");

        this.root = (ObjectNode) update;
    }


    @NotNull
    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String serialize() {
        final StringWriter sw = new StringWriter();
        final ObjectMapper mapper = this.mapperSupplier.get();
        try {
            synchronized (LOCK) {
                mapper.writeValue(sw, this.root);
            }
        }
        catch (final IOException e) {
            throw new KfgException(this.name(), e);
        }
        return sw.toString();
    }

    @Override
    public boolean isReadonly() {
        return false;
    }

    @Override
    public LiteKonfiguration toReadonly() {
        return new LiteSubsetView(this.name(), this, "", true);
    }

    @Override
    public LiteKonfiguration toWritableCopy() {
        return new ExtJacksonLiteSource(this.name, this.serialize(), this.mapperSupplier);
    }

    @Override
    public boolean has(@NotNull final String key,
                       @Nullable final Kind<?> type) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            if (this.node_(key).isMissingNode())
                return false;
            if (type == null)
                return true;

            final JsonNode node = this.node(key);

            if (this.typeMatches(type, node))
                return true;

            try {
                this.custom0(key, type);
                return true;
            }
            catch (Throwable t) {
                return false;
            }
        }
    }


    @Override
    @NotNull
    protected Boolean bool0(@NotNull final String key) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            final JsonNode at = node(key);
            return checkJsonType(at.isBoolean(), Kind.BOOL, at, key).asBoolean();
        }
    }

    @Override
    @NotNull
    protected Character char0(@NotNull final String key) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            final JsonNode at = node(key);
            return checkJsonType(at.isTextual() && at.textValue().length() == 1, Kind.STRING, at, key)
                .textValue()
                .charAt(0);
        }
    }

    @Override
    @NotNull
    protected String string0(@NotNull final String key) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            final JsonNode at = node(key);
            return checkJsonType(at.isTextual(), Kind.STRING, at, key).asText();
        }
    }

    @NotNull
    @Override
    protected Number number0(@NotNull final String key) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            final JsonNode at = node(key);
            return checkJsonType(
                at.isShort() || at.isInt() || at.isLong(),
                Kind.LONG, at, key).longValue();
        }
    }

    @NotNull
    @Override
    protected Number numberDouble0(@NotNull final String key) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            final JsonNode at = node(key);
            return checkJsonType(
                at.isFloat()
                    || at.isDouble()
                    || at.isShort()
                    || at.isInt()
                    || at.isLong(),
                Kind.DOUBLE, at, key).doubleValue();
        }
    }

    @NotNull
    @Override
    protected List<?> list0(@NotNull final String key,
                            @NotNull final Kind<?> type) {
        requireNonNull(key, "key");
        requireNonNull(type, "type");

        final ObjectMapper reader = this.mapperSupplier.get();
        final TypeFactory tf = reader.getTypeFactory();
        final JavaType ct = tf.constructSimpleType(type.klass(), new JavaType[0]);
        final CollectionType javaType = tf.constructCollectionType(List.class, ct);

        final List<?> asList;
        synchronized (LOCK) {
            final JsonNode at = node(key);
            checkJsonType(at.isArray(), type, at, key);
            try {
                asList = reader.readValue(at.traverse(), javaType);
            }
            catch (final IOException e) {
                throw new KfgTypeException(this.name(), key, type, at, "type mismatch", e);
            }
        }
        return Collections.unmodifiableList(asList);
    }

    @NotNull
    @Override
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<?> type) {
        requireNonNull(key, "key");
        requireNonNull(type, "type");

        final List<?> asList = this.list0(key, type);
        final Set<?> asSet = new HashSet<>(asList);
        if (asSet.size() != asList.size())
            throw new KfgTypeException(this.name, key, type.asSet(), asList, "is a list, not a set");
        return Collections.unmodifiableSet(asSet);
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        requireNonNull(key, "key");
        requireNonNull(type, "type");

        synchronized (LOCK) {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonNode node = this.node(key);
            final JsonParser traverse = node.traverse();

            Object ret;
            try {
                ret = reader.readValue(traverse, new TypeReference<Object>() {
                    @Override
                    public Type getType() {
                        return type.type();
                    }
                });
            }
            catch (final IOException e) {
                throw new KfgTypeException(this.name(), key, type, null, "jackson error", e);
            }

            if (ret instanceof List)
                return Collections.unmodifiableList(((List<?>) ret));
            else if (ret instanceof Set)
                return Collections.unmodifiableSet(((Set<?>) ret));
            else
                return ret;
        }
    }

    @Override
    protected boolean isNull(@NotNull final String key) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            return node(key).isNull();
        }
    }


    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Boolean value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key).put(split[split.length - 1], value);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Byte value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key).put(split[split.length - 1],
                value == null ? null : value.shortValue());
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Short value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .put(split[split.length - 1], value);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Integer value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .put(split[split.length - 1], value);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Long value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .put(split[split.length - 1], value);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Float value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .put(split[split.length - 1], value);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Double value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .put(split[split.length - 1], value);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final String value) {
        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .put(split[split.length - 1], value);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final List<?> value) {
        final String[] split = DOT_PATTERN.split(key);
        final ObjectMapper objectMapper = this.mapperSupplier.get();
        final JsonNode jsonNode = objectMapper.valueToTree(value);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .set(split[split.length - 1], jsonNode);
        }
        return this;
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Set<?> value) {
        final String[] split = DOT_PATTERN.split(key);
        final ObjectMapper objectMapper = this.mapperSupplier.get();
        final JsonNode jsonNode = objectMapper.valueToTree(value);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .set(split[split.length - 1], jsonNode);
        }
        return this;
    }

    @Override
    public LiteKonfiguration putCustom(@NotNull final String key,
                                       final Object value) {
        final String[] split = DOT_PATTERN.split(key);
        final ObjectMapper objectMapper = this.mapperSupplier.get();
        final JsonNode jsonNode = objectMapper.valueToTree(value);
        synchronized (LOCK) {
            this.ensureIntermediateNodes(key)
                .set(split[split.length - 1], jsonNode);
        }
        return this;
    }

}
