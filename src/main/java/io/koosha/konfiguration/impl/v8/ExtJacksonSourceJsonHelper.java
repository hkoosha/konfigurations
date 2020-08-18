package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.koosha.konfiguration.KfgSourceException;
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

}
