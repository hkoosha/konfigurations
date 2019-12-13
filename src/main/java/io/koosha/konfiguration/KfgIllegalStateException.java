package io.koosha.konfiguration;

import lombok.Getter;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.koosha.konfiguration.KfgException.toStringOf;

@Getter
@ThreadSafe
public class KfgIllegalStateException extends IllegalStateException {

    @Nullable
    private final String source;

    @Nullable
    private final String key;

    @Nullable
    private final Q<?> neededType;

    @Nullable
    private final String actualValue;

    public KfgIllegalStateException(@Nullable final String source,
                                    final String message) {
        this(source, null, null, null, message);
    }

    public KfgIllegalStateException(@Nullable final String source,
                                    final String message,
                                    final Throwable t) {
        this(source, null, null, null, message, t);
    }


    public KfgIllegalStateException(@Nullable final String source,
                                    @Nullable final String key,
                                    @Nullable final Q<?> neededType,
                                    @Nullable final Object actualValue,
                                    final String message) {
        super(message);
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgIllegalStateException(@Nullable final String source,
                                    @Nullable final String key,
                                    @Nullable final Q<?> neededType,
                                    @Nullable final Object actualValue,
                                    final String message,
                                    final Throwable cause) {
        super(message, cause);
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgIllegalStateException(@NotNull final String source,
                                    @Nullable final String key,
                                    @Nullable final Q<?> neededType,
                                    @Nullable final Object actualValue) {
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

}
