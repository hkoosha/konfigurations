package io.koosha.konfiguration;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.lang.String.format;

@Accessors(fluent = true)
@Getter
@SuppressWarnings("unused")
@ThreadSafe
public class KfgException extends RuntimeException {

    @Nullable
    private final String source;

    @Nullable
    private final String key;

    @Nullable
    private final Q<?> neededType;

    @Nullable
    private final String actualValue;

    public KfgException(@Nullable final String source,
                        @Nullable final Throwable cause) {
        super(cause);
        this.source = source;
        this.key = null;
        this.neededType = null;
        this.actualValue = null;
    }

    public KfgException(@Nullable final String source,
                        @Nullable final String message) {
        super(message);
        this.source = source;
        this.key = null;
        this.neededType = null;
        this.actualValue = null;
    }

    public KfgException(@Nullable final String source,
                        @Nullable final String message,
                        @Nullable final Throwable cause) {
        super(message, cause);
        this.source = source;
        this.key = null;
        this.neededType = null;
        this.actualValue = null;
    }

    public KfgException(@Nullable final String source,
                        @Nullable final String key,
                        @Nullable final Q<?> neededType,
                        @Nullable final Object actualValue,
                        @Nullable final String message,
                        @Nullable final Throwable cause) {
        super(message, cause);
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgException(@Nullable final String source,
                        @Nullable final String key,
                        @Nullable final Q<?> neededType,
                        @Nullable final Object actualValue,
                        @Nullable String message) {
        super(message);
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgException(@Nullable final String source,
                        @Nullable final String key,
                        @Nullable final Q<?> neededType,
                        @Nullable final Object actualValue,
                        @Nullable final Throwable cause) {
        super(cause);
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgException(@Nullable final String source,
                        @Nullable final String key,
                        @Nullable final Q<?> neededType,
                        @Nullable final Object actualValue) {
        this.source = source;
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return format("%s[key=%s,  neededType=%s, actualValue=%s]",
                this.getClass().getName(),
                this.key(),
                this.neededType(),
                this.actualValue());
    }


    public boolean hasSource() {
        return this.source() != null;
    }

    public boolean hasKey() {
        return this.key() != null;
    }

    public boolean hasNeededType() {
        return this.neededType() != null;
    }

    public boolean hasActualValue() {
        return this.actualValue() != null;
    }


    static String msgOf(final Throwable t) {
        return t == null
               ? "[null exception]->[null exception]"
               : format("[throwable::%s]->[%s]", t.getClass().getName(), t.getMessage());
    }

    static String toStringOf(final Object value) {
        String representationC;
        try {
            representationC = value == null ? "null" : value.getClass().getName();
        }
        catch (Throwable t) {
            representationC = "[" + "value.getClass().getName()" + "]->" + msgOf(t);
        }

        String representationV;
        try {
            representationV = Objects.toString(value);
        }
        catch (Throwable t) {
            representationV = "[" + "Objects.toString(value)" + "]->" + msgOf(t);
        }

        return format("[%s]:[%s]", representationC, representationV);
    }

}
