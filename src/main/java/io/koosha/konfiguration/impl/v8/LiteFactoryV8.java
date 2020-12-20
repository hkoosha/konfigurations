package io.koosha.konfiguration.impl.v8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.koosha.konfiguration.LiteKonfiguration;
import io.koosha.konfiguration.LiteKonfigurationFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Internal
public class LiteFactoryV8 implements LiteKonfigurationFactory {

    @NotNull
    @Contract(pure = true,
              value = "->new")
    public static LiteKonfigurationFactory getInstanceV8() {
        return new LiteFactoryV8();
    }

    @Override
    @NotNull
    public LiteKonfiguration jacksonJson(@NotNull final String name,
                                         @NotNull final String json) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");

        return jacksonJson(name, json, ExtJacksonSourceJsonHelper::mapper);
    }

    @Override
    @NotNull
    public LiteKonfiguration jacksonJson(@NotNull final String name,
                                         @NotNull final String json,
                                         @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");

        return new ExtJacksonLiteSource(name, json, objectMapper);
    }

    @Override
    @NotNull
    public LiteKonfiguration jacksonYaml(@NotNull final String name,
                                         @NotNull final String yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");

        return jacksonYaml(name, yaml, ExtJacksonSourceYamlHelper::mapper);
    }

    @Override
    @NotNull
    public LiteKonfiguration jacksonYaml(@NotNull final String name,
                                         @NotNull final String yaml,
                                         @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");

        return new ExtJacksonLiteSource(name, yaml, objectMapper);
    }

    @Override
    @NotNull
    public LiteKonfiguration gsonJson(@NotNull final String name,
                                      @NotNull final String json) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");

        return gsonJson(name, json, ExtGsonSourceHelper::mapper);
    }

    @Override
    public @NotNull LiteKonfiguration gsonJson(@NotNull final String name,
                                               @NotNull final String json,
                                               @NotNull final Supplier<Gson> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");

        return new ExtGsonJsonLiteSource(name, json, objectMapper);
    }

}
