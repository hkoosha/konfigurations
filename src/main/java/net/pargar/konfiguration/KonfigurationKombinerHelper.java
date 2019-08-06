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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


final class KonfigurationKombinerHelper {

    private final Collection<KonfigSource> sources;

    private final Object UPDATE_LOCK = new Object();
    private final ReadWriteLock VALUES_LOCK = new ReentrantReadWriteLock();
    private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

    private final Map<String, Object> valueCache = new HashMap<>();
    private final Map<String, KonfigVImpl> kvalCache = new HashMap<>();

    private final WeakHashMap<KeyObserver, Collection<String>> keyObservers = new WeakHashMap<>();

    @SuppressWarnings("WeakerAccess")
    public KonfigurationKombinerHelper(Collection<KonfigSource> sources) {
        this.sources = sources;
    }

    // --------------------

    private Object putValue(final KonfigSource source,
                            final String name,
                            final Class<?> dataType,
                            final Class<?> elementType) {
        final Object result;

        if (dataType == Boolean.class)
            result = source.bool(name);
        else if (dataType == Integer.class)
            result = source.int_(name);
        else if (dataType == Long.class)
            result = source.long_(name);
        else if (dataType == Double.class)
            result = source.double_(name);
        else if (dataType == String.class)
            result = source.string(name);
        else if (dataType == List.class)
            result = source.list(name, elementType);
        else if (dataType == Map.class)
            result = source.map(name, elementType);
        else if (dataType == Set.class)
            result = source.set(name, elementType);
        else
            result = source.custom(name, elementType);

        valueCache.put(name, result);
        return result;
    }

    private <T> T getValue(String name, T def, boolean mustExist) {
        Lock lock = null;
        try {
            lock = VALUES_LOCK.readLock();
            lock.lock();

            if (valueCache.containsKey(name)) {
                @SuppressWarnings("unchecked") final T get = (T) valueCache.get(name);
                return get;
            }

            if (mustExist)
                throw new KonfigurationMissingKeyException(name);

            return def;
        }
        finally {
            if (lock != null)
                lock.unlock();
        }
    }

    // --------------------

    <T> K<T> getWrappedValue(String name, Class<?> dataType, Class<?> elementType) {

        // We can not do this, in order to support default values.
        //    if(does not exist) throw new KonfigurationMissingKeyException(key);

        Lock readLock = null;
        try {
            readLock = VALUES_LOCK.readLock();
            readLock.lock();
            if (this.kvalCache.containsKey(name))
                return this.getWrappedValue0(name, dataType, elementType);
        }
        finally {
            if (readLock != null)
                readLock.unlock();
        }

        Lock writeLock = null;
        try {
            writeLock = VALUES_LOCK.writeLock();
            writeLock.lock();

            // Cache was already populated between two locks.
            if (this.kvalCache.containsKey(name))
                return this.getWrappedValue0(name, dataType, elementType);

            for (final KonfigSource source : sources)
                if (source.contains(name)) {
                    this.putValue(source, name, dataType, elementType);
                    break;
                }

            final KonfigVImpl<T> rr = new KonfigVImpl<>(this, name, dataType, elementType);
            this.kvalCache.put(name, rr);
            return rr;
        }
        finally {
            if (writeLock != null)
                writeLock.unlock();
        }
    }

    private <T> K<T> getWrappedValue0(String name, Class<?> dataType, Class<?> elementType) {

        final KonfigVImpl<?> r = this.kvalCache.get(name);

        if (r.isSameAs(dataType, elementType)) {
            @SuppressWarnings("unchecked") final K<T> cast = (K<T>) r;
            return cast;
        } else {
            throw new KonfigurationTypeException(TypeName.typeName(dataType, elementType),
                                                 TypeName.typeName(r.getDataType(), r.getElementType()),
                                                 name);
        }
    }

    // --------------------


    boolean update() {
        synchronized (UPDATE_LOCK) {
            boolean isUpdatable = false;
            for (final KonfigSource source : this.sources)
                if (source.isUpdatable()) {
                    isUpdatable = true;
                    break;
                }
            if (!isUpdatable)
                return false;

            final List<KonfigSource> updatedSources = new ArrayList<>(this.sources.size());
            for (final KonfigSource source : this.sources)
                updatedSources.add(source.copyAndUpdate());

            final Map<String, Object> newCache = new HashMap<>();
            final Set<String> updatedKeys = new HashSet<>();

            final Map<KeyObserver, Collection<String>> observers = update0(updatedSources, newCache, updatedKeys);
            if (observers == null)
                return false;

            for (final Map.Entry<KeyObserver, Collection<String>> o : observers.entrySet()) {
                // Never empty or null.
                final Collection<String> interestedKeys = o.getValue();
                final KeyObserver observer = o.getKey();
                for (final String updatedKey : updatedKeys)
                    if (interestedKeys.contains(updatedKey))
                        observer.accept(updatedKey);
                if (interestedKeys.contains(""))
                    observer.accept("");
            }

            return true;
        }
    }

