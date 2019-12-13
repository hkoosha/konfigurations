package io.koosha.konfiguration;

import lombok.Getter;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@Getter
@ThreadSafe
public class KfgIllegalArgumentException extends IllegalArgumentException {

    @Nullable
    private final String source;

    public KfgIllegalArgumentException(@Nullable final String source,
                                       final String message) {
        super(message);
        this.source = source;
    }

}
