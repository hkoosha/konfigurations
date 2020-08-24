package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.koosha.konfiguration.KfgException;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.LiteKonfiguration;
import io.koosha.konfiguration.LiteSource;
import io.koosha.konfiguration.LiteSubsetView;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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

    @NotNull
    @Contract(pure = true)
    private JsonNode node_(@NotNull final String key) {
        requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String[] split = key.split(DOT_PATTERN_QUOTED);

        synchronized (LOCK) {
            JsonNode node = this.root;
            for (final String sub : split) {
                if (node.isMissingNode())
                    return node;
                node = root.findPath(sub);
            }
            return node;
        }
    }

    @NotNull
    @Contract(pure = true)
    private JsonNode node(@NotNull final String key) {
        requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final JsonNode node = node_(key);
        if (node.isMissingNode())
            throw new KfgMissingKeyException(this.name(), key);
        return node;
    }

    @NotNull
    @Contract(mutates = "this")
    private ObjectNode ensureIntermediateNodes(@NotNull final String[] key) {
        ObjectNode from = this.root;

        synchronized (LOCK) {
            for (int i = 0; i < key.length - 1; i++) {
                JsonNode next = from.findPath(key[i]);
                if (next.isMissingNode()) {
                    from.set(key[i], from.objectNode());
                    next = from.findPath(key[i]);
                }
                else if (!next.isObject()) {
                    throw new KfgTypeException(
                        this.name, Arrays.toString(key), null, null,
                        "expected all objects in path, found=" + next + " at=" + key[i]);
                }
                from = (ObjectNode) next;
            }
        }

        return from;
    }

    private Optional<ObjectNode> ensureIntermediateNodesOrNull(@NotNull final String key) {
        requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String[] split = DOT_PATTERN.split(key);
        synchronized (LOCK) {
            ObjectNode from = this.root;
            for (int i = 0; i < split.length - 1; i++) {
                JsonNode next = from.findPath(split[i]);
                if (next.isMissingNode())
                    return Optional.empty();
                else if (!next.isObject())
                    throw new KfgTypeException(this.name, key, null, null, "expected all objects in path, found=" + next + " at=" + split[i]);
                from = (ObjectNode) next;
            }

            return Optional.of(from);
        }
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
    ExtJacksonLiteSource(@NotNull final String name,
                         @NotNull final String json,
                         @NotNull final Supplier<ObjectMapper> objectMapper) {
        requireNonNull(name, "name");
        requireNonNull(json, "json");
        requireNonNull(objectMapper, "objectMapper");
        requireNonNull(objectMapper.get(), "supplied mapper is null");

        this.name = name;
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
    @NotNull
    @Contract(pure = true)
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
    @NotNull
    public LiteKonfiguration toReadonly() {
        return new LiteSubsetView(this.name(), this, "", true);
    }

    @Override
    @NotNull
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

            if (ExtJacksonSourceJsonHelper.typeMatches(type, node))
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

        final JsonNode at;
        synchronized (LOCK) {
            at = node(key);
        }

        return ExtJacksonSourceJsonHelper.checkJsonType(at.isBoolean(), Kind.BOOL, at, key, this.name())
                                         .asBoolean();
    }

    @Override
    @NotNull
    protected Character char0(@NotNull final String key) {
        requireNonNull(key, "key");

        final JsonNode at;
        synchronized (LOCK) {
            at = node(key);
        }

        return ExtJacksonSourceJsonHelper.checkJsonType(
            at.isTextual() && at.textValue().length() == 1, Kind.STRING, at, key, this.name())
                                         .textValue()
                                         .charAt(0);
    }

    @Override
    @NotNull
    protected String string0(@NotNull final String key) {
        requireNonNull(key, "key");

        final JsonNode at;
        synchronized (LOCK) {
            at = node(key);
        }

        return ExtJacksonSourceJsonHelper.checkJsonType(at.isTextual(), Kind.STRING, at, key, this.name())
                                         .asText();
    }

    @NotNull
    @Override
    protected Number number0(@NotNull final String key) {
        requireNonNull(key, "key");

        final JsonNode at;
        synchronized (LOCK) {
            at = node(key);
        }

        return ExtJacksonSourceJsonHelper.checkJsonType(
            at.isShort() || at.isInt() || at.isLong(),
            Kind.LONG, at, key, this.name())
                                         .longValue();
    }

    @NotNull
    @Override
    protected Number numberDouble0(@NotNull final String key) {
        requireNonNull(key, "key");

        final JsonNode at;
        synchronized (LOCK) {
            at = node(key);
        }

        return ExtJacksonSourceJsonHelper.checkJsonType(
            at.isFloat()
                || at.isDouble()
                || at.isShort()
                || at.isInt()
                || at.isLong(),
            Kind.DOUBLE, at, key, this.name())
                                         .doubleValue();
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
            final JsonNode at = this.node(key);
            ExtJacksonSourceJsonHelper.checkJsonType(at.isArray(), type, at, key, this.name());
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
        final Set<?> asSet = new LinkedHashSet<>(asList);
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

        final ObjectMapper reader = this.mapperSupplier.get();
        Object ret;

        try {
            synchronized (LOCK) {
                final JsonNode node = this.node(key);
                final JsonParser traverse = node.traverse();
                ret = reader.readValue(traverse, new TypeReference<Object>() {
                    @Override
                    public Type getType() {
                        return type.type();
                    }
                });
            }
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

    @Override
    protected boolean isNull(@NotNull final String key) {
        requireNonNull(key, "key");

        synchronized (LOCK) {
            return node(key).isNull();
        }
    }


    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Boolean value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Byte value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value == null ? null : value.shortValue());
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Short value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Integer value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Long value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Float value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Double value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final String value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .put(split[split.length - 1], value);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final List<?> value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);
        final ObjectMapper objectMapper = this.mapperSupplier.get();
        final JsonNode jsonNode = objectMapper.valueToTree(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .set(split[split.length - 1], jsonNode);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Set<?> value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);
        final ObjectMapper objectMapper = this.mapperSupplier.get();
        final JsonNode jsonNode = objectMapper.valueToTree(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .set(split[split.length - 1], jsonNode);
        }

        return this;
    }

    @Override
    @NotNull
    @Contract(mutates = "this")
    public LiteKonfiguration putCustom(@NotNull final String key,
                                       @Nullable final Object value) {
        requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);
        final ObjectMapper objectMapper = this.mapperSupplier.get();
        final JsonNode jsonNode = objectMapper.valueToTree(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split)
                .set(split[split.length - 1], jsonNode);
        }

        return this;
    }

    @Override
    @NotNull
    @Contract(mutates = "this")
    public LiteKonfiguration delete(@NotNull final String key) {
        requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        if (key.contains(".")) {
            final String field = key.substring(key.lastIndexOf('.') + 1);
            synchronized (LOCK) {
                this.ensureIntermediateNodesOrNull(key)
                    .ifPresent(node -> node.remove(field));
            }
        }
        else {
            synchronized (LOCK) {
                this.root.remove(key);
            }
        }

        return this;
    }

}