    /**
     * Thread safe (lock guarded) part of {@link #update()}.
     */
    private Map<KeyObserver, Collection<String>> update0(List<KonfigSource> updatedSources,
                                                         Map<String, Object> newCache,
                                                         Set<String> updatedKeys) {
        Lock vLock = null;
        try {
            vLock = VALUES_LOCK.writeLock();
            vLock.lock();

            // Bad idea, I know. Dead locks?
            Lock oLock = null;
            try {
                oLock = this.OBSERVERS_LOCK.writeLock();
                oLock.lock();

                for (final Map.Entry<String, Object> each : this.valueCache.entrySet()) {
                    final String name = each.getKey();
                    final Object value = each.getValue();
                    boolean found = false;
                    boolean changed = false;
                    for (final KonfigSource source : updatedSources) {
                        if (source.contains(name)) {
                            found = true;
                            final KonfigVImpl kVal = kvalCache.get(name);
                            final Object newVal = this.putValue(source,
                                                                name,
                                                                kVal.getDataType(),
                                                                kVal.getElementType());
                            newCache.put(name, newVal);
                            changed = !newVal.equals(value);
                            break;
                        }
                    }

                    if (!found || changed)
                        updatedKeys.add(name);
                }

                if (updatedKeys.isEmpty())
                    return null;

                valueCache.clear();
                valueCache.putAll(newCache);

                this.sources.clear();
                this.sources.addAll(updatedSources);

                return new HashMap<>(this.keyObservers);
            }
            finally {
                if (oLock != null)
                    oLock.unlock();
            }
        }
        finally {
            if (vLock != null)
                vLock.unlock();
        }
    }

    // --------------------

    void deregister(final KeyObserver observer, final String key) {
        Lock lock = null;
        try {
            lock = this.OBSERVERS_LOCK.writeLock();
            lock.lock();

            final Collection<String> keys = this.keyObservers.get(observer);
            if (keys == null)
                return;
            keys.remove(key);
            if (keys.isEmpty())
                this.keyObservers.remove(observer);
        }
        finally {
            if (lock != null)
                lock.unlock();
        }
    }

    void register(final KeyObserver observer, final String key) {
        Objects.requireNonNull(observer, "observer");
        Objects.requireNonNull(key, "key");

        Lock lock = null;
        try {
            lock = this.OBSERVERS_LOCK.writeLock();
            lock.lock();

            if (!this.keyObservers.containsKey(observer)) {
                final HashSet<String> put = new HashSet<>(1);
                put.add(key);
                this.keyObservers.put(observer, put);
            } else {
                this.keyObservers.get(observer).add(key);
            }
        }
        finally {
            if (lock != null)
                lock.unlock();
        }
    }


    // --------------------


    private static final class KonfigVImpl<T> implements K<T> {

        private final KonfigurationKombinerHelper origin;
        private final String key;
        private final Class<?> dataType;
        private final Class<?> elementType;

        private KonfigVImpl(KonfigurationKombinerHelper origin, String key, Class<?> dataType, Class<?> elementType) {
            this.origin = origin;
            this.key = key;
            this.dataType = dataType;
            this.elementType = elementType;
        }


        boolean isSameAs(Class<?> dataType, Class<?> elementType) {
            return Objects.equals(this.getDataType(), dataType) && Objects.equals(this.getElementType(), elementType);
        }

        Class<?> getDataType() {
            return this.dataType;
        }

        Class<?> getElementType() {
            return this.elementType;
        }


        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public T v() {
            return this.origin.getValue(key, null, true);
        }

        @Override
        public T v(final T defaultValue) {
            return this.origin.getValue(key, defaultValue, false);
        }

        @Override
        public K<T> deregister(final KeyObserver observer) {
            this.origin.deregister(observer, this.key);
            return this;
        }

        @Override
        public K<T> register(final KeyObserver observer) {
            Objects.requireNonNull(observer, "observer");
            this.origin.register(observer, this.key);
            return this;
        }

        @Override
        public String toString() {
            try {
                return String.format("KonfigV(%s=%s)", this.key, this.v().toString());
            }
            catch (final Exception e) {
                return String.format("KonfigV(%s=?)", this.key);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            KonfigVImpl<?> konfigV = (KonfigVImpl<?>) o;
            return Objects.equals(origin, konfigV.origin) && Objects.equals(key, konfigV.key);
        }

        @Override
        public int hashCode() {

            return Objects.hash(origin, key);
        }

    }

}
