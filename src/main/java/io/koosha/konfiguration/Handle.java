package io.koosha.konfiguration;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;

@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(Factory.VERSION_8)
public interface Handle {

    long id();

    static Handle M_1 = () -> -1L;

}
