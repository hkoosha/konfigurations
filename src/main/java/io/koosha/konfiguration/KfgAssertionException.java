package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;

/**
 * This shouldn't happen. This must be prevented by konfigurations.
 */
@ThreadSafe
public class KfgAssertionException extends KfgException {

    @NotNull
    private final Map<String, ?> context;

    public KfgAssertionException(@Nullable String message) {
        super(null, null, null, null, message);
        this.context = emptyMap();
    }

    public KfgAssertionException(@Nullable String message,
                                 @NotNull final Map<String, ?> context) {
        super(null, null, null, null, message);
        Objects.requireNonNull(context, "context");
        this.context = context;
    }

    public KfgAssertionException(@Nullable String source,
                                 @Nullable String message,
                                 @Nullable Throwable cause) {
        super(source, null, null, message, cause);
        this.context = emptyMap();
    }

    public KfgAssertionException(@Nullable String source,
                                 @Nullable String key,
                                 @Nullable Q<?> neededType,
                                 @Nullable Object actualValue,
                                 @Nullable String message,
                                 @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
        this.context = emptyMap();
    }

    public KfgAssertionException(@Nullable String source,
                                 @Nullable String key,
                                 @Nullable Q<?> neededType,
                                 @Nullable Object actualValue,
                                 @Nullable String message) {
        super(source, key, neededType, actualValue, message);
        this.context = emptyMap();
    }

    public KfgAssertionException(@Nullable String source,
                                 @Nullable String key,
                                 @Nullable Q<?> neededType,
                                 @Nullable Object actualValue,
                                 @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, cause);
        this.context = emptyMap();
    }

    public KfgAssertionException(@Nullable String source,
                                 @Nullable String key,
                                 @Nullable Q<?> neededType,
                                 @Nullable Object actualValue) {
        super(source, key, neededType, actualValue);
        this.context = emptyMap();
    }

    @NotNull
    public Map<String, ?> context() {
        return this.context;
    }

}