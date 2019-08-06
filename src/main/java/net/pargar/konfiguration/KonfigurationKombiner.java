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


/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 *
 * <p>Clients may safely use any method, BUT the {@link #update()} must be
 * called from a single thread only. Although calling it still does NOT
 * invalidate thread safety of clients.
 *
 * <p>In order to stop clients from calling {@link #update()}, call
 * {@link #readonly()} and obtain a readonly view of the konfiguration.
 *
 * <p>Pitfall: The first call for a key, defines the expected type of that key.
 * For instance calling string("even_though_is_int"), will make
 * KonfigurationKombiner to always return an string typed key, while it actually
 * is an int.
 */
public final class KonfigurationKombiner implements Konfiguration {

    private final KonfigurationKombinerHelper kh;

    /**
     * @param sources
     *         sources to combine in one konfiguration.
     *
     * @throws NullPointerException
     *         if sources is null.
     */
    public KonfigurationKombiner(final KonfigSource... sources) {
        this(Arrays.asList(Objects.requireNonNull(sources)));
    }

    /**
     * @param sources
     *         sources to combine.
     *
     * @throws NullPointerException
     *         if sources is null.
     * @throws IllegalArgumentException
     *         is sources is empty.
     */
    @SuppressWarnings("WeakerAccess")
    public KonfigurationKombiner(final Collection<KonfigSource> sources) {
        final List<KonfigSource> sources_ = new ArrayList<>(Objects.requireNonNull(sources));

        if (sources_.isEmpty())
            throw new IllegalArgumentException("no source given");

        kh = new KonfigurationKombinerHelper(sources_);
    }

    @SuppressWarnings({"unused",
                       "WeakerAccess"
                      })
    public Konfiguration readonly() {
        return new KonfigurationSubsetView(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Boolean> bool(final String key) {
        return kh.getWrappedValue(key, Boolean.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Integer> int_(final String key) {
        return kh.getWrappedValue(key, Integer.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Long> long_(final String key) {
        return kh.getWrappedValue(key, Long.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Double> double_(final String key) {
        return kh.getWrappedValue(key, Double.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<String> string(final String key) {
        return kh.getWrappedValue(key, String.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> K<List<T>> list(final String key, final Class<T> type) {
        return kh.getWrappedValue(key, List.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> K<Map<String, T>> map(final String key, final Class<T> type) {
        return kh.getWrappedValue(key, Map.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> K<Set<T>> set(final String key, final Class<T> type) {
        return kh.getWrappedValue(key, Set.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <T> K<T> custom(final String key, final Class<T> type) {
        Objects.requireNonNull(type);

        if (Boolean.class.equals(type))
            return (K<T>) this.bool(key);

        if (Integer.class.equals(type))
            return (K<T>) this.int_(key);

        if (Long.class.equals(type))
            return (K<T>) this.long_(key);

        if (Double.class.equals(type))
            return (K<T>) this.double_(key);

        if (String.class.equals(type))
            return (K<T>) this.string(key);

        if (type.isAssignableFrom(Map.class) || type.isAssignableFrom(Set.class) || type.isAssignableFrom(List.class))
            throw new KonfigurationTypeException("for collection types, use corresponding methods");

        return kh.getWrappedValue(key, null, type);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final Konfiguration subset(final String key) {
        Objects.requireNonNull(key, "key");
        if (key.startsWith("."))
            throw new IllegalArgumentException("key must not start with a dot: " + key);
        return new KonfigurationSubsetView(this, key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final Konfiguration register(final KeyObserver observer) {
        kh.register(observer, "");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Konfiguration deregister(final KeyObserver observer) {
        kh.deregister(observer, "");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update() {
        return kh.update();
    }

    /**
     * Read only subset view of a konfiguration. Prepends a pre-defined key
     * to all konfig values
     *
     * <p>Immutable and thread safe by itself, although the underlying wrapped
     * konfiguration's thread safety is not guarantied.
     */
    private static final class KonfigurationSubsetView implements Konfiguration {

        private final Konfiguration wrapped;
        private final String baseKey;

        KonfigurationSubsetView(final Konfiguration wrapped, final String baseKey) {
            Objects.requireNonNull(baseKey, "baseKey");
            this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
            this.baseKey = baseKey.endsWith(".") ? baseKey : baseKey + ".";
        }

        KonfigurationSubsetView(final Konfiguration wrapped) {
            this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
            this.baseKey = "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Boolean> bool(final String key) {
            return wrapped.bool(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Integer> int_(final String key) {
            return wrapped.int_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Long> long_(final String key) {
            return wrapped.long_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Double> double_(final String key) {
            return wrapped.double_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<String> string(final String key) {
            return wrapped.string(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> K<List<T>> list(final String key, final Class<T> type) {
            return wrapped.list(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> K<Map<String, T>> map(final String key, final Class<T> type) {
            return wrapped.map(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> K<Set<T>> set(final String key, final Class<T> type) {
            return wrapped.set(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> K<T> custom(final String key, final Class<T> type) {
            return wrapped.custom(baseKey + key, type);
        }


        /**
         * Does not return false or true, as clients might wait for this method
         * it in a while loop and wait for it to return true, indicating a
         * change being applied. Simply fails fast.
         *
         * @throws KonfigurationException
         *         always.
         */
        @Override
        public boolean update() {
            throw new KonfigurationException("update is not available from subset view");
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration subset(final String key) {
            Objects.requireNonNull(key, "key");
            final String newKey = this.baseKey + "." + key;
            return new KonfigurationSubsetView(this.wrapped, newKey);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration register(final KeyObserver observer) {
            return this.wrapped.register(observer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration deregister(final KeyObserver observer) {
            return this.wrapped.deregister(observer);
        }

    }

}
