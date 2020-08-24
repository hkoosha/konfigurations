package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.type.Kind;
import jdk.nashorn.internal.ir.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Immutable
@ThreadSafe
final class ExtJacksonSourceJsonHelper {

    ExtJacksonSourceJsonHelper() {
        throw new UnsupportedOperationException();
    }

    @Contract(pure = true,
              value = "->new")
    @NotNull
    static ObjectMapper mapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.findAndRegisterModules();
        return mapper;
    }

    static void ensureLibraryJarIsOnPath() {
        final String klass = "com.fasterxml.jackson.databind.ObjectMapper";
        try {
            Class.forName(klass);
        }
        catch (final ClassNotFoundException e) {
            throw new KfgSourceException(null,
                "jackson yaml library is required to be present in " +
                    "the class path, can not find the class: " + klass, e);
        }
    }

    @Contract(pure = true)
    static boolean typeMatches(@NotNull final Kind<?> type,
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

    @NotNull
    @Contract(pure = true)
    static JsonNode checkJsonType(final boolean condition,
                                  @NotNull final Kind<?> required,
                                  @NotNull final JsonNode node,
                                  @NotNull final String key,
                                  @NotNull final String name) {
        if (!condition)
            throw new KfgTypeException(name, key, required, node);

        if (node.isNull())
            throw new KfgAssertionException(name, key, required, null, null);

        return node;
    }

}
