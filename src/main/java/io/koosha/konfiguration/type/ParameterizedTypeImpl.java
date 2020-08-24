package io.koosha.konfiguration.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

final class ParameterizedTypeImpl implements ParameterizedType, Serializable {

    private static final long serialVersionUID = 1;

    @NotNull
    private final Type[] actualTypeArguments;

    @NotNull
    private final Type rawType;

    @Nullable
    private final Type ownerType;

    ParameterizedTypeImpl(@NotNull final Type[] actualTypeArguments,
                          @NotNull final Type rawType,
                          @Nullable final Type ownerType) {
        Objects.requireNonNull(actualTypeArguments);
        Objects.requireNonNull(rawType);
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ownerType;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public Type[] getActualTypeArguments() {
        return this.actualTypeArguments.clone();
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public Type getRawType() {
        return this.rawType;
    }

    @Contract(pure = true)
    @Nullable
    @Override
    public Type getOwnerType() {
        return this.ownerType;
    }


    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments)
            ^ Objects.hashCode(this.ownerType)
            ^ Objects.hashCode(this.rawType);
    }

    @Contract(pure = true)
    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ParameterizedTypeImpl))
            return false;
        final ParameterizedTypeImpl other = (ParameterizedTypeImpl) obj;
        return Objects.equals(other.ownerType, this.ownerType)
            && Objects.equals(other.rawType, this.rawType)
            && Arrays.equals(this.actualTypeArguments, other.actualTypeArguments);
    }

}
