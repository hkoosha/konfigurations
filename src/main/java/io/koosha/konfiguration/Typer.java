package io.koosha.konfiguration;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(KonfigurationFactory.VERSION_8)
public abstract class Typer<TYPE> {

    @ApiStatus.Experimental
    private static final boolean ALLOW_PARAMETRIZED = true;

    @Nullable
    private final String key;

    @Nullable
    private final ParameterizedType pt;

    @NotNull
    private final Class<TYPE> klass;

    private Typer(@NotNull final Class<TYPE> type) {
        Objects.requireNonNull(type, "type");
        this.key = null;
        this.pt = null;
        this.klass = type;
    }

    private Typer(@Nullable final String key,
                  @Nullable final ParameterizedType pt,
                  @NotNull final Class<TYPE> klass) {
        Objects.requireNonNull(klass, "klass");
        this.key = key;
        this.pt = pt;
        this.klass = klass;
    }

    @SuppressWarnings("unchecked")
    protected Typer() {
        final Type t = ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        checkIsClassOrParametrizedType(t, null);

        if (t instanceof ParameterizedType) {
            this.pt = (ParameterizedType) t;
            this.klass = (Class<TYPE>) this.pt.getRawType();
        }
        else {
            this.pt = null;
            this.klass = (Class<TYPE>) t;
        }

        this.key = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format(
                "Q::%s::%s::%s",
                this.klass().getTypeName(),
                this.pt == null ? "?" : this.pt,
                this.key == null ? "?" : this.key
        );
    }

