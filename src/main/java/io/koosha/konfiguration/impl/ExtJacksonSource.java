package io.koosha.konfiguration.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Kind;
import jdk.nashorn.internal.ir.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Reads konfig from a json/yaml source (supplied as string).
 *
 * <p>for {@link #custom(String, Kind)} to work, the supplied mapper must
 * be configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe and immutable.
 */
@Immutable
@ThreadSafe
@ApiStatus.Internal
final class ExtJacksonSource extends Source {

    protected static final String DOT_PATTERN = Pattern.quote(".");

    private final Supplier<ObjectMapper> mapperSupplier;
    private final Supplier<String> jsonSupplier;
    private final String lastJson;
    private final JsonNode root;
    private final Object LOCK = new Object();

    @NotNull
    private final String name;

    private JsonNode node_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String[] split = key.split(DOT_PATTERN);

        JsonNode node = this.root;
        synchronized (LOCK) {
            for (final String sub : split) {
                if (node.isMissingNode())
                    return node;
                node = node.findPath(sub);
            }
        }
        return node;
    }

    @NotNull
    private JsonNode node(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final JsonNode node = node_(key);
        if (node.isMissingNode())
            throw new KfgMissingKeyException(this.name(), key);
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
     *                     deserialize custom types, so that {@link #custom(String, Kind)}
     *                     works as well.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    ExtJacksonSource(@NotNull final String name,
                     @NotNull final Supplier<String> jsonSupplier,
                     @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(jsonSupplier, "jsonSupplier");
        Objects.requireNonNull(objectMapper, "objectMapper");
        Objects.requireNonNull(objectMapper.get(), "supplied mapper is null");

        this.name = name;
        this.jsonSupplier = jsonSupplier;
        this.mapperSupplier = objectMapper;

        this.lastJson = this.jsonSupplier.get();
        Objects.requireNonNull(this.lastJson, "supplied json is null");

        final JsonNode update;
        try {
            update = this.mapperSupplier.get().readTree(this.lastJson);
        }
        catch (final IOException e) {
            throw new KfgSourceException(this.name(), "error parsing json string", e);
        }
        Objects.requireNonNull(update, "root element is null");

        this.root = update;
    }


    @NotNull
    @Override
    public String name() {
        return this.name;
    }

    @Override
    @NotNull
    protected Boolean bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonNode at = node(key);
        return ExtJacksonSourceJsonHelper
            .checkJsonType(at.isBoolean(), Kind.BOOL, at, key, this.name())
            .asBoolean();
    }

    @Override
    @NotNull
    protected Character char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonNode at = node(key);
        return ExtJacksonSourceJsonHelper
            .checkJsonType(at.isTextual() && at.textValue().length() == 1, Kind.STRING, at, key, this.name())
            .textValue()
            .charAt(0);
    }

    @Override
    @NotNull
    protected String string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonNode at = this.node(key);
        return ExtJacksonSourceJsonHelper
            .checkJsonType(at.isTextual(), Kind.STRING, at, key, this.name())
            .asText();
    }

    @NotNull
    @Override
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonNode at = this.node(key);
        return ExtJacksonSourceJsonHelper
            .checkJsonType(
                at.isShort() || at.isInt() || at.isLong(),
                Kind.LONG, at, key, this.name())
            .longValue();
    }

    @NotNull
    @Override
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonNode at = this.node(key);
        return ExtJacksonSourceJsonHelper
            .checkJsonType(
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
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final ObjectMapper reader = this.mapperSupplier.get();
        final TypeFactory tf = reader.getTypeFactory();
        final JavaType ct = tf.constructSimpleType(type.klass(), new JavaType[0]);
        final CollectionType javaType = tf.constructCollectionType(List.class, ct);

        final JsonNode at = this.node(key);
        ExtJacksonSourceJsonHelper.checkJsonType(at.isArray(), type, at, key, this.name());

        final List<?> asList;
        try {
            asList = reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), key, type, at, "type mismatch", e);
        }

        return Collections.unmodifiableList(asList);
    }

    @NotNull
    @Override
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<?> type) {
        return listToSet(key, type);
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

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
            throw new KfgSourceException(this.name(), key, type, null, "jackson error", e);
        }

        if (ret instanceof List)
            return Collections.unmodifiableList((List<?>) ret);
        else if (ret instanceof Set)
            return Collections.unmodifiableSet((Set<?>) ret);
        else
            return ret;
    }

    @Override
    protected boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        return node(key).isNull();
    }

    @Override
    public boolean has(@NotNull final String key,
                       @Nullable final Kind<?> type) {
        Objects.requireNonNull(key, "key");

        if (this.node_(key).isMissingNode())
            return false;

        if (type == null)
            return true;

        if (ExtJacksonSourceJsonHelper.typeMatches(type, this.node(key)))
            return true;

        try {
            this.custom0(key, type);
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }


    @Override
    @Contract(pure = true)
    public boolean hasUpdate() {
        final String newJson = jsonSupplier.get();
        return newJson != null && !Objects.equals(newJson, lastJson);
    }

    @Contract(pure = true,
              value = "->new")
    @Override
    @NotNull
    public Source updatedCopy() {
        return new ExtJacksonSource(this.name(), this.jsonSupplier, this.mapperSupplier);
    }

}
