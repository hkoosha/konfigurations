## Java Configuration Library [![Build Status](https://travis-ci.com/hkoosha/konfigurations.svg?branch=master)](https://travis-ci.com/hkoosha/konfigurations.svg?branch=master)

Simple, small and extendable zero dependency configuration management library with live updates.

## Maven

```xml
<dependency>
    <groupId>io.koosha.konfigurations</groupId>
    <artifactId>konfigurations</artifactId>
    <version>9.0.0</version>
</dependency>
```

```groovy
compile group: 'io.koosha.konfigurations', name: 'konfigurations', version: '9.0.0'
```

Development happens on the master branch. Releases are tagged. 


## Project goals:

- Lightweight, small, easy.
- Supporting multiple configuration sources.
- Live updates, with possibility of removing old keys or adding new ones.
- Possibility of registering to configuration changes, per key or generally.
- Configuration namespace management.

## Usage:

**Overrides**: Konfiguration sources can override each other. The first source 
which contains the requested key is selected and takes precedence.

**Default values**: For non-existing keys, default values can be passed to 
`v(DEFAULT_VALUE)`

**List, Map, Custom Type**: As long as the underlying source can parse it from 
the actual configuration source, it's possible. The ExtJacksonJsonSource uses 
Jackson for parsing json, so if the Jackson parser can parse the map / list /
object, so can the configuration. The same is true for ExtYamlSource which
uses snakeyaml. You could also implement your own source.

**Observing changes**: Observers can register to changes of a specific key 
(`register(KeyObserver` on the `V` interface) or to any configuration change 
(`register(EverythingObserver)` on the `Konfiguration` interface). 

**Observing multiple keys** from a single listener is possible, and the 
observer will be notified once for each updated key in each update. 

**De-Registering** an observer: is possible but not necessary as no hard 
reference is kept to listeners (GC works as expected).

```java
KonfigurationFactory f    = KonfigurationFactory.getInstance();

// Create as many sources as necessary
Konfiguration        json = f.jacksonJson ("myJsonSource", "some_valid_json...");
Konfiguration        yaml = f.snakeYaml   ("myYamlSource", () -> "some_valid_yaml_provider");
Konfiguration        mem  = f.map         ("myMapSource",  () -> Map.of(foo, bar, baz, quo));
Konfiguration        net  = f.snakeYaml   ("fromNetwork",  () -> NetworkUtil.httpGet("http://example.com/endpoint/config.yaml?token=hahaha"));

// Kombine them (json takes priority over yaml and yaml over mem, and mem over net).
Konfiguration konfig = f.konbine(json, yaml, mem, net);

// Get the value, notice the .v()
boolean b = konfig.bool   ("some.konfig.key.deeply.nested").v()
int     i = konfig.int_   ("some.int").v()
long    l = konfig.long_  ("some.long").v()
String  s = konfig.string ("aString").v()
double  d = konfig.double_("double").v()

List<Invoice>       list = konfig.list("a.nice.string.list", Invoice.class).v()

// --------------

K<Integer> maybe = konfig.int_("might.be.unavailable");
int value = maybe.v(42);
assert value == 42;

// Sometime later, "non.existing.key = 99" is actually written into config source,
// and konfig.update() is called in the worker thread.

K<Integer> defValue = konfig.int_("non.existing.key");
int value = defValue.v(42);
assert value == 99;

```

### Live updates

Value can be updated during runtime. A konfiguration instance does not return
a value directly but returns a wrapper. The wrapper has a method v() which
returns the actual value.

```java
String theJsonKonfigString = "{ \"isAllowed\": true }";
KonfigurationFactory f = KonfigurationFactory.getInstance();
Konfiguration konfig = f.jacksonJson("mySource", () -> this.theJsonKonfigString);

// ...

K<Boolean> amIAllowed = konfig.bool("isAllowed");
amIAllowed.register(updatedKey -> {
    System.out.println("Hey! we're updated:: " + konfig.bool(updatedKey)));
    System.out.println("Also accessible from: " + amIAllowed.v());
});

assert amIAllowed.v() == true;

this.theJsonKonfigString = "{ \"isAllowed\": false }";
konfig.update(); 

// Changed!
assert amIAllowed.v() == false;

```


### Lite version:
There's a lite version, currently only for jackson source. The lite version
does not support:

- Kombining
- Registering to updates
- Live updates

But instead supports writing and modifying:

```java
LiteKonfiguration konfig = new ExtJacksonJsonLiteSource("my_source", "{}");
konfig.put("some.nested.key", 99L);
// Here serialize will be <"some": { "nested": { "key": {99}}}>
final String serialize = konfig.serialize();
```

### Assumptions / Limitations:
 - First source containing a key takes priority over others.

 - A value returned from K.v() must be immutable. Care must be taken
   when using custom types.

 - K.v() will return new values if the konfiguration source is updated,
   while konfig key observers are still waiting to be notified (are not 
   notified *yet*).

 - Currently, custom types and ALL the required fields corresponding to those
   read from json string, MUST be public. (This is actually a jackson limitation  
   (feature?) as it won't find private, protected and package local fields AND 
   classes (important: both class and fields, must be public)). This affects 
   list() and custom() methods.

 - TODO: Do not call isUpdatable on a source twice.

 - TODO: Many more unit tests
