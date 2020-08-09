package io.koosha.konfiguration;

import org.jetbrains.annotations.Nullable;

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

}
