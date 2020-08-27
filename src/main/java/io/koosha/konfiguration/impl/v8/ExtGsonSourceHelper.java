package io.koosha.konfiguration.impl.v8;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

final class ExtGsonSourceHelper {

    private static final Gson GSON = new Gson();


    private ExtGsonSourceHelper() {
        throw new UnsupportedOperationException();
    }

    static void ensureLibraryJarIsOnPath() {
        // final String klass = "com.google.gson.Gson";
        // try {
        //     Class.forName(klass);
        // }
        // catch (final ClassNotFoundException e) {
        //     throw new KfgSourceException(null,
        //         "gson library is required to be present in " +
        //             "the class path, can not find the class: " + klass, e);
        // }
    }

    @NotNull
    @Contract(pure = true)
    static JsonElement checkJsonType(final boolean condition,
                                     @NotNull final String name,
                                     @NotNull final Kind<?> required,
                                     @NotNull final JsonElement node,
                                     @NotNull final String key) {
        if (!condition)
            throw new KfgTypeException(name, key, required, node);

        if (node.isJsonNull())
            throw new KfgTypeException(name, key, required, null, "node is null");

        return node;
    }

    @Contract(pure = true)
    static boolean typeMatches(@NotNull final Kind<?> type,
                               @NotNull final JsonElement node) {
        if (type.isNull() && node.isJsonNull()
            || type.isList() && node.isJsonArray()
            || type.isSet() && node.isJsonArray())
            return true;

        if (!(node instanceof JsonPrimitive))
            return false;

        final JsonPrimitive primitive = (JsonPrimitive) node;

        if (type.isBool() && primitive.isBoolean()
            || type.isChar() && primitive.isString() && primitive.getAsString().length() == 1
            || type.isString() && primitive.isString())
            return true;

        if (!primitive.isNumber())
            return false;

        final long l = primitive.getAsLong();
        if (type.isByte() && Byte.MIN_VALUE <= l && l <= Byte.MAX_VALUE
            || type.isShort() && Short.MIN_VALUE <= l && l <= Short.MAX_VALUE
            || type.isInt() && Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE
            || type.isLong())
            return true;

        final double d = primitive.getAsDouble();
        if (type.isFloat() && Float.MIN_VALUE <= d && d <= Float.MAX_VALUE)
            return true;

        return type.isDouble();
    }

    @Contract(pure = true)
    @NotNull
    static Gson mapper() {
        return GSON;
    }

}
