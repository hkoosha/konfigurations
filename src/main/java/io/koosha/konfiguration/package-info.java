/**
 * <h1>Entry Point</h1>
 * Entry point is at {@link io.koosha.konfiguration.Konfiguration#kFactory()}
 * or the versioned alternatives such as
 * {@link io.koosha.konfiguration.KonfigurationFactory#getInstanceV8()}.
 *
 * <h1>Usage</h1>
 * Kombine multiple sources such as plain java map, json file using jackson,
 * a yaml file using simple yaml library and any other source type by providing
 * your own source type. Then, you can use get value methods from the kombiner.
 *
 * <h1>Extending</h1>
 * For extending the konfiguration library / connecting new sources, look at
 * the * {@link io.koosha.konfiguration.impl.v8.FactoryV8} <em>BUT DO NOT USE
 * THAT CLASS</em> or any class * in that package directly, they are internal
 * implementation and subject to change even in minor releases.
 * <p>
 * Generally for a new source, you can take
 * {@code io.koosha.konfiguration.impl.v0.ExtJacksonJsonSource} or any of
 * {@code io.koosha.konfiguration.impl.v0.Ext*} classes as an example, and
 * later wrap them in a {@link io.koosha.konfiguration.Konfiguration} just as
 * {@link io.koosha.konfiguration.impl.v8.FactoryV8#jacksonJson(java.lang.String, java.lang.String)}
 * does.
 */
package io.koosha.konfiguration;