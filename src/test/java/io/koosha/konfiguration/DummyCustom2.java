package io.koosha.konfiguration;

import java.beans.ConstructorProperties;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
class DummyCustom2 {
    final String str;
    final Map<String, String> olf;
    final int i;
    final String again;

    DummyCustom2(final String str, final String again, final Map<String, String> olf, final int i) {
        this.str = str;
        this.olf = olf;
        this.i = i;
        this.again = again;
    }

    @SuppressWarnings("unused")
    @ConstructorProperties({"again", "olf", "i", "str"})
    DummyCustom2(final String again, final Map<String, String> olf, final int i, final String str) {
        this(str, again, olf, i);
    }
}
