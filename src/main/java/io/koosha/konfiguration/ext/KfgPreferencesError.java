package io.koosha.konfiguration.ext;

import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Nullable;

public class KfgPreferencesError extends KfgSourceException {

    public KfgPreferencesError(@Nullable final String source,
                               @Nullable final String message) {
        super(source, message);
    }

    public KfgPreferencesError(@Nullable final String source,
                               @Nullable final String message,
                               @Nullable final Throwable cause) {
        super(source, message, cause);
    }

    public KfgPreferencesError(@Nullable final String source,
                               @Nullable final String key,
                               @Nullable final Kind<?> neededType,
                               @Nullable final Object actualValue,
                               @Nullable final String message,
                               @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgPreferencesError(@Nullable final String source,
                               @Nullable final String key,
                               @Nullable final Kind<?> neededType,
                               @Nullable final Object actualValue,
                               @Nullable final String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgPreferencesError(@Nullable final String source,
                               @Nullable final String key,
                               @Nullable final Kind<?> neededType,
                               @Nullable final Object actualValue,
                               @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgPreferencesError(@Nullable final String source,
                               @Nullable final String key,
                               @Nullable final Kind<?> neededType,
                               @Nullable final Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
