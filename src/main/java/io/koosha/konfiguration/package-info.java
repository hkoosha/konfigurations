/**
 * Entry point is at {@link io.koosha.konfiguration.KonfigurationFactory#getInstance(String)}
 * or the versioned alternatives such as {@link io.koosha.konfiguration.KonfigurationFactory#getInstanceV8()}.
 * <p>
 * For extending the konfiguration library for connecting new sources, look at the
 * {@link io.koosha.konfiguration.impl.v8.FactoryV0} <em>BUT DO NOT USE THAT CLASS</em> or any class
 * in that package directly, they are internal implementation and subject to change even in minor
 * releases.
 * <p>
 * Generally for a new source, you can take {@code io.koosha.konfiguration.impl.v0.ExtJacksonJsonSource}
 * or any of {@code io.koosha.konfiguration.impl.v0.Ext*} classes as an example, and later wrap them in a
 * {@link io.koosha.konfiguration.Konfiguration} just as
 * {@link io.koosha.konfiguration.impl.v8.FactoryV0#jacksonJson(java.lang.String, java.lang.String)}
 * does.
 */
package io.koosha.konfiguration;