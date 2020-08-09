package io.koosha.konfiguration.ext;

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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

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
public final class ExtPreferencesSource extends Source {

    private final Preferences source;
    private final int lastHash;

    @NotNull
    private final String name;

    private final Object LOCK = new Object();

    public ExtPreferencesSource(@NotNull final String name,
                                @NotNull final Preferences preferences) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(preferences, "preferences");

        this.name = name;
        this.source = preferences;
        this.lastHash = hashOf();
    }

    @Override
    @NotNull
    protected Object bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            return this.source.getBoolean(sane(key), false);
        }
    }

    @Override
    @NotNull
    protected Object char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            final String s = ((String) this.string0(sane(key)));
            if (s.length() != 1)
                throw new KfgTypeException(this.name(), key, Kind.CHAR, s);
            return ((String) this.string0(sane(key))).charAt(0);
        }
    }

    @Override
    @NotNull
    protected Object string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            return this.source.get(sane(key), null);
        }
    }

    @Override
    @NotNull
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            return this.source.getLong(sane(key), 0);
        }
    }

    @Override
    @NotNull
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            return this.source.getDouble(sane(key), 0);
        }
    }

    @Override
    @NotNull
    protected List<?> list0(@NotNull final String key,
                            @NotNull final Kind<?> type) {
        throw new KfgAssertionException(this.name, key, type, null, "operation not supported on this source");
    }

    @Override
    @NotNull
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<?> type) {
        throw new KfgAssertionException(this.name, key, type, null, "operation not supported on this source");
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        throw new KfgAssertionException(this.name, key, type, null, "operation not supported on this source");
    }

    @Override
    protected boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        synchronized (LOCK) {
            return this.source.get(sane(key), null) == null
                && this.source.get(sane(key), "") == null;
        }
    }

    @Override
    public boolean has(@NotNull final String key,
                       @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

            try {
                synchronized (LOCK) {
                    if (!source.nodeExists(sane(key)))
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
        }
        catch (final KfgTypeException k) {
            return false;
        }
        return false;
    }

    private String sane(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        try {
            synchronized (LOCK) {
                if (!this.source.nodeExists(key))
                    throw new KfgIllegalStateException(this.name(), "missing key=" + key);
            }
        }
        catch (final BackingStoreException e) {
            throw new KfgIllegalStateException(this.name(), "backing store error for key=" + key, e);
        }

        return key.replace('.', '/');
    }

    private int hashOf() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            synchronized (LOCK) {
                this.source.exportSubtree(buffer);
            }
        }
        catch (final IOException | BackingStoreException e) {
            throw new KfgSourceException(this.name(), "could not calculate hash of the java.util.prefs.Preferences source", e);
        }
        return Arrays.hashCode(buffer.toByteArray());
    }

    @NotNull
    @Override
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
    public Source updatedCopy() {
        return ExtPreferencesSource.this;
    }

}
