package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

/**
 * Exceptions regarding the source (backing storage), or as a wrapper around
 * exceptions thrown by the backing storage.
 */
@ThreadSafe
public class KfgSourceException extends KfgException {

    public KfgSourceException(@Nullable final String source,
                              @Nullable final String message) {
        super(source, null, null, null, message);
    }

    public KfgSourceException(@Nullable final String source,
                              @Nullable final String message,
                              @Nullable final Throwable cause) {
        super(source, null, null, null, message, cause);
    }


    public KfgSourceException(@Nullable final String source,
                              @Nullable final String key,
                              @Nullable final Q<?> neededType,
                              @Nullable final Object actualValue,
                              @Nullable final String message,
                              @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgSourceException(@Nullable final String source,
                              @Nullable final String key,
                              @Nullable final Q<?> neededType,
                              @Nullable final Object actualValue,
                              @Nullable final String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgSourceException(@Nullable final String source,
                              @Nullable final String key,
                              @Nullable final Q<?> neededType,
                              @Nullable final Object actualValue,
                              @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgSourceException(@Nullable final String source,
                              @Nullable final String key,
                              @Nullable final Q<?> neededType,
                              @Nullable final Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
