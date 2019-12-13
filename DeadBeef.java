package io.koosha.konfiguration;

enum DeadBeef {
    ;

//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public final K<Boolean> bool(final String key) {
//        return getWrappedValue(key, Q.BOOL);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public K<Byte> byte_(String key) {
//        return getWrappedValue(key, Q.BYTE);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public K<Character> char_(String key) {
//        return getWrappedValue(key, Q.CHAR);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public K<Short> short_(String key) {
//        return getWrappedValue(key, Q.SHORT);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public final K<Integer> int_(final String key) {
//        return getWrappedValue(key, Q.INT);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public final K<Long> long_(final String key) {
//        return getWrappedValue(key, Q.LONG);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public K<Float> float_(String key) {
//        return getWrappedValue(key, Q.FLOAT);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public final K<Double> double_(final String key) {
//        return getWrappedValue(key, Q.DOUBLE);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public final K<String> string(final String key) {
//        return getWrappedValue(key, Q.STRING);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public <U> K<List<U>> list(final String key, final Q<List<U>> type) {
//        return getWrappedValue(key, type);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public <U, V> K<Map<U, V>> map(final String key, Q<Map<U, V>> type) {
//        return getWrappedValue(key, type);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public <U> K<Set<U>> set(final String key, final Q<Set<U>> type) {
//        return getWrappedValue(key, type);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    @SuppressWarnings({"unchecked", "rawtypes"})
//    public <U> K<U> custom(final String key, final Q<U> type) {
//        nn(type, "type");
//
//        if (type.typeName().isBool())
//            return (K<U>) this.bool(key);
//
//        if (type.typeName().isInt())
//            return (K<U>) this.int_(key);
//
//        if (type.typeName().isLong())
//            return (K<U>) this.long_(key);
//
//        if (type.typeName().isDouble())
//            return (K<U>) this.double_(key);
//
//        if (type.typeName().isString())
//            return (K<U>) this.string(key);
//
//
//        if (type.typeName().isMap())
//            return (K<U>) this.map(key, (Q) type);
//
//        if (type.typeName().isSet())
//            return (K<U>) this.set(key, (Q) type);
//
//        if (type.typeName().isList())
//            return (K<U>) this.list(key, (Q) type);
//
//
//        if (type.typeName().isCustom())
//            return getWrappedValue(key, type);
//
//        throw new KfgIllegalStateException(this, key, type, null, "assertion error, unhandled case in custom()");
//    }
//



//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getName() {
//        return this.name;
//    }
//
}
