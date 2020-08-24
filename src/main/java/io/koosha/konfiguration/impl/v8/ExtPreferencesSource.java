package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads konfig from a {@link Preferences} source.
 *
 * <p>for {@link #custom(String, Kind)} to work, the supplied deserializer
 * must be configured to handle arbitrary types accordingly.
 *
 * <p><b>IMPORTANT</b> Does not coup too well with keys being added / removed
 * from backing source. Only changes are supported (as stated in
 * {@link Preferences#addNodeChangeListener(NodeChangeListener)})
 *
 * <p>Thread safe and immutable.
 *
 * <p>For now, pref change listener is not used
 */
@ApiStatus.Internal
@ThreadSafe
final class ExtPreferencesSource extends Source {

    private final Pattern LIST_SPLITTER = Pattern.compile(",");

    private final Preferences source;
    private final int lastHash;

    @NotNull
    private final String name;

    private final Object LOCK = new Object();

    ExtPreferencesSource(@NotNull final String name,
                         @NotNull final Preferences preferences) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(preferences, "preferences");

        this.name = name;
        this.source = preferences;
        this.lastHash = hashOf();
    }

    @Override
    @NotNull
    @Contract(pure = true)
    protected Object bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final String sane = sane(key);

        synchronized (LOCK) {
            return this.source.getBoolean(sane, false);
        }
    }

    @Override
    @NotNull
    @Contract(pure = true)
    protected Object char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final String sane = sane(key);

        final String s;
        synchronized (LOCK) {
            s = ((String) this.string0(sane));
        }
        if (s.length() != 1)
            throw new KfgTypeException(this.name(), key, Kind.CHAR, s);

        return s.charAt(0);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    protected Object string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final String sane = sane(key);

        synchronized (LOCK) {
            return this.source.get(sane, null);
        }
    }

    @Override
    @NotNull
    @Contract(pure = true)
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final String sane = sane(key);

        synchronized (LOCK) {
            return this.source.getLong(sane, 0);
        }
    }

    @Override
    @NotNull
    @Contract(pure = true)
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final String sane = sane(key);

        synchronized (LOCK) {
            return this.source.getDouble(sane, 0);
        }
    }

    @Override
    @NotNull
    @Contract(pure = true,
              value = "_,_->new")
    protected List<?> list0(@NotNull final String key,
                            @NotNull final Kind<?> type) {
        final String value = this.source.get(key, "");
        if (value.isEmpty())
            return Collections.emptyList();

        final String[] split = LIST_SPLITTER.split(value);
        if (type.isString())
            return Collections.unmodifiableList(Arrays.asList(split));

        final Stream<String> stream = Stream.of(split);

        final Stream<?> values;
        if (type.isLong())
            values = stream.map(Long::parseLong);
        else if (type.isInt())
            values = stream.map(Integer::parseInt);
        else if (type.isByte())
            values = stream.map(Byte::parseByte);
        else if (type.isChar())
            values = stream
                .peek(it -> {
                    if (it.length() > 1)
                        throw new KfgTypeException(this.name, key, null, it, "expecting character");
                })
                .map(it -> it.charAt(0));
        else
            throw new KfgTypeException(this.name, key, type, null, "unsupported list type");

        return Collections.unmodifiableList(values.collect(Collectors.toList()));
    }

    @Override
    @NotNull
    @Contract(pure = true)
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<?> type) {
        return listToSet(key, type);
    }

    @Override
    @NotNull
    @Contract(pure = true,
              value = "_,_->fail")
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        throw new KfgAssertionException(this.name, key, type, null,
            "operation not supported on this source");
    }

    @Override
    @Contract(pure = true)
    protected boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final String sane = sane(key);

        synchronized (LOCK) {
            return this.source.get(sane, null) == null
                && this.source.get(sane, "") == null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Contract(pure = true)
    @Override
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        final String sane = sane(key);

        try {
            synchronized (LOCK) {
                if (!source.nodeExists(sane))
                    return false;
            }
        }
        catch (Throwable e) {
            throw new KfgSourceException(this.name(), key, null, null, "error checking existence of key", e);
        }

        try {
            if (type.isChar()) {
                this.char0(key);
                return true;
            }
            if (type.isBool()) {
                this.bool0(key);
                return true;
            }
            if (type.isShort()) {
                return ((long) this.number0(key).shortValue()) == this.number0(key).longValue();
            }
            if (type.isInt()) {
                return ((long) this.number0(key).intValue()) == this.number0(key).longValue();
            }
            if (type.isLong()) {
                this.number0(key).longValue();
                return true;
            }
            if (type.isFloat()) {
                // Shaky
                return this.numberDouble0(key).doubleValue() >= Float.MIN_VALUE
                    && this.numberDouble0(key).doubleValue() <= Float.MAX_VALUE;
            }
            if (type.isDouble()) {
                this.numberDouble0(key);
                return true;
            }
            if (type.isList()) {
                this.list0(key, type.getCollectionContainedKind());
                return true;
            }
            if (type.isSet()) {
                this.set(key, type.getCollectionContainedKind());
                return true;
            }
        }
        catch (final KfgTypeException k) {
            return false;
        }

        return false;
    }

    @Contract(pure = true)
    private String sane(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        final String newKey = key.replace('.', '/');

        try {
            synchronized (LOCK) {
                if (!this.source.nodeExists(newKey))
                    throw new KfgIllegalStateException(this.name(), "missing key=" + key);
            }
        }
        catch (final BackingStoreException e) {
            throw new KfgIllegalStateException(this.name(), "backing store error for key=" + key, e);
        }

        return newKey;
    }

    @Contract(pure = true)
    private int hashOf() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            synchronized (LOCK) {
                this.source.exportSubtree(buffer);
            }
        }
        catch (final IOException | BackingStoreException e) {
            throw new KfgSourceException(this.name(),
                "could not calculate hash of the java.util.prefs.Preferences source", e);
        }
        return Arrays.hashCode(buffer.toByteArray());
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public String name() {
        return this.name;
    }

    @Override
    @Contract(pure = true)
    public boolean hasUpdate() {
        return lastHash != hashOf();
    }

    @NotNull
    @Override
    @Contract(pure = true,
              value = "->this")
    public Source updatedCopy() {
        return new ExtPreferencesSource(this.name(), this.source);
    }

}
