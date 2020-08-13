package io.koosha.konfiguration;

import org.jetbrains.annotations.Nullable;

public class KfgReadonlyException extends KfgException {

    public KfgReadonlyException(@Nullable final String source,
                                @Nullable final String message) {
        super(source, message);
    }

}
