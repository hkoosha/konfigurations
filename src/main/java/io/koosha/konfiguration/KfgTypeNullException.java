package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
public class KfgTypeNullException extends KfgTypeException {

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Kind<?> neededType,
                                @Nullable final String message,
                                @Nullable final Throwable cause) {
        super(source, key, neededType, null, message, cause);
    }

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Kind<?> neededType,
                                @Nullable final String message) {
        super(source, key, neededType, null, message);
    }

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Kind<?> neededType,
                                @Nullable final Throwable cause) {
        super(source, key, neededType, null, cause);
    }

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Kind<?> neededType) {
        super(source, key, neededType, null);
    }

}
