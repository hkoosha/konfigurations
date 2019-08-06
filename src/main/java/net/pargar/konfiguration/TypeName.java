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
 * To create consistent error messages, name of types are taken from here.
 */
enum TypeName {
    BOOLEAN,
    INT,
    STRING,
    DOUBLE,
    LONG,
    LIST,
    MAP,
    SET,
    STRING_ARRAY("string array"),
    ;

    private final String tName;

    TypeName() {
        this.tName = this.name().toLowerCase();
    }

    TypeName(String tName) {
        this.tName = tName;
    }

    public String getTName() {
        return this.tName;
    }

    static String typeName(Class<?> base, Class<?> aux) {
        if (base == null && aux == null)
            return "?";
        if (base == null)
            return aux.getName();
        if (aux == null)
            return base.getName();
        else
            return base.getName() + "/" + aux.getName();
    }

}
