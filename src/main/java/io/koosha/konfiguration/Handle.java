package io.koosha.konfiguration;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
@Immutable
public interface Handle {

    long id();


    @Override
    boolean equals(Object other);

    @Override
    int hashCode();

}
