package io.koosha.konfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.impl.v8.LiteFactoryV8;
import io.koosha.konfiguration.type.Kind;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface LiteKonfigurationFactory {

    @Contract(pure = true)
    @NotNull
    static LiteKonfigurationFactory getInstance() {
        return LiteFactoryV8.getInstanceV8();
    }

    /**
     * Creates a {@link Konfiguration} with the given json string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Kind}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param name name of created konfiguration.
     * @param json backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    LiteKonfiguration jacksonJson(@NotNull String name,
                                  @NotNull String json);

    /**
     * Creates a {@link Konfiguration} with the given json string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Kind}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param name         name of created konfiguration.
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Kind)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    LiteKonfiguration jacksonJson(@NotNull String name,
                                  @NotNull String json,
                                  @NotNull Supplier<ObjectMapper> objectMapper);

}
