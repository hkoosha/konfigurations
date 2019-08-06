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


import java.util.function.Consumer;


/**
 * Konfiguration observer which observes change in the konfiguration source
 * related to a specific key.
 */
public interface KeyObserver extends Consumer<String> {

    /**
     * Called when the konfiguration for the {@code key} is changed (updated).
     *
     * @param key
     *         the konfiguration key that it's value was updated. use empty
     *         key (that is "") to receive update on all keys.
     */
    void accept(String key);

}
