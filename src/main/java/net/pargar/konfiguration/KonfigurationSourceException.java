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


/**
 * Exceptions regarding the source (backing storage), or as a wrapper around
 * exceptions thrown by the backing storage.
 */
@SuppressWarnings({"unused",
                   "WeakerAccess"
                  })
public class KonfigurationSourceException extends KonfigurationException {

    public KonfigurationSourceException() {
        super();
    }

    public KonfigurationSourceException(String message) {
        super(message);
    }

    public KonfigurationSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public KonfigurationSourceException(Throwable cause) {
        super(cause);
    }

}
