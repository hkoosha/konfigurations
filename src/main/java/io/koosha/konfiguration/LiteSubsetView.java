package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Read only subset view of a konfiguration. Prepends a pre-defined key
 * to all konfig values
 * <p>
 * Ignore the J prefix.
 *
 * <p>Immutable and thread safe by itself, although the underlying wrapped
 * konfiguration's thread safety is not guarantied.
 */
@ThreadSafe
public final class LiteSubsetView implements LiteKonfiguration {

    private final String name;
    private final LiteKonfiguration wrapped;
    private final String baseKey;
    private final boolean isReadonly;

    public LiteSubsetView(@NotNull final String name,
                          @NotNull final LiteKonfiguration wrappedKonfiguration,
                          @NotNull final String baseKey,
                          final boolean isReadonly) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(wrappedKonfiguration, "wrappedKonfiguration");
        Objects.requireNonNull(baseKey, "baseKey");
        this.name = name;
        this.wrapped = wrappedKonfiguration;

        if (baseKey.startsWith(".")) // covers baseKey == "." too.
            throw new KfgIllegalArgumentException(this.name(), "key must not start with a dot: " + baseKey);
        if (baseKey.contains(".."))
            throw new KfgIllegalArgumentException(this.name(), "key can not contain subsequent dots: " + baseKey);

        if (baseKey.isEmpty())
            this.baseKey = "";
        else if (baseKey.endsWith("."))
            this.baseKey = baseKey;
        else
            this.baseKey = baseKey + ".";

        this.isReadonly = isReadonly;
    }

    @Override
    @Contract(pure = true)
    @NotNull
    public String name() {
        return this.name;
    }

    @Override
    public String serialize() {
        throw new KfgException(this.name, "serialize not supported on subset view.");
    }

    @Override
    public boolean isReadonly() {
        return this.isReadonly;
    }

    @Override
    public LiteKonfiguration toReadonly() {
        return this.isReadonly()
            ? this
            : new LiteSubsetView(this.name, this.wrapped, this.baseKey, true);
    }

    @Override
    public LiteKonfiguration toWritableCopy() {
        return new LiteSubsetView(this.name, this.wrapped.toWritableCopy(), this.baseKey, false);
    }

    private void ensureWritable() {
        if (this.isReadonly())
            throw new KfgReadonlyException(this.name, "source is readonly");
    }


    @Contract(pure = true)
    @Override
    public Boolean bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.bool(key(key));
    }

    @Override
    public Boolean bool(@NotNull final String key,
                        final Boolean def) {
        Objects.requireNonNull(key, "key");
        return wrapped.bool(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Boolean value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @Override
    public Byte byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.byte_(key(key));
    }

    @Override
    public Byte byte_(@NotNull final String key,
                      final Byte def) {
        Objects.requireNonNull(key, "key");
        return wrapped.byte_(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Byte value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @Override
    public Character char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.char_(key(key));
    }

    @Override
    public Character char_(@NotNull final String key,
                           final Character def) {
        Objects.requireNonNull(key, "key");
        return wrapped.char_(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Character value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @Override
    public Short short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.short_(key(key));
    }

    @Override
    public Short short_(@NotNull final String key, final Short def) {
        Objects.requireNonNull(key, "key");
        return wrapped.short_(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Short value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @Override
    public Integer int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.int_(key(key));
    }

    @Override
    public Integer int_(@NotNull final String key,
                        final Integer def) {
        Objects.requireNonNull(key, "key");
        return wrapped.int_(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Integer value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public Long long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.long_(key(key));
    }

    @Override
    public Long long_(@NotNull final String key,
                      final Long def) {
        Objects.requireNonNull(key, "key");
        return wrapped.long_(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Long value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public Float float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.float_(key(key));
    }

    @Override
    public Float float_(@NotNull final String key,
                        final Float def) {
        Objects.requireNonNull(key, "key");
        return wrapped.float_(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Float value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public Double double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.double_(key(key));
    }

    @Override
    public Double double_(@NotNull final String key,
                          final Double def) {
        Objects.requireNonNull(key, "key");
        return wrapped.double_(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Double value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String string(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.string(key(key));
    }

    @Override
    public String string(@NotNull final String key,
                         final String def) {
        Objects.requireNonNull(key, "key");
        return wrapped.string(key(key), def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final String value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public <U> List<U> list(@NotNull final String key,
                            @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.list(key(key), type);
    }

    @Override
    public <U> List<U> list(@NotNull final String key,
                            @NotNull final Kind<U> type,
                            final List<U> def) {
        Objects.requireNonNull(key, "key");
        return wrapped.list(key(key), type, def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final List<?> value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public <U> Set<U> set(@NotNull final String key,
                          @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.set(key(key), type);
    }

    @Override
    public <U> Set<U> set(@NotNull final String key,
                          @NotNull final Kind<U> type,
                          final Set<U> def) {
        Objects.requireNonNull(key, "key");
        return wrapped.set(key(key), type, def);
    }

    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final Set<?> value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public <U> U custom(@NotNull final String key,
                        @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.custom(key(key), type);
    }

    @Override
    public <U> U custom(@NotNull final String key,
                        @NotNull final Kind<U> type,
                        final U def) {
        Objects.requireNonNull(key, "key");
        return wrapped.custom(key(key), type, def);
    }

    @Override
    public LiteKonfiguration putCustom(@NotNull final String key,
                                       final Object value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.putCustom(key(key), value);
        return this;
    }

    @Contract(pure = true)
    @Override
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.has(key(key), type);
    }

    @Contract(pure = true,
              value = "_ -> _")
    @NotNull
    @Override
    public LiteKonfiguration subset(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return key.isEmpty()
            ? this
            : new LiteSubsetView(
            this.name.split("::")[0] + "::" + key,
            this.wrapped,
            this.baseKey + this.key(key),
            this.isReadonly
        );
    }

    @Contract(pure = true,
              value = "_ -> _")
    @NotNull
    private String key(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        if (key.startsWith("."))
            throw new KfgIllegalArgumentException(this.name(), "key must not start with a dot: " + key);

        return this.baseKey + key;
    }

}
