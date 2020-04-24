package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.koosha.konfiguration.KfgException.toStringOf;

public class KfgIllegalStateException extends IllegalStateException {

    @Nullable
    private final String source;

    @Nullable
    private final String key;

    @Nullable
    private final Kind<?> neededType;

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
                                    @Nullable final Kind<?> neededType,
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
                                    @Nullable final Kind<?> neededType,
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
                                    @Nullable final Kind<?> neededType,
                                    @Nullable final Object actualValue) {
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    @Nullable
    public String getSource() {
        return this.source;
    }

    @Nullable
    public String getKey() {
        return this.key;
    }

    @Nullable
    public Kind<?> getNeededType() {
        return this.neededType;
    }

    @Nullable
    public String getActualValue() {
        return this.actualValue;
    }

}
