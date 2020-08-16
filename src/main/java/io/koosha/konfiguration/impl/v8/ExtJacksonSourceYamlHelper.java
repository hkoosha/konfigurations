package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.koosha.konfiguration.KfgSourceException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

final class ExtJacksonSourceYamlHelper {

    private ExtJacksonSourceYamlHelper() {
        throw new UnsupportedOperationException();
    }

    @Contract(pure = true,
              value = "->new")
    @NotNull
    static ObjectMapper mapper() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.findAndRegisterModules();
        return mapper;
    }

    static void ensureLibraryJarIsOnPath() {
        final String klass = "com.fasterxml.jackson.dataformat.yaml.YAMLFactory";
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
