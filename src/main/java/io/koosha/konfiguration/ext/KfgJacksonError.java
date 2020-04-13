package io.koosha.konfiguration.ext;

import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.Typer;
import org.jetbrains.annotations.Nullable;

public final class KfgJacksonError extends KfgSourceException {

    public KfgJacksonError(@Nullable final String source,
                           @Nullable final String message) {
        super(source, message);
    }

    public KfgJacksonError(@Nullable final String source,
                           @Nullable final String message,
                           @Nullable final Throwable cause) {
        super(source, message, cause);
    }

    public KfgJacksonError(@Nullable final String source,
                           @Nullable final String key,
                           @Nullable final Typer<?> neededType,
                           @Nullable final Object actualValue,
                           @Nullable final String message,
                           @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgJacksonError(@Nullable final String source,
                           @Nullable final String key,
                           @Nullable final Typer<?> neededType,
                           @Nullable final Object actualValue,
                           @Nullable final String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgJacksonError(@Nullable final String source,
                           @Nullable final String key,
                           @Nullable final Typer<?> neededType,
                           @Nullable final Object actualValue,
                           @Nullable final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgJacksonError(@Nullable final String source,
                           @Nullable final String key,
                           @Nullable final Typer<?> neededType,
                           @Nullable final Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
