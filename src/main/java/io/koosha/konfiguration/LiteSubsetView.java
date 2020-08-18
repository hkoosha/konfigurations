package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    private final String name;

    @NotNull
    private final LiteKonfiguration wrapped;

    @NotNull
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
    @NotNull
    @Contract(pure = true,
              value = "->fail")
    public String serialize() {
        throw new KfgException(this.name, "serialize not supported on subset view.");
    }

    @Contract(pure = true)
    @Override
    public boolean isReadonly() {
        return this.isReadonly;
    }

    @Override
    @NotNull
    public LiteKonfiguration toReadonly() {
        return this.isReadonly()
            ? this
            : new LiteSubsetView(this.name, this.wrapped, this.baseKey, true);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public LiteKonfiguration toWritableCopy() {
        return new LiteSubsetView(this.name, this.wrapped.toWritableCopy(), this.baseKey, false);
    }

    private void ensureWritable() {
        if (this.isReadonly())
            throw new KfgReadonlyException(this.name, "source is readonly");
    }


    @Override
    @Nullable
    public Boolean bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.bool(key(key));
    }

    @Override
    @Nullable
    public Boolean bool(@NotNull final String key,
                        final Boolean def) {
        Objects.requireNonNull(key, "key");
        return wrapped.bool(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Boolean value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Override
    @Nullable
    public Byte byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.byte_(key(key));
    }

    @Override
    @Nullable
    public Byte byte_(@NotNull final String key,
                      final Byte def) {
        Objects.requireNonNull(key, "key");
        return wrapped.byte_(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Byte value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public Character char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.char_(key(key));
    }

    @Nullable
    @Override
    public Character char_(@NotNull final String key,
                           final Character def) {
        Objects.requireNonNull(key, "key");
        return wrapped.char_(key(key), def);
    }

    @Nullable
    @Override
    public Short short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.short_(key(key));
    }

    @Nullable
    @Override
    public Short short_(@NotNull final String key, final Short def) {
        Objects.requireNonNull(key, "key");
        return wrapped.short_(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Short value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Override
    @Nullable
    public Integer int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.int_(key(key));
    }

    @Override
    @Nullable
    public Integer int_(@NotNull final String key,
                        final Integer def) {
        Objects.requireNonNull(key, "key");
        return wrapped.int_(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Integer value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public Long long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.long_(key(key));
    }

    @Nullable
    @Override
    public Long long_(@NotNull final String key,
                      final Long def) {
        Objects.requireNonNull(key, "key");
        return wrapped.long_(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Long value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public Float float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.float_(key(key));
    }

    @Nullable
    @Override
    public Float float_(@NotNull final String key,
                        final Float def) {
        Objects.requireNonNull(key, "key");
        return wrapped.float_(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Float value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public Double double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.double_(key(key));
    }

    @Nullable
    @Override
    public Double double_(@NotNull final String key,
                          final Double def) {
        Objects.requireNonNull(key, "key");
        return wrapped.double_(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Double value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public String string(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.string(key(key));
    }

    @Nullable
    @Override
    public String string(@NotNull final String key,
                         final String def) {
        Objects.requireNonNull(key, "key");
        return wrapped.string(key(key), def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final String value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public <U> List<U> list(@NotNull final String key,
                            @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.list(key(key), type);
    }

    @Nullable
    @Override
    public <U> List<U> list(@NotNull final String key,
                            @NotNull final Kind<U> type,
                            final List<U> def) {
        Objects.requireNonNull(key, "key");
        return wrapped.list(key(key), type, def);
    }

    @NotNull
    @Override
    public LiteKonfiguration put(@NotNull final String key,
                                 final List<?> value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public <U> Set<U> set(@NotNull final String key,
                          @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.set(key(key), type);
    }

    @Nullable
    @Override
    public <U> Set<U> set(@NotNull final String key,
                          @NotNull final Kind<U> type,
                          final Set<U> def) {
        Objects.requireNonNull(key, "key");
        return wrapped.set(key(key), type, def);
    }

    @Override
    @NotNull
    public LiteKonfiguration put(@NotNull final String key,
                                 final Set<?> value) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.put(key(key), value);
        return this;
    }

    @Nullable
    @Override
    public <U> U custom(@NotNull final String key,
                        @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.custom(key(key), type);
    }

    @Nullable
    @Override
    public <U> U custom(@NotNull final String key,
                        @NotNull final Kind<U> type,
                        @Nullable final U def) {
        Objects.requireNonNull(key, "key");
        return wrapped.custom(key(key), type, def);
    }

    @NotNull
    @Override
    public LiteKonfiguration delete(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        this.ensureWritable();
        this.wrapped.delete(key(key));
        return this;
    }

    @Override
    public @NotNull LiteKonfiguration putCustom(@NotNull final String key,
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
