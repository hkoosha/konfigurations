package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
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
                                  @Nullable final Q<?> type) {
        super(source, key, type, null);
    }

}
