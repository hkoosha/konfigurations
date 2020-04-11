package io.koosha.konfiguration;

/**
 * A dummy custom value object, used to test de/serialization frameworks.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess", "unused"})
public class DummyCustom {

    public String str;
    public int i;

    public DummyCustom() {
        this("", 0);
    }

    public DummyCustom(final String str, final int i) {
        this.str = str;
        this.i = i;
    }

    public String concat() {
        return this.str + " ::: " + this.i;
    }

}
