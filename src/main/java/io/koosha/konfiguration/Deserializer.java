package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

@ThreadSafe
@FunctionalInterface
public interface Deserializer {

    <T> T deserialize(@NotNull byte[] bytes, @NotNull Q<T> q);

}
