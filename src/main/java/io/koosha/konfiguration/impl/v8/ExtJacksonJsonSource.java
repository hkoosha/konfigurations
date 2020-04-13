package io.koosha.konfiguration.impl.v8;

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
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Reads konfig from a json source (supplied as string).
 *
 * <p>for {@link #custom(String, Typer)} to work, the supplied json reader must
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
    private final String lastJson;
    private final JsonNode root;
    private final Object LOCK = new Object();

    @NotNull
    private final String name;

    @NotNull
    private final KonfigurationManager8 manager = new KonfigurationManager8() {

        /**
         * {@inheritDoc}
         */
        @Override
        @Contract(pure = true)
        public boolean hasUpdate() {
            final String newJson = json.get();
            return newJson != null && !Objects.equals(newJson, lastJson);
        }

        /**
         * {@inheritDoc}
         */
        @Contract(pure = true,
                value = "-> new")
        @Override
        public @NotNull Source _update() {
            return this.hasUpdate()
                    ? new ExtJacksonJsonSource(name(), json, mapperSupplier)
                    : ExtJacksonJsonSource.this;
        }

    };

    private JsonNode node_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String k = "/" + key.replace('.', '/');
        return this.root.findPath(k);
    }

    @NotNull
    private JsonNode node(@NotNull final String key) {
        synchronized (LOCK) {
            Objects.requireNonNull(key, "key");

            if (key.isEmpty())
                throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

            final JsonNode node = node_(key);
            if (node.isMissingNode())
                throw new KfgMissingKeyException(this.name(), key);
            return node;
        }
    }

    @NotNull
    private JsonNode checkJsonType(final boolean condition,
                                   final Typer<?> required,
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
     * @param jsonSupplier backing store provider. Must always return a non-null valid json
     *                     string.
     * @param objectMapper {@link ObjectMapper} provider. Must always return a valid
     *                     non-null ObjectMapper, and if required, it must be able to
     *                     deserialize custom types, so that {@link #custom(String, Typer)}
     *                     works as well.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    ExtJacksonJsonSource(@NotNull final String name,
                         @NotNull final Supplier<String> jsonSupplier,
                         @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(jsonSupplier, "jsonSupplier");
        Objects.requireNonNull(objectMapper, "objectMapper");

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

        this.json = jsonSupplier;
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
        this.lastJson = this.json.get();
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
    @NotNull
    @Override
    public KonfigurationManager8 manager() {
        return this.manager;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Boolean bool0(@NotNull final String key) {
        synchronized (LOCK) {
            Objects.requireNonNull(key, "key");
            final JsonNode at = node(key);
            return checkJsonType(at.isBoolean(), Typer.BOOL, at, key).asBoolean();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Character char0(@NotNull final String key) {
        synchronized (LOCK) {
            Objects.requireNonNull(key, "key");
            final JsonNode at = node(key);
            return checkJsonType(at.isTextual() && at.textValue().length() == 1, Typer.STRING, at, key)
                    .textValue()
                    .charAt(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    String string0(@NotNull final String key) {
        synchronized (LOCK) {
            Objects.requireNonNull(key, "key");
            final JsonNode at = node(key);
            return checkJsonType(at.isTextual(), Typer.STRING, at, key).asText();
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Number number0(@NotNull final String key) {
        synchronized (LOCK) {
            Objects.requireNonNull(key, "key");
            final JsonNode at = node(key);
            return checkJsonType(
                    at.isShort() || at.isInt() || at.isLong(),
                    Typer.LONG, at, key).longValue();
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            final JsonNode at = node(key);
            return checkJsonType(
                    at.isFloat()
                            || at.isDouble()
                            || at.isShort()
                            || at.isInt()
                            || at.isLong(),
                    Typer.DOUBLE, at, key).doubleValue();
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    List<?> list0(@NotNull final String key,
                  @NotNull final Typer<? extends List<?>> type) {
        synchronized (LOCK) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(type, "type");

            final JsonNode at = node(key);
            checkJsonType(at.isArray(), Typer.UNKNOWN_LIST, at, key);
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
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Set<?> set0(@NotNull final String key,
                @NotNull final Typer<? extends Set<?>> type) {
        synchronized (LOCK) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(type, "type");

            final JsonNode at = node(key);

            checkJsonType(at.isArray(), Typer.UNKNOWN_SET, at, key);
            final ObjectMapper reader = this.mapperSupplier.get();
            final CollectionType javaType = reader
                    .getTypeFactory()
                    .constructCollectionType(Set.class, type.klass());

            final Set<?> s;

            try {
                s = reader.readValue(at.traverse(), javaType);
            }
            catch (final IOException e) {
                throw new KfgTypeException(this.name(), key, Typer.UNKNOWN_LIST, type, "type mismatch", e);
            }

            final List<?> l = this.list0(key, Typer.UNKNOWN_LIST);
            if (l.size() != s.size())
                throw new KfgTypeException(this.name(), key, type, at, "type mismatch, duplicate values");

            return s;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Map<?, ?> map0(@NotNull final String key,
                   @NotNull final Typer<? extends Map<?, ?>> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        synchronized (LOCK) {
            final JsonNode at = node(key);
            checkJsonType(at.isObject(), Typer.UNKNOWN_MAP, at, key);
            final ObjectMapper reader = this.mapperSupplier.get();
            final MapType javaType = reader
                    .getTypeFactory()
                    .constructMapType(Map.class, String.class, type.klass());

            try {
                return reader.readValue(at.traverse(), javaType);
            }
            catch (final IOException e) {
                throw new KfgTypeException(this.name(), key, Typer.UNKNOWN_LIST, type, "type mismatch", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Object custom0(@NotNull final String key,
                   @NotNull final Typer<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        synchronized (LOCK) {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonParser traverse = this.node(key).traverse();

            try {
                return reader.readValue(traverse, type.klass());
            }
            catch (final IOException e) {
                throw new KfgTypeException(this.name(), key, type, null, "jackson error", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            return node(key).isNull();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull final String key,
                       @Nullable final Typer<?> type) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
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
                    this.set0(key, Typer.UNKNOWN_SET).size() != this.list0(key, Typer.UNKNOWN_LIST).size())
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

}
