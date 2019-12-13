package io.koosha.konfiguration;


@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
class DummyCustom {

    String str = "";
    int i = 0;

    DummyCustom() {
    }

    DummyCustom(final String str, final int i) {
        this.str = str;
        this.i = i;
    }

    String concat() {
        return this.str + " ::: " + this.i;
    }

}
