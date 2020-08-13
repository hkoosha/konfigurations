package io.koosha.konfiguration.lite.v8;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.LiteKonfiguration;
import io.koosha.konfiguration.LiteKonfigurationFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public class LiteFactoryV8 implements LiteKonfigurationFactory {

    @NotNull
    public static LiteKonfigurationFactory getInstanceV8() {
        return new LiteFactoryV8();
    }

    @Override
    public @NotNull LiteKonfiguration jacksonJson(@NotNull final String name,
                                                  @NotNull final String json) {
        return new ExtJacksonJsonLiteSource(name, json);
    }

    @Override
    public @NotNull LiteKonfiguration jacksonJson(@NotNull final String name,
                                                  @NotNull final String json,
                                                  @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        return new ExtJacksonJsonLiteSource(name, json, objectMapper);
    }
}