    @Override
    public final boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Typer))
            return false;
        final Typer<?> other = (Typer<?>) o;
        return Objects.equals(this.key, other.key)
                && Objects.equals(this.pt, other.pt)
                && Objects.equals(this.klass, other.klass);
    }

    @Override
    public final int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.key == null ? 43 : ((Object) this.key).hashCode());
        result = result * PRIME + (this.pt == null ? 43 : this.pt.hashCode());
        result = result * PRIME + this.klass.hashCode();
        return result;
    }

    @Contract(pure = true)
    @Nullable
    public final String key() {
        return this.key;
    }

    @Contract(pure = true)
    @Nullable
    public final ParameterizedType pt() {
        return this.pt;
    }

    @Contract(pure = true)
    @NotNull
    public final Class<TYPE> klass() {
        return this.klass;
    }

    @Contract(pure = true)
    public final boolean isParametrized() {
        return this.pt != null;
    }

    @Contract(pure = true)
    @NotNull
    public final Typer<TYPE> withKey(@Nullable final String key) {
        return Objects.equals(this.key, key)
                ? this
                : new Typer<TYPE>(key, this.pt, this.klass) {
        };
    }

    // ---------------------------------

    @Contract(pure = true)
    final boolean matchesType(@Nullable final Typer<?> other) {
        if (other == null || other == this)
            return true;
        // TODO
        return other.klass().isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    final boolean matchesValue(@Nullable final Object v) {
        if (v == null)
            return true;
        // TODO
        return v.getClass().isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    public static boolean matchesType(@Nullable final Typer<?> typer0,
                                      @Nullable final Typer<?> typer1) {
        if (typer0 == null || typer1 == null)
            return true;
        return typer0.matchesType(typer1);
    }

    @Contract(pure = true)
    public static boolean matchesValue(@Nullable final Typer<?> typer0,
                                       @Nullable final Object value) {
        if (typer0 == null || value == null)
            return true;
        return typer0.matchesValue(value);
    }

    // ---------------------------------

    /**
     * If Q represents a collection, get type argument of the collection.
     *
     * @return type argument of the collection represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a collection.
     */
    @Contract(pure = true)
    @Nullable
    public final Type getCollectionContainedType() {
        if (!this.isCollection())
            throw new KfgIllegalStateException(null, "type is not a collection");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
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
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, null, null, "type is not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
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
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, null, null, "type is not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[1];
    }

    // ---------------------------------

    @Contract(pure = true)
    public final boolean isBool() {
        return Boolean.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isChar() {
        return Character.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isString() {
        return String.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isByte() {
        return Byte.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isShort() {
        return Short.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isInt() {
        return Integer.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isLong() {
        return Long.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isFloat() {
        return Float.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isDouble() {
        return Double.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isSet() {
        return Set.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isCollection() {
        return this.isList() || this.isSet();
    }

    @Contract(pure = true)
    public final boolean isList() {
        return List.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isMap() {
        return Map.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isNull() {
        return isVoid();
    }

    @Contract(pure = true)
    public final boolean isVoid() {
        //noinspection ConstantConditions
        return Void.class.isAssignableFrom(this.klass);
    }

    // =========================================================================

    /**
     * Factory method.
     *
     * @param klass the type to create a Q for.
     * @param <U>   Generic type of requested class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <U> Typer<U> of(@NotNull final Class<U> klass) {
        Objects.requireNonNull(klass, "klass");
        if (!ALLOW_PARAMETRIZED && Typer.isParametrized(klass))
            throw new KfgIllegalArgumentException(
                    null, "parametrized types are not supported, klass=" + klass);
        return new Typer<U>(klass) {
        };
    }

    @Contract(value = "_ -> new", pure = true)
    private static <U> Typer<U> unsafeOf(@NotNull final Class<U> klass) {
        Objects.requireNonNull(klass, "klass");
        return new Typer<U>(klass) {
        };
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static <U> Typer<U> of_(@NotNull final Class<?> klass) {
        Objects.requireNonNull(klass, "klass");
        return (Typer<U>) of(klass);
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
                    "only Class and ParameterizedType are supported: "
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

    public static final Typer<Boolean> BOOL = of(Boolean.class);
    public static final Typer<Character> CHAR = of(Character.class);

    public static final Typer<Byte> BYTE = of(Byte.class);
    public static final Typer<Short> SHORT = of(Short.class);
    public static final Typer<Integer> INT = of(Integer.class);
    public static final Typer<Long> LONG = of(Long.class);
    public static final Typer<Float> FLOAT = of(Float.class);
    public static final Typer<Double> DOUBLE = of(Double.class);

    public static final Typer<String> STRING = of(String.class);

    public static final Typer<Map<?, ?>> UNKNOWN_MAP = of_(Map.class);
    public static final Typer<Set<?>> UNKNOWN_SET = of_(Set.class);
    public static final Typer<List<?>> UNKNOWN_LIST = of_(List.class);
    public static final Typer<Collection<?>> UNKNOWN_COLLECTION = of_(Collection.class);

    public static final Typer<Object> OBJECT = of_(Object.class);
    public static final Typer<?> UNKNOWN = OBJECT;

    public static final Typer<List<Byte>> LIST_BYTE = new Typer<List<Byte>>() {
    };

    public static final Typer<List<Short>> LIST_SHORT = new Typer<List<Short>>() {
    };
    public static final Typer<List<Integer>> LIST_INT = new Typer<List<Integer>>() {
    };
    public static final Typer<List<Long>> LIST_LONG = new Typer<List<Long>>() {
    };
    public static final Typer<List<Float>> LIST_FLOAT = new Typer<List<Float>>() {
    };
    public static final Typer<List<Double>> LIST_DOUBLE = new Typer<List<Double>>() {
    };
    public static final Typer<List<String>> LIST_STRING = new Typer<List<String>>() {
    };

    public static final Typer<Set<Byte>> SET_BYTE = new Typer<Set<Byte>>() {
    };
    public static final Typer<Set<Short>> SET_SHORT = new Typer<Set<Short>>() {
    };
    public static final Typer<Set<Integer>> SET_INT = new Typer<Set<Integer>>() {
    };
    public static final Typer<Set<Long>> SET_LONG = new Typer<Set<Long>>() {
    };
    public static final Typer<Set<Float>> SET_FLOAT = new Typer<Set<Float>>() {
    };
    public static final Typer<Set<Double>> SET_DOUBLE = new Typer<Set<Double>>() {
    };
    public static final Typer<Set<String>> SET_STRING = new Typer<Set<String>>() {
    };

    public static final Typer<Map<String, Short>> MAP_STRING__SHORT = new Typer<Map<String, Short>>() {
    };
    public static final Typer<Map<String, Integer>> MAP_STRING__INT = new Typer<Map<String, Integer>>() {
    };
    public static final Typer<Map<String, Long>> MAP_STRING__LONG = new Typer<Map<String, Long>>() {
    };
    public static final Typer<Map<String, Float>> MAP_STRING__FLOAT = new Typer<Map<String, Float>>() {
    };
    public static final Typer<Map<String, Double>> MAP_STRING__DOUBLE = new Typer<Map<String, Double>>() {
    };
    public static final Typer<Map<String, String>> MAP_STRING__STRING = new Typer<Map<String, String>>() {
    };

    public static final Typer<?> _VOID = of_(Void.class);

}
