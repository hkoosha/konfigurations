package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KfgException;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
@Immutable
@ApiStatus.Internal
final class HandleImpl implements Handle {

    private static final AtomicLong ID_POOL = new AtomicLong(Long.MIN_VALUE);

    private final long id;

    HandleImpl() {
        this.id = ID_POOL.updateAndGet(it -> {
            if (it == NONE.id())
                throw new KfgException(null, "the id pool is exhausted.");
            return it + 1;
        });
    }

    private HandleImpl(final long id) {
        this.id = id;
    }


    @Override
    public long id() {
        return this.id;
    }


    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof HandleImpl))
            return false;
        return this.id == ((HandleImpl) o).id;
    }

    @Override
    public int hashCode() {
        return 59 + (int) (this.id >>> 32 ^ this.id);
    }


    static final Handle NONE = new HandleImpl(Long.MAX_VALUE);

}
