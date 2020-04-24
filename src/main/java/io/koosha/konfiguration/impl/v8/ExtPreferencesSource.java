package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.*;
import io.koosha.konfiguration.ext.KfgPreferencesError;
import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
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
final class ExtPreferencesSource extends Source {

    private final Deserializer deser;
    private final Preferences source;
    private final int lastHash;

    @NotNull
    private final String name;

    ExtPreferencesSource(@NotNull final String name,
                         @NotNull final Preferences preferences,
                         @Nullable final Deserializer deserializer) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(preferences, "preferences");

        this.name = name;
        this.source = preferences;
        this.deser = deserializer;
        this.lastHash = hashOf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object bool0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.source.getBoolean(sane(key), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object char0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        final String s = ((String) this.string0(sane(key)));
        if (s.length() != 1)
            throw new KfgTypeException(this.name(), key, Kind.CHAR, s);
        return ((String) this.string0(sane(key))).charAt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object string0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.source.get(sane(key), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Number number0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.source.getLong(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Number numberDouble0(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.source.getDouble(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected List<?> list0(@NotNull final String key,
                            @NotNull final Kind<? extends List<?>> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.deserialize(this.source.getByteArray(sane(key), new byte[0]), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Set<?> set0(@NotNull final String key,
                          @NotNull final Kind<? extends Set<?>> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.deserialize(this.source.getByteArray(sane(key), new byte[0]), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Map<?, ?> map0(@NotNull final String key,
                             @NotNull final Kind<? extends Map<?, ?>> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.deserialize(this.source.getByteArray(sane(key), new byte[0]), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object custom0(@NotNull final String key,
                             @NotNull final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");

        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.deserialize(this.source.getByteArray(sane(key), new byte[0]), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isNull(@NotNull final String key) {
        Objects.requireNonNull(key, "key");
        return this.source.get(sane(key), null) == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull final String key,
                       @Nullable final Kind<?> type) {
        Objects.requireNonNull(key, "key");
        try {
            return source.nodeExists(sane(key));
        }
        catch (Throwable e) {
            throw new KfgSourceException(this.name(), key, null, null, "error checking existence of key", e);
        }
    }

    private String sane(@NotNull final String key) {
        Objects.requireNonNull(key, "key");

        try {
            if (!this.source.nodeExists(key))
                throw new KfgIllegalStateException(this.name(), "missing key=" + key);
        }
        catch (final BackingStoreException e) {
            throw new KfgIllegalStateException(this.name(), "backing store error for key=" + key, e);
        }

        return key.replace('.', '/');
    }

    private int hashOf() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            this.source.exportSubtree(buffer);
        }
        catch (IOException | BackingStoreException e) {
            throw new KfgSourceException(this.name(), "could not calculate hash of the java.util.prefs.Preferences source", e);
        }
        return Arrays.hashCode(buffer.toByteArray());
    }


    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean hasUpdate() {
        return lastHash != hashOf();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Source updatedCopy() {
        return ExtPreferencesSource.this;
    }

}
