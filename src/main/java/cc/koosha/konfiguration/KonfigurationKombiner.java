package cc.koosha.konfiguration;

import lombok.NonNull;
import lombok.val;

import java.util.*;


/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 * <p>
 * Clients may safely use any method, BUT the {@link #update()} must be
 * called from a single thread only. Although calling it still does NOT
 * invalidate thread safety of clients.
 * <p>
 * In order to stop clients from calling {@link #update()}, call
 * {@link #readonly()} and obtain a readonly view of the konfiguration.
 * <p>
 * Pitfall: The first call for a key, defines the expected type of that key.
 * For instance calling string("even_though_is_int"), will make KonfigurationKombiner
 * to always return an string typed key, while it actually is an int.
 */
public final class KonfigurationKombiner implements Konfiguration {

    private final KonfigurationKombinerHelper kh;

    public KonfigurationKombiner(@NonNull final KonfigSource... sources) {
        this(Arrays.asList(sources));
    }

    public KonfigurationKombiner(@NonNull final Collection<KonfigSource> sources) {
        val sources_ = new ArrayList<KonfigSource>(sources);

        if (sources_.isEmpty())
            throw new IllegalArgumentException("no source given");

        kh = new KonfigurationKombinerHelper(sources_);
    }

    @SuppressWarnings("unused")
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
    public final <T> K<T> custom(final String key, @NonNull final Class<T> type) {
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

        if (type.isAssignableFrom(Map.class)
                || type.isAssignableFrom(Set.class)
                || type.isAssignableFrom(List.class))
            throw new KonfigurationException("for collection types, use corresponding methods");

        return kh.getWrappedValue(key, null, type);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final Konfiguration subset(@NonNull final String key) {
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
     * <p>
     * Immutable and thread safe by itself, although the underlying wrapped
     * konfiguration's thread safety is not guarantied.
     */
    private static final class KonfigurationSubsetView implements Konfiguration {

        private final Konfiguration wrapped;
        private final String baseKey;

        KonfigurationSubsetView(@NonNull final Konfiguration wrapped,
                                @NonNull final String baseKey) {
            this.wrapped = wrapped;
            this.baseKey = baseKey.endsWith(".") ? baseKey : baseKey + ".";
        }

        KonfigurationSubsetView(@NonNull final Konfiguration wrapped) {
            this.wrapped = wrapped;
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
         * {@inheritDoc}
         */
        @Override
        public boolean update() {
            // Do not return false or true, as clients might put it in a while
            // loop and wait for a change. Simply fail.
            throw new KonfigurationException("update is not available from subset view");
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration subset(@NonNull final String key) {
            val newKey = this.baseKey + "." + key;
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
