package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Nullable;

public class KfgMissingKeyException extends KfgException {

    public KfgMissingKeyException(@Nullable final String source,
                                  @Nullable final String key) {
        super(source, key, null, null);
    }

    public KfgMissingKeyException(@Nullable final String source,
                                  @Nullable final String key,
                                  @Nullable final String message) {
        super(source, key, null, null, message);
    }

    public KfgMissingKeyException(@Nullable final String source,
                                  @Nullable final String key,
                                  @Nullable final Kind<?> type) {
        super(source, key, type, null);
    }

    @Override
    public String toString() {
        return String.format("%s(key=%s, neededType=%s)",
            this.getClass().getName(),
            this.key(),
            this.neededType());
    }

}
