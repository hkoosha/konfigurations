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
package net.pargar.konfiguration.guice;


import net.pargar.konfiguration.Konfig;

import java.lang.annotation.Annotation;

import static java.lang.String.format;


/**
 * Thread-safe, immutable.
 */
@SuppressWarnings({"ClassExplicitlyAnnotation",
                   "unused"
                  })
public final class KonfigImpl implements Konfig {

    private static final long serialVersionUID = 0;

    private final String value;

    private final Class<? extends Annotation> annotationType = Konfig.class;

    @SuppressWarnings("WeakerAccess")
    public KonfigImpl(final String value) {
        if (value == null)
            throw new NullPointerException("value");
        this.value = value;
    }

    public static Konfig annon(final String value) {

        return new KonfigImpl(value);
    }


    public String value() {

        return this.value;
    }

    public Class<? extends Annotation> annotationType() {

        return this.annotationType;
    }


    @Override
    public int hashCode() {

        // This is specified in java.lang.Annotation.
        int h = 0;
        h += (127 * "value".hashCode()) ^ value.hashCode();

        return h;
    }

    public boolean equals(final Object o) {

        if (o == this)
            return true;

        if (!(o instanceof Konfig))
            return false;

        final Konfig other = (Konfig) o;

        return this.value.equals(other.value());
    }

    @Override
    public String toString() {

        return format("@%s(value=%s)", Konfig.class.getName(), this.value);
    }

}
