/*
 * Copyright (C) 2019 Koosha Hosseiny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.pargar.konfiguration;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static net.pargar.konfiguration.TypeName.*;


/**
 * Reads konfig from a json source (supplied as string).
 *
 * <p>for {@link #custom(String, Class)} to work, the supplied json reader must
 * be configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe and immutable.
 */
public final class JsonKonfigSource implements KonfigSource {

    private final Supplier<ObjectMapper> mapperSupplier;
    private final Supplier<String> json;
    private int lastHash;
    private JsonNode root;


    private JsonNode node_(final String key) {
        if (key == null || key.isEmpty())
            throw new KonfigurationMissingKeyException("empty konfig key");

        final String k = "/" + key.replace('.', '/');
        return this.root.at(k);
    }

    private JsonNode node(final String key) {
        final JsonNode node = node_(key);

        if (node.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        return node;
    }


    private static ObjectMapper defaultObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    private static void checkType(final boolean isOk, final TypeName required, final JsonNode node, final String key) {
        if (isOk)
            return;

        throw new KonfigurationTypeException(required.getTName(), node.getNodeType().toString(), key);
    }


    /**
     * Wraps the provided json string in a {@link Supplier} and calls
     * {@link #JsonKonfigSource(Supplier)}.
     *
     * @param json
     *         constant json string as backing storage.
     */
    @SuppressWarnings("unused")
    public JsonKonfigSource(final String json) {
        this(() -> json);
    }

    /**
     * Calls {@link #JsonKonfigSource(Supplier, Supplier)} with a default
     * Object mapper provided by {@link #defaultObjectMapper()}.
     *
     * @param json
     *         constant json string as backing storage.
     */
    @SuppressWarnings("WeakerAccess")
    public JsonKonfigSource(final Supplier<String> json) {
        this(json, new Supplier<ObjectMapper>() {
            private final ObjectMapper mapper = defaultObjectMapper();

            @Override
            public ObjectMapper get() {
                return mapper;
            }
        });
    }

    /**
     * Creates a {@link JsonKonfigSource} with the given json provider and
     * object mapper provider.
     *
     * @param json
     *         backing store provider. Must always return a non-null valid json
     *         string.
     * @param objectMapper
     *         {@link ObjectMapper} provider. Must always return a valid
     *         non-null ObjectMapper, and if required, it must be able to
     *         deserialize custom types, so that {@link #custom(String, Class)}
     *         works as well.
     *
     * @throws NullPointerException
     *         if any of its arguments are null.
     * @throws KonfigurationSourceException
     *         if jackson library is not in the classpath. it specifically looks
     *         for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KonfigurationSourceException
     *         if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException
     *         if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException
     *         if the the root element returned by jackson is null.
     */
    @SuppressWarnings("WeakerAccess")
    public JsonKonfigSource(final Supplier<String> json, final Supplier<ObjectMapper> objectMapper) {
        Objects.requireNonNull(json, "jsonSupplier");
        Objects.requireNonNull(objectMapper, "objectMapperSupplier");
        // Check early, so we're not fooled with a dummy object reader.
        try {
            Class.forName("com.fasterxml.jackson.databind.JsonNode");
        }
        catch (final ClassNotFoundException e) {
            throw new KonfigurationSourceException(getClass().getName() + " requires " + "jackson library to be present in the class path",
                                                   e);
        }

        this.json = json;
        this.mapperSupplier = objectMapper;

        final String newJson = this.json.get();
        if (newJson == null)
            throw new KonfigurationSourceException("storage is null");

        final JsonNode update;
        try {
            update = this.mapperSupplier.get().readTree(newJson);
        }
        catch (final IOException e) {
            throw new KonfigurationSourceException("error parsing json string", e);
        }

        if (update == null)
            throw new KonfigurationSourceException("root element is null");

        this.root = update;
        this.lastHash = newJson.hashCode();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean bool(final String key) {
        final JsonNode at = node(key);
        checkType(at.isBoolean(), BOOLEAN, at, key);
        return at.asBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer int_(final String key) {
        final JsonNode at = node(key);
        checkType(at.isInt(), INT, at, key);
        return at.asInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long long_(final String key) {
        final JsonNode at = node(key);

        if (at.isInt())
            return (long) at.asInt();
        else if (at.isLong())
            return at.asLong();

        //noinspection ConstantConditions
        checkType(false, LONG, at, key);
        throw new AssertionError("?!!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double double_(final String key) {
        final JsonNode at = node(key);
        checkType(at.isDouble(), DOUBLE, at, key);
        return at.asDouble();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String string(final String key) {
        final JsonNode at = node(key);

        if (at.isArray()) {
            final StringBuilder sb = new StringBuilder();
            for (final JsonNode jsonNode : at) {
                checkType(jsonNode.isTextual(), STRING_ARRAY, at, key);
                sb.append(jsonNode.textValue());
            }
            return sb.toString();
        } else if (at.isTextual()) {
            return at.asText();
        }

        //noinspection ConstantConditions
        checkType(false, STRING, at, key);
        throw new AssertionError("?!!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> list(final String key, final Class<T> el) {
        final JsonNode at = node(key);
        checkType(at.isArray(), LIST, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, el);

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a list of [" + el.getCanonicalName() + "]",
                                                 e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<String, T> map(final String key, final Class<T> el) {
        final JsonNode at = node(key);
        checkType(at.isObject(), MAP, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final MapType javaType = reader.getTypeFactory().constructMapType(Map.class, String.class, el);

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a map of [" + el.getCanonicalName() + "]",
                                                 e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<T> set(final String key, final Class<T> el) {
        final JsonNode at = node(key);
        checkType(at.isArray(), SET, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, el);

        final List<T> l;
        try {
            l = reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a set of [" + el.getCanonicalName() + "]",
                                                 e);
        }

        return new HashSet<>(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T custom(final String key, final Class<T> el) {
        final ObjectMapper reader = this.mapperSupplier.get();
        final JsonParser traverse = this.node(key).traverse();

        try {
            return reader.readValue(traverse, el);
        }
        catch (final IOException e) {
            throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a custom type [" + el.getCanonicalName() + "]",
                                                 e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String key) {
        return !this.node_(key).isMissingNode();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdatable() {
        String newJson = this.json.get();

        if (newJson == null)
            return false;

        final int newHash = newJson.hashCode();
        return newHash != this.lastHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KonfigSource copyAndUpdate() {
        return new JsonKonfigSource(this.json, this.mapperSupplier);
    }

}
