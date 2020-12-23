package io.koosha.konfiguration.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.LiteKonfiguration;
import io.koosha.konfiguration.LiteSource;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@ThreadSafe
final class ExtGsonJsonLiteSource extends LiteSource {

    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    @SuppressWarnings("FieldCanBeLocal")
    private final String json;
    private final Supplier<Gson> mapperSupplier;
    private final JsonObject root;
    private final Object LOCK = new Object();

    @NotNull
    private final String name;

    @Nullable
    private JsonElement node_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String[] split = DOT_PATTERN.split(key);

        JsonElement node = this.root;
        synchronized (LOCK) {
            for (final String sub : split) {
                if (!(node instanceof JsonObject))
                    return null;
                if (!((JsonObject) node).has(sub))
                    return null;
                node = ((JsonObject) node).get(sub);
            }
        }
        return node;
    }

    @NotNull
    private JsonElement node(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final JsonElement node = node_(key);
        if (node == null)
            throw new KfgMissingKeyException(this.name(), key);
        return node;
    }

    @Contract(mutates = "this")
    private JsonObject ensureIntermediateNodes(@NotNull final String[] key,
                                               @NotNull final String keyJoined) {
        JsonObject from = this.root;

        for (int i = 0; i < key.length - 1; i++) {
            JsonElement next = from.get(key[i]);
            if (next == null) {
                from.add(key[i], new JsonObject());
                next = from.get(key[i]);
            }
            else if (!(next instanceof JsonObject)) {
                throw new KfgTypeException(
                    this.name, keyJoined, null, null,
                    "expected all objects in path, found=" + next + " at=" + key[i]);
            }
            from = (JsonObject) next;
        }

        return from;
    }

    @Contract(pure = true)
    private Optional<JsonObject> ensureIntermediateNodesOrNull(@NotNull final String[] key,
                                                               @NotNull final String keyJoined) {
        JsonObject from = this.root;

        for (int i = 0; i < key.length - 1; i++) {
            JsonElement next = from.get(key[i]);
            if (next == null)
                return Optional.empty();
            else if (!(next instanceof JsonObject))
                throw new KfgTypeException(this.name, keyJoined, null, null,
                    "expected all objects in path, found=" + next + " at=" + key[i]);
            from = (JsonObject) next;
        }

        return Optional.of(from);
    }


    ExtGsonJsonLiteSource(@NotNull final String name,
                          @NotNull final String json,
                          @NotNull final Supplier<Gson> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");

        this.name = name;
        this.json = json;
        this.mapperSupplier = objectMapper;

        Objects.requireNonNull(this.mapperSupplier.get(), "supplied mapper is null");

        final JsonObject update;
        try {
            update = this.mapperSupplier.get().fromJson(this.json, JsonObject.class);
        }
        catch (final JsonSyntaxException e) {
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

        final JsonElement at = node(key);

        return ExtGsonSourceHelper.checkJsonType(ExtGsonSourceHelper.typeMatches(Kind.BOOL, at), this.name(), Kind.BOOL, at, key)
                                  .getAsBoolean();
    }

    @Override
    @NotNull
    protected Character char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = node(key);

        return ExtGsonSourceHelper.checkJsonType(ExtGsonSourceHelper.typeMatches(Kind.CHAR, at), this.name(), Kind.CHAR, at, key)
                                  .getAsString()
                                  .charAt(0);
    }

    @Override
    @NotNull
    protected String string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = node(key);

        return ExtGsonSourceHelper.checkJsonType(ExtGsonSourceHelper.typeMatches(Kind.STRING, at), this.name(), Kind.STRING, at, key)
                                  .getAsString();
    }

    @NotNull
    @Override
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = node(key);
        return ExtGsonSourceHelper.checkJsonType(ExtGsonSourceHelper.typeMatches(Kind.LONG, at), this.name(), Kind.LONG, at, key)
                                  .getAsLong();
    }

    @NotNull
    @Override
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = node(key);
        return ExtGsonSourceHelper.checkJsonType(ExtGsonSourceHelper.typeMatches(Kind.DOUBLE, at), this.name(), Kind.DOUBLE, at, key)
                                  .getAsDouble();
    }

    @NotNull
    @Override
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<?> type) {
        return listToSet(key, type);
    }

    @NotNull
    @Override
    protected List<?> list0(@NotNull final String key,
                            @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final Gson reader = this.mapperSupplier.get();

        final JsonElement at = node(key);
        ExtGsonSourceHelper.checkJsonType(at.isJsonArray(), this.name(), type, at, key);
        final JsonArray asJsonArray = at.getAsJsonArray();

        final Type typeToken = TypeToken.get(type.type()).getType();

        final List<?> asList = new ArrayList<>(asJsonArray.size());
        for (final JsonElement jsonElement : asJsonArray)
            try {
                asList.add(reader.fromJson(jsonElement, typeToken));
            }
            catch (final JsonSyntaxException e) {
                throw new KfgSourceException(this.name(), key, type, null, "gson error", e);
            }

        return Collections.unmodifiableList(asList);
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final Gson reader = this.mapperSupplier.get();
        final JsonElement node = this.node(key);

        final Type typeToken = TypeToken.get(type.type()).getType();

        Object ret;
        try {
            ret = reader.fromJson(node, typeToken);
        }
        catch (final JsonSyntaxException e) {
            throw new KfgTypeException(this.name(), key, type, null, "gson error", e);
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

        return node(key).isJsonNull();
    }

    @Override
    @NotNull
    public LiteKonfiguration delete(final @NotNull String key) {
        Objects.requireNonNull(key, "key");
        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        if (key.contains(".")) {
            final String field = key.substring(key.lastIndexOf('.') + 1);
            final String[] split = DOT_PATTERN.split(key);
            synchronized (LOCK) {
                this.ensureIntermediateNodesOrNull(split, key)
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

    @Override
    public boolean has(@NotNull final String key,
                       @Nullable final Kind<?> type) {
        Objects.requireNonNull(key, "key");

        if (this.node_(key) == null)
            return false;
        if (type == null)
            return true;

        final JsonElement node = this.node(key);

        if (ExtGsonSourceHelper.typeMatches(type, node))
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
    public boolean isReadonly() {
        return false;
    }

    @Override
    @NotNull
    public LiteKonfiguration toWritableCopy() {
        return new ExtGsonJsonLiteSource(this.name, this.serialize(), this.mapperSupplier);
    }

    @Override
    @NotNull
    public String serialize() {
        return this.root.toString();
    }


    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Boolean value) {
        Objects.requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        final JsonElement serialized = value == null
            ? JsonNull.INSTANCE
            : new JsonPrimitive(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split, key)
                .add(split[split.length - 1], serialized);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Byte value) {
        return putNumber(key, value);
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Short value) {
        return putNumber(key, value);
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Integer value) {
        return putNumber(key, value);
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Long value) {
        return putNumber(key, value);
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Float value) {
        return putNumber(key, value);
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Double value) {
        return putNumber(key, value);
    }

    private LiteKonfiguration putNumber(@NotNull final String key,
                                        @Nullable final Number value) {
        Objects.requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        final JsonElement serialized = value == null
            ? JsonNull.INSTANCE
            : new JsonPrimitive(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split, key)
                .add(split[split.length - 1], serialized);
        }
        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final String value) {
        Objects.requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);

        final JsonElement serialized = value == null
            ? JsonNull.INSTANCE
            : new JsonPrimitive(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split, key)
                .add(split[split.length - 1], serialized);
        }

        return this;
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final List<?> value) {
        return this.putCollection(key, value);
    }

    @Override
    @Contract(mutates = "this")
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 @Nullable final Set<?> value) {
        return this.putCollection(key, value);
    }

    private LiteKonfiguration putCollection(@NotNull final String key,
                                            @Nullable final Collection<?> value) {
        Objects.requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);
        final Gson objectMapper = this.mapperSupplier.get();

        final JsonElement serialize = value == null
            ? JsonNull.INSTANCE
            : objectMapper.toJsonTree(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split, key)
                .add(split[split.length - 1], serialize);
        }

        return this;
    }

    @Override
    @NotNull
    @Contract(mutates = "this")
    public LiteKonfiguration putCustom(@NotNull final String key,
                                       @Nullable final Object value) {
        Objects.requireNonNull(key, "key");

        final String[] split = DOT_PATTERN.split(key);
        final Gson objectMapper = this.mapperSupplier.get();

        final JsonElement serialize = value == null
            ? JsonNull.INSTANCE
            : objectMapper.toJsonTree(value);

        synchronized (LOCK) {
            this.ensureIntermediateNodes(split, key)
                .add(split[split.length - 1], serialize);
        }

        return this;
    }

}
