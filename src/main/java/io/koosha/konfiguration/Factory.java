package io.koosha.konfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

@SuppressWarnings("unused")
@ApiStatus.AvailableSince(Factory.VERSION_8)
public interface Factory {

    public static final String VERSION_1 = "1.0.0";

    public static final String VERSION_8 = "8.0.0";

    public static final String VERSION = VERSION_8;

    long LOCK_WAIT_MILLIS__DEFAULT = 300;
    AtomicBoolean UNSAFE_YAML = new AtomicBoolean(true);

    /**
     * Implementation version.
     *
     * @return implementation version.
     */
    @Contract(pure = true)
    @NotNull String getVersion();

    // =========================================================================

    @Contract("_ -> new")
    @NotNull
    KonfigurationBuilder builder(@NotNull final String name);

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    @Contract("_, _ -> new")
    @NotNull
    Konfiguration kombine(@NotNull String name,
                          @NotNull Konfiguration k0);

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0      first source
     * @param sources rest of sources
     * @return kombined sources.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    Konfiguration kombine(@NotNull String name,
                          @NotNull Konfiguration k0,
                          @NotNull Konfiguration... sources);

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param sources sources to combine.
     * @return kombined sources.
     * @throws NullPointerException     if sources is null.
     * @throws KfgIllegalStateException is sources is empty.
     */
    @NotNull
    @Contract("_, _ -> new")
    Konfiguration kombine(@NotNull String name,
                          @NotNull Collection<Konfiguration> sources);

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    @Contract("_ -> new")
    Konfiguration kombine(@NotNull Konfiguration k0);

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0      first source
     * @param sources rest of sources
     * @return kombined sources.
     */
    @Contract("_, _ -> new")
    Konfiguration kombine(@NotNull Konfiguration k0,
                          @NotNull Konfiguration... sources);

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param sources sources to combine.
     * @return kombined sources.
     * @throws NullPointerException     if sources is null.
     * @throws KfgIllegalStateException is sources is empty.
     */
    @Contract("_ -> new")
    Konfiguration kombine(@NotNull Collection<Konfiguration> sources);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     * <p>
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    Konfiguration map(@NotNull String name,
                      @NotNull Supplier<Map<String, ?>> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    Konfiguration map(@NotNull String name,
                      @NotNull Map<String, ?> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    Konfiguration map_(@NotNull Map<String, ?> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     * <p>
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    Konfiguration map_(@NotNull Supplier<Map<String, ?>> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     * <p>
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    Konfiguration mapWithNested(@NotNull String name,
                                @NotNull Supplier<Map<String, ?>> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    Konfiguration mapWithNested(@NotNull String name,
                                @NotNull Map<String, ?> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    Konfiguration mapWithNested_(@NotNull Map<String, ?> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     * <p>
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    Konfiguration mapWithNested_(@NotNull Supplier<Map<String, ?>> storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract("_ -> new")
    Konfiguration preferences_(@NotNull Preferences storage);

    @NotNull
    @Contract("_, _ -> new")
    Konfiguration preferences(@NotNull String name,
                              @NotNull Preferences storage);

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract("_, _ -> new")
    Konfiguration preferences_(@NotNull Preferences storage,
                               @NotNull Deserializer deser);

    @NotNull
    @Contract("_, _, _ -> new")
    Konfiguration preferences(@NotNull String name,
                              @NotNull Preferences storage,
                              @NotNull Deserializer deser);

    /**
     * Creates a {@link Konfiguration} with the given json provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * @param json backing store provider. Must always return a
     *             non-null valid json string.
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
    Konfiguration jacksonJson(@NotNull String name,
                              @NotNull Supplier<String> json);

    /**
     * Creates a {@link Konfiguration} with the given json string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
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
    @Contract("_ -> new")
    Konfiguration jacksonJson_(@NotNull String json);

    /**
     * Creates a {@link Konfiguration} with the given json string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
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
    Konfiguration jacksonJson_(@NotNull String json,
                               @NotNull Supplier<ObjectMapper> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given json provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * @param json backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @Contract("_ -> new")
    Konfiguration jacksonJson_(@NotNull Supplier<String> json);

    /**
     * Creates a {@link Konfiguration} with the given json provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
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
    Konfiguration jacksonJson_(@NotNull Supplier<String> json,
                               @NotNull Supplier<ObjectMapper> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given json string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
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
    Konfiguration jacksonJson(@NotNull String name,
                              @NotNull String json);

    /**
     * Creates a {@link Konfiguration} with the given json string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
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
    Konfiguration jacksonJson(@NotNull String name,
                              @NotNull String json,
                              @NotNull Supplier<ObjectMapper> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given json provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
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
    Konfiguration jacksonJson(@NotNull String name,
                              @NotNull Supplier<String> json,
                              @NotNull Supplier<ObjectMapper> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given yaml string as source.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * @param yaml backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if snake yaml library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_ -> new")
    Konfiguration snakeYaml_(@NotNull String yaml);

    /**
     * Creates a {@link Konfiguration} with the given yaml string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    Konfiguration snakeYaml_(@NotNull String yaml,
                             @NotNull Supplier<Yaml> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_ -> new")
    Konfiguration snakeYaml_(@NotNull Supplier<String> yaml);

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    Konfiguration snakeYaml_(@NotNull Supplier<String> yaml,
                             @NotNull Supplier<Yaml> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given yaml string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if snake yaml library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    Konfiguration snakeYaml(@NotNull String name,
                            @NotNull String yaml);

    /**
     * Creates a {@link Konfiguration} with the given yaml string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    Konfiguration snakeYaml(@NotNull String name,
                            @NotNull String yaml,
                            @NotNull Supplier<Yaml> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store provider. Must always return a
     *             non-null valid json string.
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
    Konfiguration snakeYaml(@NotNull String name,
                            @NotNull Supplier<String> yaml);

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    Konfiguration snakeYaml(@NotNull String name,
                            @NotNull Supplier<String> yaml,
                            @NotNull Supplier<Yaml> objectMapper);

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    Konfiguration snakeYaml_Unsafe(@NotNull String name,
                                   @NotNull Supplier<String> yaml);

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    Konfiguration snakeYaml_Unsafe(@NotNull String name,
                                   @NotNull Supplier<String> yaml,
                                   @NotNull Supplier<Yaml> objectMapper);
}
