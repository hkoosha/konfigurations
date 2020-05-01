package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KfgConcurrencyException;
import io.koosha.konfiguration.KfgIllegalStateException;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_Lock {

    @NotNull
    private final String name;

    @Nullable
    private final Long lockWaitTimeMillis;

    @NotNull
    private final ReadWriteLock LOCK;

    public Kombiner_Lock(@NotNull final String name,
                         @Nullable final Long lockWaitTimeMillis,
                         final boolean fair) {
        Objects.requireNonNull(name, "name");

        if (lockWaitTimeMillis != null && lockWaitTimeMillis < 0)
            throw new KfgIllegalStateException(name, "wait time must be gte 0: " + lockWaitTimeMillis);
        this.name = name;
        this.lockWaitTimeMillis = lockWaitTimeMillis;
        this.LOCK = new ReentrantReadWriteLock(fair);
    }

    private void acquire(@NotNull final Lock lock) {
        Objects.requireNonNull(lock, "lock");

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

    <T> T doReadLocked(@NotNull final Supplier<T> func) {
        Objects.requireNonNull(func, "func");

        Lock lock = null;
        try {
            lock = this.LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

    <T> T doWriteLocked(@NotNull final Supplier<T> func) {
        Objects.requireNonNull(func, "func");

        Lock lock = null;
        try {
            lock = this.LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

}
