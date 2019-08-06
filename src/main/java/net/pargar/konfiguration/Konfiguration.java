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


import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * All methods thread-safe, except for {@link #update()}.
 */
public interface Konfiguration {

    /**
     * Get a boolean konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    K<Boolean> bool(String key);

    /**
     * Get an int konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    K<Integer> int_(String key);

    /**
     * Get a long konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    K<Long> long_(String key);

    /**
     * Get a double konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    K<Double> double_(String key);

    /**
     * Get a string konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    K<String> string(String key);

    /**
     * Get a list of T konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     * @param type
     *         type object of values in the list.
     * @param <T>
     *         generic type of elements in the list.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    <T> K<List<T>> list(String key, Class<T> type);

    /**
     * Get a map of String to T konfiguration value. Keys are always of type
     * string.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     * @param type
     *         type object of map values. (keys are always of type String.class).
     * @param <T>
     *         generic type of elements in the map (for map values).
     *
     * @return konfiguration value wrapper for the requested key.
     */
    <T> K<Map<String, T>> map(String key, Class<T> type);

    /**
     * Get a set of T konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         unique key of the konfiguration being requested.
     * @param type
     *         type object of values in the set.
     * @param <T>
     *         generic type of elements in the set.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    <T> K<Set<T>> set(String key, Class<T> type);

    /**
     * Get a custom object of type T konfiguration value.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain maps,
     * lists or sets. Use the corresponding methods {@link #map(String, Class)},
     * {@link #list(String, Class)} and {@link #set(String, Class)}.
     *
     * <p>Thread-safe
     *
     * @param key
     *         unique key of the konfiguration being requested.
     * @param type
     *         type object of the requested value.
     * @param <T>
     *         generic type of requested value.
     *
     * @return konfiguration value wrapper for the requested key.
     */
    <T> K<T> custom(String key, Class<T> type);


    /**
     * Update all the konfiguration values and notify the update observers.
     *
     * <p>Important: the key observers might be notified <em>after</em> the
     * cache update, and it is implementation specific. So it's possible that a
     * call to {@link K#v()} returns the new value, while the observer is not
     * notified yet.
     *
     * <p>Important: order of calling {@link KeyObserver}s is implementation
     * specific.
     *
     * <p>Thread safe.
     *
     * @return true if anything was changed during this update.
     *
     * @throws KonfigurationException
     *         if the konfiguration is not updatable
     */
    boolean update();

    /**
     * Get a subset view of this konfiguration representing all the values under
     * the namespace of supplied key.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the key to which the scope of returned konfiguration is
     *         limited.
     *
     * @return a konfiguration whose scope is limited to the supplied key.
     */
    @SuppressWarnings("unused")
    Konfiguration subset(String key);

    /**
     * Register a listener to be notified of any updates to this konfiguration.
     * register to empty key (that is "") to receive update on all keys.
     *
     * <p>Thread-safe.
     *
     * <p><b>IMPORTANT:</b> Do NOT just pass in lambdas, as this method stores
     * only weak references and the observer will be garbage collected. Keep a
     * reference to the observer yourself.
     *
     * @param observer
     *         the listener to register.
     *
     * @return this.
     */
    Konfiguration register(KeyObserver observer);

    /**
     * De-Register a previously registered listener via
     * {@link #register(KeyObserver)}.
     * register to empty key (that is "") to receive update on all keys.
     *
     * <p>Thread-safe.
     *
     * @param observer
     *         the listener to de-register.
     *
     * @return this.
     *
     * @see #register(KeyObserver)
     */
    Konfiguration deregister(KeyObserver observer);

}
