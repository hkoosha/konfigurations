package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import jdk.nashorn.internal.ir.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Immutable
@ThreadSafe
final class ExtJacksonSourceYamlHelper {

    private ExtJacksonSourceYamlHelper() {
        throw new UnsupportedOperationException();
    }

    @Contract(pure = true,
              value = "->new")
    @NotNull
    static ObjectMapper mapper() {
        final JsonFactory yamlFactory = new YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        final ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.findAndRegisterModules();
        return mapper;
    }

}
