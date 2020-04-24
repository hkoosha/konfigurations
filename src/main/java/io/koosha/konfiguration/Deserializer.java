package io.koosha.konfiguration;

import io.koosha.konfiguration.type.Kind;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

@ThreadSafe
@FunctionalInterface
public interface Deserializer {

    <T> T deserialize(@NotNull byte[] bytes,
                      @NotNull Kind<T> kind);

}
