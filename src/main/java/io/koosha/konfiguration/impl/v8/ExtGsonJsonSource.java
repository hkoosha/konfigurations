
package io.koosha.konfiguration.impl.v8;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.koosha.konfiguration.KfgMissingKeyException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static io.koosha.konfiguration.impl.v8.ExtGsonSourceHelper.checkJsonType;
import static io.koosha.konfiguration.impl.v8.ExtGsonSourceHelper.typeMatches;

@Immutable
@ThreadSafe
@ApiStatus.Internal
public final class ExtGsonJsonSource extends Source {

    private static final String DOT_PATTERN = Pattern.quote(".");

    private final Supplier<Gson> mapperSupplier;
    private final Supplier<String> json;
    private final String lastJson;
    private final JsonObject root;
    private final Object LOCK = new Object();

    @NotNull
    private final String name;

    @Nullable
    private JsonElement node_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        if (key.isEmpty())
            throw new KfgMissingKeyException(this.name(), key, "empty konfig key");

        final String[] split = key.split(DOT_PATTERN);

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


    ExtGsonJsonSource(@NotNull final String name,
                      @NotNull final Supplier<String> jsonSupplier,
                      @NotNull final Supplier<Gson> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(jsonSupplier, "jsonSupplier");
        Objects.requireNonNull(objectMapper, "objectMapper");

        ExtGsonSourceHelper.ensureLibraryJarIsOnPath();

        this.name = name;
        this.json = jsonSupplier;
        this.mapperSupplier = objectMapper;
        this.lastJson = this.json.get();

        Objects.requireNonNull(this.lastJson, "supplied json is null");
        Objects.requireNonNull(this.mapperSupplier.get(), "supplied mapper is null");

        final JsonObject update;
        try {
            update = this.mapperSupplier.get().fromJson(this.lastJson, JsonObject.class);
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

        final JsonElement at = this.node(key);

        return checkJsonType(typeMatches(Kind.BOOL, at), this.name(), Kind.BOOL, at, key)
            .getAsBoolean();
    }

    @Override
    @NotNull
    protected Character char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = this.node(key);

        return checkJsonType(typeMatches(Kind.CHAR, at), this.name(), Kind.CHAR, at, key)
            .getAsString()
            .charAt(0);
    }

    @Override
    @NotNull
    protected String string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = this.node(key);

        return checkJsonType(typeMatches(Kind.STRING, at), this.name(), Kind.STRING, at, key)
            .getAsString();
    }

    @NotNull
    @Override
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = this.node(key);
        return checkJsonType(typeMatches(Kind.LONG, at), this.name(), Kind.LONG, at, key)
            .getAsLong();
    }

    @NotNull
    @Override
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final JsonElement at = this.node(key);
        return checkJsonType(typeMatches(Kind.DOUBLE, at), this.name(), Kind.DOUBLE, at, key)
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

        final JsonElement at = this.node(key);
        checkJsonType(at.isJsonArray(), this.name(), type, at, key);
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

        return this.node(key).isJsonNull();
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

        if (typeMatches(type, node))
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
        final String newJson = json.get();
        return newJson != null && !Objects.equals(newJson, lastJson);
    }

    @Contract(pure = true,
              value = "->new")
    @Override
    @NotNull
    public Source updatedCopy() {
        return new ExtGsonJsonSource(this.name(), this.json, this.mapperSupplier);
    }

}
