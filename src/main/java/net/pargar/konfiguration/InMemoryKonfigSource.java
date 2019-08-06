/*
 * Copyright (C) 2019 Koosha Hosseiny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.pargar.konfiguration;


import java.util.*;
import java.util.function.Supplier;

import static net.pargar.konfiguration.TypeName.*;


/**
 * Reads konfig from a plain java map.
 *
 * <p>To fulfill contract of {@link KonfigSource}, all the values put in the
 * map of konfiguration key/values supplied to the konfiguration, should be
 * immutable.
 *
 * <p>Thread safe and immutable.
 */
public final class InMemoryKonfigSource implements KonfigSource {

    private final Map<String, Object> storage;
    private final Supplier<Map<String, Object>> storageProvider;

    private KonfigurationTypeException checkType(final String required, final String key) {
        return new KonfigurationTypeException(required, this.storage.get(key).getClass().toString(), key);
    }

    private KonfigurationTypeException checkType(final TypeName required, final String key) {
        return this.checkType(required.getTName(), key);
    }

    /**
     * Important: {@link Supplier#get()} might be called multiple
     * times in a short period (once call to see if it's changed and if so, one
     * mode call to get the new values afterward.
     *
     * @param storage
     *         konfig source.
     *
     * @throws NullPointerException
     *         if provided storage provider is null
     * @throws KonfigurationSourceException
     *         if the provided storage by provider is null
     */
    @SuppressWarnings("WeakerAccess")
    public InMemoryKonfigSource(final Supplier<Map<String, Object>> storage) {
        if (storage == null)
            throw new NullPointerException("storage");
        this.storageProvider = storage;

        final Map<String, Object> newStorage = this.storageProvider.get();
        if (newStorage == null)
            throw new KonfigurationSourceException("storage is null");

        this.storage = new HashMap<>(newStorage);
    }

    /**
     * Wraps the provided storage in a {@link Supplier} and calls
     * {@link #InMemoryKonfigSource(Supplier)}
     *
     * @param storage
     *         konfig source.
     *
     * @throws NullPointerException
     *         if storage is null.
     */
    @SuppressWarnings("unused")
    public InMemoryKonfigSource(final Map<String, Object> storage) {
        this(new Supplier<Map<String, Object>>() {
            private final Map<String, Object> s = new HashMap<>(Objects.requireNonNull(storage));

            @Override
            public Map<String, Object> get() {
                return s;
            }
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean bool(final String key) {
        try {
            return (Boolean) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(BOOLEAN, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer int_(final String key) {
        try {
            return (Integer) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(INT, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long long_(final String key) {
        try {
            return (Long) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(LONG, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double double_(final String key) {
        try {
            return (Double) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(DOUBLE, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String string(final String key) {
        try {
            return (String) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(STRING, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> list(final String key, final Class<T> type) {
        // TODO check type

        try {
            return (List<T>) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(LIST, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> map(final String key, final Class<T> type) {
        // TODO check type

        try {
            return (Map<String, T>) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(MAP, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> set(final String key, final Class<T> type) {
        // TODO check type

        try {
            return (Set<T>) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            try {
                final List<T> l = (List<T>) this.storage.get(key);
                final HashSet<T> s = new HashSet<>(l);
                return Collections.unmodifiableSet(s);
            }
            catch (final ClassCastException cceList) {
                throw checkType(SET, key);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T custom(final String key, final Class<T> type) {
        // TODO check type
        try {
            return (T) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(type.toString(), key);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String key) {
        return this.storage.containsKey(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdatable() {
        final Map<String, Object> newStorage = this.storageProvider.get();
        return newStorage != null && !this.storage.equals(newStorage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KonfigSource copyAndUpdate() {
        return new InMemoryKonfigSource(this.storageProvider);
    }

}
