package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
@ThreadSafe
public class KfgTypeException extends KfgException {

    public KfgTypeException(@Nullable final String source,
                            @Nullable final String key,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue,
                            @Nullable final String message,
                            @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgTypeException(@Nullable final String source,
                            @Nullable final String key,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue,
                            @Nullable final String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgTypeException(@Nullable final String source,
                            @Nullable final String key,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue,
                            @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgTypeException(@Nullable final String source,
                            @Nullable final String key,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
