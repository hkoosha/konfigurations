package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
@Immutable
@ApiStatus.Internal
final class HandleImpl implements Handle {

    private static final AtomicLong id_pool = new AtomicLong(Long.MAX_VALUE);

    private final long id = id_pool.incrementAndGet();


    /**
     * {@inheritDoc}
     */
    @Override
    public long id() {
        return this.id;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HandleImpl))
            return false;
        return this.id == ((HandleImpl) o).id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 59 + (int) (this.id >>> 32 ^ this.id);
    }

}
