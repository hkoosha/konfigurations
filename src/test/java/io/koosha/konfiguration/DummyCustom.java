package io.koosha.konfiguration;


@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class DummyCustom {

    public String str = "";
    public int i = 0;

    public DummyCustom() {
    }

    public DummyCustom(final String str, final int i) {
        this.str = str;
        this.i = i;
    }

    public String concat() {
        return this.str + " ::: " + this.i;
    }

}
