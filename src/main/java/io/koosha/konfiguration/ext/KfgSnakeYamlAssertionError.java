package io.koosha.konfiguration.ext;

import io.koosha.konfiguration.Q;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static java.util.Collections.emptyMap;

public class KfgSnakeYamlAssertionError extends KfgSnakeYamlError {

    @Nullable
    private final Map<String, ?> context;

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String message,
                                      @Nullable final Map<String, ?> context) {
        super(source, message);
        this.context = context;
    }


    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String message) {
        super(source, message);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String message,
                                      @Nullable final Throwable cause) {
        super(source, message, cause);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String key,
                                      @Nullable final Q<?> neededType,
                                      @Nullable final Object actualValue,
                                      @Nullable final String message,
                                      @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String key,
                                      @Nullable final Q<?> neededType,
                                      @Nullable final Object actualValue,
                                      @Nullable final String message) {
        super(source, key, neededType, actualValue, message);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String key,
                                      @Nullable final Q<?> neededType,
                                      @Nullable final Object actualValue,
                                      @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
        this.context = emptyMap();
    }

    public KfgSnakeYamlAssertionError(@Nullable final String source,
                                      @Nullable final String key,
                                      @Nullable final Q<?> neededType,
                                      @Nullable final Object actualValue) {
        super(source, key, neededType, actualValue);
        this.context = emptyMap();
    }

    @Nullable
    public Map<String, ?> context() {
        return this.context;
    }

}
