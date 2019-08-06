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
 * Provides a konfiguration source to a {@link Konfiguration}
 *
 * <p>A config source must NOT be used directly, it must be supplied to
 * {@link Konfiguration}.<br>
 * Also see {@link #isUpdatable()}.
 *
 * <p>All the methods denoted with 'Thread-safe' in their comment section must be
 * implemented in a thread safe fashion.
 *
 * <p>All the implementations of this interface (and for the love of god all the
 * values returned by methods of this interface) must be immutable.
 *
 * <b>Important</b>: there is no {@code update()} method and there wont be.
 * there's only a {@link #copyAndUpdate()}, which updates too but does not modify the
 * original class.
 */
public interface KonfigSource {

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (boolean).
     */
    Boolean bool(String key);

    /**
     * Read and return a int value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (int).
     */
    Integer int_(String key);

    /**
     * Read and return a long value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (long).
     */
    Long long_(String key);

    /**
     * Read and return a double value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (double).
     */
    Double double_(String key);

    /**
     * Read and return a string value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (string).
     */
    String string(String key);


    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of elements in the list.
     * @param <T>
     *         generic type of elements in the list.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (list of
     *         T).
     */
    <T> List<T> list(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of values of the map.
     * @param <T>
     *         generic type of value elements in the map.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (map from
     *         String to T).
     */
    <T> Map<String, T> map(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of elements in the set.
     * @param <T>
     *         generic type of elements in the set.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (set of
     *         T).
     */
    <T> Set<T> set(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of the custom object requested.
     * @param <T>
     *         generic type of the custom object requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (object
     *         of type T).
     */
    <T> T custom(String key, Class<T> type);


    /**
     * Check if requested key is present in this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the key to check.
     *
     * @return true if the requested key exists in this source.
     */
    boolean contains(String key);


    /**
     * Indicates whether if anything is actually updated in the origin of this
     * source (that is, the source returned by {@link #copyAndUpdate()} differs
     * from this source.)
     *
     * <p>This action must <b>NOT</b> modify this source.
     *
     * <p><b>VERY VERY IMPORTANT:</b> This method is to be called only from
     * {@link KonfigurationKombiner} or
     * concurrency issues will arise.
     * <br>Why? To check and see if it's updatable, a source might ask it's
     * origin (a web url?) to get the new content, to compare with the old
     * content, and it asks it's origin for the new content once more, to
     * actually update the values. If this method is called during
     * KonfigurationKombiner is also calling it, this might interfere and lost
     * updates may happen.
     *
     * <p>NOT Thread-safe.
     *
     * @return true if the source obtained via {@link #copyAndUpdate()} will
     * differ from this source.
     */
    boolean isUpdatable();

    /**
     * Creates an <b>updated</b> copy of this source.
     *
     * <p>A call to this method must <b>NOT</b> modify this source, but the
     * newly created source must contain the updated values.
     *
     * <p>NOT Thread-safe.
     *
     * @return an updated copy of this source.
     */
    KonfigSource copyAndUpdate();

}
