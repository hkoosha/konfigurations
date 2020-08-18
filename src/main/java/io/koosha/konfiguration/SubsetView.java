package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public final class SubsetView implements Konfiguration {

    private final String name;
    private final Konfiguration wrapped;
    private final String baseKey;

    public SubsetView(@NotNull final String name,
                      @NotNull final Konfiguration wrappedKonfiguration,
                      @NotNull final String baseKey) {
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
    }


    @NotNull
    @Override
    public String name() {
        return this.name;
    }


    @Contract(pure = true)
    @Override
    @NotNull
    public K<Boolean> bool(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.bool(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<Byte> byte_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.byte_(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<Character> char_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.char_(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<Short> short_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.short_(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<Integer> int_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.int_(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<Long> long_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.long_(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<Float> float_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.float_(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<Double> double_(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.double_(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public K<String> string(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return wrapped.string(key(key));
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public <U> K<List<U>> list(@NotNull final String key,
                               @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.list(key(key), type);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public <U> K<Set<U>> set(@NotNull final String key,
                             @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.set(key(key), type);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public <U> K<U> custom(@NotNull final String key,
                           @NotNull final Kind<U> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.custom(key(key), type);
    }

    @Contract(pure = true)
    @Override
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        return wrapped.has(key(key), type);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public Handle registerSoft(@NotNull final KeyObserver observer,
                               @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");
        return this.wrapped.registerSoft(observer, key(key));
    }

    @Contract(pure = true)
    @Override
    public void deregister(@NotNull final Handle observer,
                           @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");
        this.wrapped.deregister(observer, key(key));
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public Handle register(@NotNull final KeyObserver observer,
                           @NotNull final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");
        return this.wrapped.register(observer, key(key));
    }

    @Contract(pure = true,
              value = "_ -> _")
    @NotNull
    @Override
    public Konfiguration subset(@NotNull final String key) {
        // TODO optimise if this i.wrapped instance of LiteSubsetView.
        Objects.requireNonNull(key, "key");
        return key.isEmpty()
            ? this
            : new SubsetView(
            this.name.split("::")[0] + "::" + key,
            this.wrapped,
            this.baseKey + this.key(key)
        );
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public Optional<KonfigurationManager> manager() {
        return Optional.empty();
    }

    @Contract(pure = true,
              value = "_ -> _")
    @NotNull
    private String key(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        if (Objects.equals(key, KeyObserver.LISTEN_TO_ALL))
            return key;

        if (key.startsWith("."))
            throw new KfgIllegalArgumentException(this.name(), "key must not start with a dot: " + key);

        return this.baseKey + key;
    }

}
