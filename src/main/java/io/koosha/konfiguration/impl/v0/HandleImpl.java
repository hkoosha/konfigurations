package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Handle;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
@Immutable
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
@ApiStatus.Internal
final class HandleImpl implements Handle {

    private static final AtomicLong id_pool = new AtomicLong(Long.MAX_VALUE);

    @Getter
    private final long id = id_pool.incrementAndGet();

}
