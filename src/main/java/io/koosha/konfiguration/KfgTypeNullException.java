package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
@ThreadSafe
public class KfgTypeNullException extends KfgTypeException {

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Q<?> neededType,
                                @Nullable final String message,
                                @Nullable final Throwable cause) {
        super(source, key, neededType, null, message, cause);
    }

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Q<?> neededType,
                                @Nullable final String message) {
        super(source, key, neededType, null, message);
    }

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Q<?> neededType,
                                @Nullable final Throwable cause) {
        super(source, key, neededType, null, cause);
    }

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Q<?> neededType) {
        super(source, key, neededType, null);
    }

}
