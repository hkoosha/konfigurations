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
 * Dummy konfig value, holding a constant konfig value with no source.
 *
 * <p>Good for use in cases where a konfiguration source is not available but a
 * konfiguration value is needed.
 *
 * <p>Thread-safe and immutable.
 *
 * <p>{@link #deregister(KeyObserver)} and {@link #register(KeyObserver)} do NOT
 * work.
 *
 * <p>Regarding equals and hashcode: Each instance of DummyV is considered to be
 * from a different origin (in contrast to _KonfigVImpl, so only each
 * object is equal to itself only, even with same key and values.
 *
 * @param <T>
 *         type of konfig value this object holds.
 */
@SuppressWarnings({"unused",
                   "WeakerAccess"
                  })
public final class DummyV<T> implements K<T> {

    private final String key;
    private final T v;
    private final boolean hasValue;

    private DummyV(final String key, final T v, final boolean hasValue) {
        Objects.requireNonNull(key, "key");
        this.key = key;
        this.v = v;
        this.hasValue = hasValue;
    }

    /**
     * For konfiguration value holding no actual value ({@link #v()} always
     * fails).
     *
     * @param key
     *         key representing this konfiguration value.
     *
     * @throws NullPointerException
     *         if the key is null
     */
    public DummyV(final String key) {
        this(key, null, false);
    }

    /**
     * For konfiguration value holding v as value and key as its key.
     *
     * @param key
     *         key representing this konfiguration value.
     * @param v
     *         value this konfiguration holds (can be null).
     *
     * @throws NullPointerException
     *         if the key is null
     */
    public DummyV(final String key, final T v) {
        this(key, v, true);
    }


    /**
     * No-op. does nothing.
     */
    @Override
    public K<T> deregister(final KeyObserver observer) {
        // Note that this.v is constant and never changes, but in combination
        // to other sources, it might!
        return this;
    }

    /**
     * No-op. does nothing.
     */
    @Override
    public K<T> register(final KeyObserver observer) {
        // Note that this.v is constant and never changes, but in combination
        // to other sources, it might!
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T v() {
        if (this.hasValue)
            return this.v;

        throw new KonfigurationMissingKeyException(this.key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T v(final T defaultValue) {
        return this.hasValue ? this.v : defaultValue;
    }


    // ________________________________________________ PREDEFINED CONST VALUES

    public static K<Boolean> false_() {
        return new DummyV<>("", false);
    }

    public static K<Boolean> true_() {
        return new DummyV<>("", true);
    }

    public static K<Integer> int_(final Integer i) {
        return new DummyV<>("", i);
    }

    public static K<Long> long_(final Long l) {
        return new DummyV<>("", l);
    }

    public static K<Double> double_(final Double d) {
        return new DummyV<>("", d);
    }

    public static K<String> string(final String s) {
        return new DummyV<>("", s);
    }

    public static <T> DummyV<Collection<T>> emptyCollection() {
        return new DummyV<Collection<T>>("", Collections.<T>emptyList());
    }

    public static <T> DummyV<List<T>> emptyList() {
        return new DummyV<>("", Collections.<T>emptyList());
    }

    public static <K, V> DummyV<Map<K, V>> emptyMap() {
        return new DummyV<>("", Collections.<K, V>emptyMap());
    }

    public static <T> DummyV<Set<T>> emptySet() {
        return new DummyV<>("", Collections.<T>emptySet());
    }

    public static <T> DummyV<List<T>> list(final List<T> l) {
        return new DummyV<>("", l);
    }

    public static <K, V> DummyV<Map<K, V>> map(final Map<K, V> m) {
        return new DummyV<>("", m);
    }

    public static <T> DummyV<Set<T>> set(final Set<T> s) {
        return new DummyV<>("", s);
    }

    public static <T> K<T> null_() {
        return new DummyV<>("", null);
    }

}
