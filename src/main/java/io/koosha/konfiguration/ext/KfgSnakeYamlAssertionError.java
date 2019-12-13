package io.koosha.konfiguration.ext;

import io.koosha.konfiguration.Q;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static java.util.Collections.emptyMap;

public class KfgSnakeYamlAssertionError extends KfgSnakeYamlError {

    @Getter
    @Nullable
    @Accessors(fluent = true)
    private final Map<String, ?> context;

    public KfgSnakeYamlAssertionError(@Nullable String source,
                                      @Nullable String message,
                                      @Nullable Map<String, ?> context) {
        super(source, message);
        this.context = context;
    }


    public KfgSnakeYamlAssertionError(@Nullable String source,
                                      @Nullable String message) {
        super(source, message);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable String source,
                                      @Nullable String message,
                                      @Nullable Throwable cause) {
        super(source, message, cause);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable String source,
                                      @Nullable String key,
                                      @Nullable Q<?> neededType,
                                      @Nullable Object actualValue,
                                      @Nullable String message,
                                      @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable String source,
                                      @Nullable String key,
                                      @Nullable Q<?> neededType,
                                      @Nullable Object actualValue,
                                      @Nullable String message) {
        super(source, key, neededType, actualValue, message);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable String source,
                                      @Nullable String key,
                                      @Nullable Q<?> neededType,
                                      @Nullable Object actualValue,
                                      @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, cause);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable String source,
                                      @Nullable String key,
                                      @Nullable Q<?> neededType,
                                      @Nullable Object actualValue) {
        super(source, key, neededType, actualValue);
        this.context = emptyMap();
    }

}
