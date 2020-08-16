package io.koosha.konfiguration.impl.v8;

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
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        return jacksonJson(name, json, ExtJacksonSourceJsonHelper::mapper);
    }

    @Override
    public @NotNull LiteKonfiguration jacksonJson(@NotNull final String name,
                                                  @NotNull final String json,
                                                  @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(objectMapper, "objectMapper");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        return new ExtJacksonLiteSource(name, json, objectMapper);
    }

    @Override
    public @NotNull LiteKonfiguration jacksonYaml(@NotNull final String name,
                                                  @NotNull final String yaml) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        return jacksonYaml(name, yaml, ExtJacksonSourceYamlHelper::mapper);
    }

    @Override
    public @NotNull LiteKonfiguration jacksonYaml(@NotNull final String name,
                                                  @NotNull final String yaml,
                                                  @NotNull final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(yaml, "yaml");
        Objects.requireNonNull(objectMapper, "objectMapper");
        ExtJacksonSourceYamlHelper.ensureLibraryJarIsOnPath();

        return new ExtJacksonLiteSource(name, yaml, objectMapper);
    }

}
