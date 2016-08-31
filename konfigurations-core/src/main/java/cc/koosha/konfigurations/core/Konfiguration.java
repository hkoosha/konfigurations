package cc.koosha.konfigurations.core;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface Konfiguration {

    KonfigV<Boolean> bool(String key);

    KonfigV<Integer> int_(String key);

    KonfigV<Long> long_(String key);

    KonfigV<String> string(String key);


    <T> KonfigV<List<T>> list(String key, Class<T> type);

    <T> KonfigV<Map<String, T>> map(String key, Class<T> type);

    <T> KonfigV<Set<T>> set(String key, Class<T> type);

    <T> KonfigV<T> custom(String key, Class<T> type);


    boolean update();

    Konfiguration subset(String key);

    Konfiguration parent();

}
