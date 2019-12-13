package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
public class KfgConcurrencyException extends KfgException {

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String message) {
        super(source, null, null, null, message);
    }

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String message,
                                   @Nullable Throwable cause) {
        super(source, null, null, null, message, cause);
    }

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable Throwable cause) {
        super(source, null, null, null, "", cause);
    }

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String key,
                                   @Nullable Q<?> neededType,
                                   @Nullable Object actualValue,
                                   @Nullable String message, @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String key,
                                   @Nullable Q<?> neededType, @Nullable Object actualValue, @Nullable String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String key,
                                   @Nullable Q<?> neededType,
                                   @Nullable Object actualValue,
                                   @Nullable Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String key,
                                   @Nullable Q<?> neededType, @Nullable Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
