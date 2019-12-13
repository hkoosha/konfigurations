package io.koosha.konfiguration.impl.v0;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import io.koosha.konfiguration.*;
import io.koosha.konfiguration.ext.KfgJacksonError;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Reads konfig from a json source (supplied as string).
 *
 * <p>for {@link #custom(String, Q)} to work, the supplied json reader must
 * be configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe and immutable.
 */
@Immutable
@ThreadSafe
@ApiStatus.Internal
final class ExtJacksonJsonSource extends Source {

    @Contract(pure = true,
            value = "->new")
    @NotNull
    static ObjectMapper defaultJacksonObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    private final Supplier<ObjectMapper> mapperSupplier;
    private final Supplier<String> json;
    private final int lastHash;
    private final JsonNode root;

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    @Accessors(fluent = true)
    @Getter
    private final KonfigurationManager0 manager = new KonfigurationManager0() {

        /**
         * {@inheritDoc}
         */
        @Override
        @Contract(pure = true)
        public boolean hasUpdate() {
            final String newJson = json.get();
            return newJson != null && newJson.hashCode() != lastHash;
        }

        /**
         * {@inheritDoc}
         */
        @Contract(pure = true,
                value = "-> new")
        @Override
        public @NotNull Konfiguration0 _update() {
            return this.hasUpdate()
                   ? new ExtJacksonJsonSource(name(), json, mapperSupplier)
                   : ExtJacksonJsonSource.this;
        }

    };

    private JsonNode node_(@NonNull @NotNull final String key) {
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String k = "/" + key.replace('.', '/');
        return this.root.findPath(k);
    }

    @Synchronized
    private JsonNode node(@NotNull @NonNull final String key) {
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final JsonNode node = node_(key);
        if (node.isMissingNode())
            throw new KfgMissingKeyException(this.name(), key);
        return node;
    }

    private JsonNode checkJsonType(final boolean condition,
                                   final Q<?> required,
                                   final JsonNode node,
                                   final String key) {
        if (!condition)
            throw new KfgTypeException(this.name(), key, required, node);
        if (node.isNull())
            throw new KfgTypeNullException(this.name(), key, required);
        return node;
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
     *                     deserialize custom types, so that {@link #custom(String, Q)}
     *                     works as well.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    ExtJacksonJsonSource(@NotNull @NonNull final String name,
                         @NotNull @NonNull final Supplier<String> json,
                         @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        this.name = name;
        // Check early, so we're not fooled with a dummy object reader.
        try {
            Class.forName("com.fasterxml.jackson.databind.JsonNode");
        }
        catch (final ClassNotFoundException e) {
            // XXX
            throw new KfgJacksonError(this.name(),
                    "jackson library is required to be present in " +
                            "the class path, can not find the class: " +
                            "com.fasterxml.jackson.databind.JsonNode", e);
        }

        this.json = json;
        this.mapperSupplier = objectMapper;

        requireNonNull(this.json.get(), "supplied json is null");
        requireNonNull(this.mapperSupplier.get(), "supplied mapper is null");

        final JsonNode update;
        try {
            update = this.mapperSupplier.get().readTree(this.json.get());
        }
        catch (final IOException e) {
            // XXX
            throw new KfgJacksonError(this.name(), "error parsing json string", e);
        }

        requireNonNull(update, "root element is null");

        this.root = update;
        this.lastHash = this.json.get().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Synchronized
    Boolean bool0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(at.isBoolean(), Q.BOOL, at, key).asBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Synchronized
    Character char0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(at.isTextual() && at.textValue().length() == 1, Q.STRING, at, key)
                .textValue()
                .charAt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Synchronized
    String string0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(at.isTextual(), Q.STRING, at, key).asText();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    @Synchronized
    Number number0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                at.isShort() || at.isInt() || at.isLong(),
                Q.LONG, at, key).longValue();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    @Synchronized
    Number numberDouble0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                at.isFloat()
                        || at.isDouble()
                        || at.isShort()
                        || at.isInt()
                        || at.isLong(),
                Q.DOUBLE, at, key).doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    @Synchronized
    List<?> list0(@NotNull @NonNull final String key,
                  @NotNull Q<? extends List<?>> type) {
        final JsonNode at = node(key);
        checkJsonType(at.isArray(), Q.UNKNOWN_LIST, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader
                .getTypeFactory()
                .constructCollectionType(List.class, type.klass());

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), key, type, at, "type mismatch", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    @Synchronized
    Set<?> set0(@NotNull @NonNull final String key,
                @NotNull Q<? extends Set<?>> type) {
        final JsonNode at = node(key);

        checkJsonType(at.isArray(), Q.UNKNOWN_SET, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader
                .getTypeFactory()
                .constructCollectionType(Set.class, type.klass());

        final Set<?> s;

        try {
            s = reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), key, Q.UNKNOWN_LIST, type, "type mismatch", e);
        }

        final List<?> l = this.list0(key, Q.UNKNOWN_LIST);
        if (l.size() != s.size())
            throw new KfgTypeException(this.name(), key, type, at, "type mismatch, duplicate values");

        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Synchronized
    Map<?, ?> map0(@NotNull @NonNull final String key,
                   @NotNull Q<? extends Map<?, ?>> type) {
        final JsonNode at = node(key);
        checkJsonType(at.isObject(), Q.UNKNOWN_MAP, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final MapType javaType = reader
                .getTypeFactory()
                .constructMapType(Map.class, String.class, type.klass());

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), key, Q.UNKNOWN_LIST, type, "type mismatch", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Synchronized
    Object custom0(@NotNull @NonNull final String key,
                   @NotNull @NonNull final Q<?> type) {
        final ObjectMapper reader = this.mapperSupplier.get();
        final JsonParser traverse = this.node(key).traverse();

        try {
            return reader.readValue(traverse, type.klass());
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), key, type, null, "jackson error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Synchronized
    boolean isNull(@NonNull @NotNull String key) {
        return node(key).isNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Synchronized
    public boolean has(@NotNull @NonNull final String key,
                       @Nullable final Q<?> type) {
        if (this.node_(key).isMissingNode())
            return false;
        if (type == null)
            return true;

        final JsonNode node = this.node(key);

        if (type.isNull() && node.isNull()
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
                || type.isSet() && node.isArray() &&
                this.set0(key, Q.UNKNOWN_SET).size() != this.list0(key, Q.UNKNOWN_LIST).size())
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
