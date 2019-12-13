package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.KfgConcurrencyException;
import io.koosha.konfiguration.KfgIllegalStateException;
import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_Lock {

    @NotNull
    @NonNull
    private final String name;

    @Nullable
    private final Long lockWaitTimeMillis;

    @NotNull
    private final ReadWriteLock LOCK;

    public Kombiner_Lock(@NotNull @NonNull final String name,
                         @Nullable final Long lockWaitTimeMillis,
                         final boolean fair) {
        if (lockWaitTimeMillis != null && lockWaitTimeMillis < 0)
            throw new KfgIllegalStateException(name, "wait time must be gte 0: " + lockWaitTimeMillis);
        this.name = name;
        this.lockWaitTimeMillis = lockWaitTimeMillis;
        LOCK = new ReentrantReadWriteLock(fair);
    }

    private void acquire(@NonNull @NotNull final Lock lock) {
        if (this.lockWaitTimeMillis == null)
            lock.lock();
        else
            try {
                if (!lock.tryLock(this.lockWaitTimeMillis, MILLISECONDS))
                    throw new KfgConcurrencyException(this.name, "could not acquire lock");
            }
            catch (final InterruptedException e) {
                throw new KfgConcurrencyException(this.name, "could not acquire lock", e);
            }
    }

    private void release(@Nullable final Lock lock) {
        if (lock != null)
            lock.unlock();
    }

    <T> T doReadLocked(@NonNull @NotNull final Supplier<T> func) {
        Lock lock = null;
        try {
            lock = LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

    <T> T doWriteLocked(@NonNull @NotNull final Supplier<T> func) {
        Lock lock = null;
        try {
            lock = LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

}
