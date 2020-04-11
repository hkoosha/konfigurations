package io.koosha.konfiguration;

import java.beans.ConstructorProperties;
import java.util.Map;

/**
 * A dummy custom value object, used to test de/serialization frameworks.
 *
 * All fields are final here, only constructor can be utilized.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class DummyCustom2 {

    public final String str;
    public final Map<String, String> olf;
    public final int i;
    public final String again;

    public DummyCustom2(final String str, final String again, final Map<String, String> olf, final int i) {
        this.str = str;
        this.olf = olf;
        this.i = i;
        this.again = again;
    }

    @ConstructorProperties({"again", "olf", "i", "str"})
    public DummyCustom2(final String again, final Map<String, String> olf, final int i, final String str) {
        this(str, again, olf, i);
    }

}
