package io.koosha.konfiguration.type;

import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KonfigurationFactory;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
public abstract class Kind<TYPE> implements Serializable {

    private static final long serialVersionUID = 1;

    @Nullable
    private final String key;

    @NotNull
    private final Type type;

    private Kind(@Nullable final String key,
                 @Nullable final Type t) {
        Objects.requireNonNull(t, "type");
        checkIsClassOrParametrizedType(t, null);
        this.key = key;
        this.type = t;
    }

    private Kind(@NotNull final Type t) {
        this(null, t);
    }

    protected Kind() {
        this((String) null);
    }

    protected Kind(@Nullable final String key) {
        final Type t =
                ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];

        checkIsClassOrParametrizedType(t, null);

        this.key = key;
        this.type = t;
    }

    @Contract(pure = true)
    @Nullable
    public final String key() {
        return this.key;
    }

    @Contract(pure = true)
    @NotNull
    public final Type type() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    @NotNull
    public final Class<TYPE> klass() {
        return this.type instanceof Class
                ? (Class<TYPE>) this.type
                : (Class<TYPE>) ((ParameterizedType) this.type).getRawType();
    }

    @Contract(pure = true)
    @NotNull
    public final ParameterizedType parametrized() {
        return (ParameterizedType) this.type;
    }

    @Contract(pure = true)
    public final boolean isParametrized() {
        return this.type instanceof ParameterizedType;
    }

    @Contract(pure = true)
    @NotNull
    public final Kind<TYPE> withKey(@Nullable final String key) {
        return Objects.equals(this.key, key)
                ? this
                : new Kind<TYPE>(key, this.type) {
        };
    }

    // ---------------------------------

    /**
     * If Q represents a collection, get type argument of the collection.
     *
     * @return type argument of the collection represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a collection.
     */
    @Contract(pure = true)
    @NotNull
    public final Type getCollectionContainedType() {
        if (!this.isCollection() || !this.isParametrized())
            throw new KfgIllegalStateException(null, this.key, null, null, "is not a collection or collection type is not known");
        return this.parametrized().getActualTypeArguments()[0];
    }

    /**
     * If Q represents a map, get type argument of the map's key.
     *
     * @return type argument of the map's key represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Contract(pure = true)
    @Nullable
    public final Type getMapKeyType() {
        if (!this.isMap() || !this.isParametrized())
            throw new KfgIllegalStateException(null, this.key, null, null, "is not a map or map type is not known");
        return this.parametrized().getActualTypeArguments()[0];
    }

    /**
     * If Q represents a map, get type argument of the map's value.
     *
     * @return type argument of the map's value represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Contract(pure = true)
    @Nullable
    public final Type getMapValueType() {
        if (!this.isMap() || !this.isParametrized())
            throw new KfgIllegalStateException(null, this.key, null, null, "is not a map or map type is not known");
        return this.parametrized().getActualTypeArguments()[1];
    }

    public final Kind<List<TYPE>> getAsListTypeFromSetType() {
        return new Kind<List<TYPE>>(this.key, new PatametrizedTypeImpl(
                this.parametrized().getActualTypeArguments(), Set.class, null)) {
        };
    }

    // ---------------------------------

    @Contract(pure = true)
    public final boolean isBool() {
        return Boolean.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isChar() {
        return Character.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isString() {
        return String.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isByte() {
        return Byte.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isShort() {
        return Short.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isInt() {
        return Integer.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isLong() {
        return Long.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isFloat() {
        return Float.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isDouble() {
        return Double.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isSet() {
        return Set.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isCollection() {
        return this.isList() || this.isSet();
    }

    @Contract(pure = true)
    public final boolean isList() {
        return List.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isMap() {
        return Map.class.isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public final boolean isNull() {
        return isVoid();
    }

    @Contract(pure = true)
    public final boolean isVoid() {
        //noinspection ConstantConditions
        return Void.class.isAssignableFrom(this.klass());
    }


    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public final String toString() {
        return String.format(
                "Q::%s::%s",
                this.key == null ? "key?" : this.key,
                this.type
        );
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public final boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Kind))
            return false;
        final Kind<?> other = (Kind<?>) o;
        return Objects.equals(this.key, other.key)
                && Objects.equals(this.type, other.type);
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public final int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.key == null ? 43 : ((Object) this.key).hashCode());
        result = result * PRIME + this.type.hashCode();
        return result;
    }


    // =========================================================================

    @Contract(pure = true)
    public static boolean matchesValue(@Nullable final Kind<?> kind0,
                                       @Nullable final Object value) {
        if (kind0 == null || value == null)
            return true;

        if (!kind0.klass().isAssignableFrom(value.getClass()))
            return false;
        if (!kind0.isParametrized())
            return true;
    }

    // ---------------------------------


    /**
     * Factory method.
     *
     * @param klass the type to create a Q for.
     * @param <U>   Generic type of requested class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <U> Kind<U> of(@NotNull Class<U> klass) {
        Objects.requireNonNull(klass, "klass");
        if (klass == boolean.class)
            klass = (Class<U>) Boolean.class;
        else if (klass == byte.class)
            klass = (Class<U>) Byte.class;
        else if (klass == char.class)
            klass = (Class<U>) Character.class;
        else if (klass == short.class)
            klass = (Class<U>) Short.class;
        else if (klass == int.class)
            klass = (Class<U>) Integer.class;
        else if (klass == long.class)
            klass = (Class<U>) Long.class;
        else if (klass == float.class)
            klass = (Class<U>) Float.class;
        else if (klass == double.class)
            klass = (Class<U>) Double.class;
        return new Kind<U>(klass) {
        };
    }


    public static <U> Kind<Set<U>> set(@NotNull final Class<U> u) {
        Objects.requireNonNull(u, "u (set type)");
        throw new UnsupportedOperationException();
    }

    public static <U> Kind<List<U>> list(@NotNull final Class<U> u) {
        Objects.requireNonNull(u, "u (list type)");
        throw new UnsupportedOperationException();
    }

    public static <K, V> Kind<Map<K, V>> map(@NotNull final Class<K> k,
                                             @NotNull final Class<V> v) {
        Objects.requireNonNull(k, "k (map key type)");
        Objects.requireNonNull(v, "v (map value type)");
        throw new UnsupportedOperationException();
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static <U> Kind<U> of_(@NotNull final Class<?> klass) {
        Objects.requireNonNull(klass, "klass");
        return new Kind(klass) {
        };
    }

    @Contract(pure = true)
    private static void checkIsClassOrParametrizedType(@Nullable final Type p,
                                                       @Nullable Type root) {
        if (root == null)
            root = p;

        if (p == null)
            return;

        if (!(p instanceof Class) && !(p instanceof ParameterizedType))
            throw new UnsupportedOperationException(
                    "only Class and ParameterizedType are supported (no array, wildcard or anything else), got: "
                            + root + "::" + p);

        if (!(p instanceof ParameterizedType))
            return;
        final ParameterizedType pp = (ParameterizedType) p;

        checkIsClassOrParametrizedType(root, pp.getRawType());
        for (final Type ppp : pp.getActualTypeArguments())
            checkIsClassOrParametrizedType(root, ppp);
    }

    @Contract(pure = true)
    private static boolean isParametrized(@NotNull final Class<?> p) {
        return p.getTypeParameters().length > 0 ||
                p.getSuperclass() != null && isParametrized(p.getSuperclass());
    }

    // =========================================================================

    public static final Kind<Boolean> BOOL = of(Boolean.class);
    public static final Kind<Character> CHAR = of(Character.class);
    public static final Kind<Byte> BYTE = of(Byte.class);
    public static final Kind<Short> SHORT = of(Short.class);
    public static final Kind<Integer> INT = of(Integer.class);
    public static final Kind<Long> LONG = of(Long.class);
    public static final Kind<Float> FLOAT = of(Float.class);
    public static final Kind<Double> DOUBLE = of(Double.class);
    public static final Kind<String> STRING = of(String.class);
    public static final Kind<?> _VOID = of_(Void.class);

    public static final Kind<List<Integer>> LIST_INT = new Kind<List<Integer>>() {
    };
    public static final Kind<Set<Integer>> SET_INT = new Kind<Set<Integer>>() {
    };

    public final Kind<List<TYPE>> asList() {

    }

    public final Kind<Set<TYPE>> asSet() {

    }

    // =========================================================================

    private static final class PatametrizedTypeImpl implements ParameterizedType {

        private final Type[] actualTypeArguments;
        private final Type rawType;
        private final Type ownerType;

        private PatametrizedTypeImpl(@NotNull final Type[] actualTypeArguments,
                                     @NotNull final Type rawType,
                                     final Type ownerType) {
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
            if (!(obj instanceof Kind.PatametrizedTypeImpl))
                return false;
            final PatametrizedTypeImpl other = (PatametrizedTypeImpl) obj;
            return Objects.equals(other.ownerType, this.ownerType)
                    && Objects.equals(other.rawType, this.rawType)
                    && Arrays.equals(this.actualTypeArguments, other.actualTypeArguments);
        }
    }

}
