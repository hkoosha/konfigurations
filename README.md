## Java Configuration Library [![Build Status](https://travis-ci.org/hkoosha/konfigurations.svg?branch=master)](https://travis-ci.org/hkoosha/konfigurations)

Simple, small and extendable configuration management library with live updates.

## Maven

```xml
<dependency>
    <groupId>io.koosha.konfigurations</groupId>
    <artifactId>konfigurations</artifactId>
    <version>5.0.0</version>
</dependency>
```

```groovy
compile group: 'io.koosha.konfigurations', name: 'konfigurations', version: '5.0.0'
```

Development happens on master branch. Releases (versions) are tagged. 


## Project goals:

- Lightweight, small, easy.
- Supporting multiple configuration sources.
- Live updates, with possibility of removing old keys or adding new ones.
- Possibility of registering to configuration changes, per key or generally.
- Configuration namespace management.
- Hopefully support Android.

## Usage:

**Overrides**: Konfiguration sources can override each other. The first source 
which contains the requested key is selected and takes precendece.

**Default values**: For non-existing keys, default values can be passed to 
`v(DEFAULT_VALUE)`

**List, Map, Custom Type**: As long as the underlying source can parse it from 
the actual configuration source, it's possible. The JsonKonfigSource uses 
Jackson for parsing json, so if the Jackson parser can parse the map / list /
object, so can the configuration.

**Observing changes**: Observers can register to changes of a specific key 
(`register(KeyObserver` on the `V` interface) or to any configuration change 
(`register(EverythingObserver)` on the `Konfiguration` interface). 

**Observing multiple keys** from a single listener is possible, and the 
observer will be notified once for each updated key in each update. 

**De-Registering** an observer: is possible but not necessary as no hard 
reference is kept to listeners (GC works as expected).

```java
// Create as many sources as necessary
JsonKonfigSource     json = new JsonKonfigSource    (() -> Files.read("/etc/my_app.cfg"));
InMemoryKonfigSource map0 = new InMemoryKonfigSource(() -> Map.of(foo, bar, baz, quo));
InMemoryKonfigSource map1 = new InMemoryKonfigSource(() -> NetworkUtil.decodeAsMap("http://example.com/endpoint/config?token=hahaha"));

// Kombine them (map0 takes priority over json and json over map1).
Konfiguration konfig = new KonfigurationKombiner(map0, json, map1);

// Get the value, notice the .v()
boolean b = konfig.bool   ("some.konfig.key.deeply.nested").v()
int     i = konfig.int_   ("some.int").v()
long    l = konfig.long_  ("some.long").v()
String  s = konfig.string ("aString").v()
double  d = konfig.double_("double").v()

List<Invoice>       list = konfig.list("a.nice.string.list", Invoice.class).v()
Map<String, Integer> map = konfig.map ("my.map", int.class).v()

// --------------

K<Integer> maybe = konfig.int_("might.be.unavailable");
int value = maybe.v(42);
assert value == 42;

// Sometime later, "non.existing.key" is actually written into config source,
// and konfig.update() is called in the worker thread.

K<Integer> defValue = konfig.int_("non.existing.key");
int value = defValue.v(42);
assert value == 42;


```

### Live updates

Value can be updated during runtime. A konfiguration instance does not return
a value directly but returns a wrapper. The wrapper has a method v() which
returns the actual value.

```java
private static String theJsonKonfigString = "{ \"isAllowed\": true }";
private static Konfiguration konfig =
    new KonfigurationKombiner(new JsonKonfigSource(() -> theJsonKonfigString));;

// ...

K<Boolean> amIAllowed = konfig.bool("isAllowed");
amIAllowed.register(updatedKey -> {
    System.out.println("Hey! we're updated:: " + konfig.bool(updatedKey)));
    System.out.println("Also accessible from: " + amIAllowed.v());
});

assert amIAllowed.v() == true;

theJsonKonfigString = "{ \"isAllowed\": false }";
konfig.update(); 

// Changed!
assert amIAllowed.v() == false;

```

### Assumptions / Limitations:
 - First source containing a key takes priority over others.

 - First call to get a konfig value, defines type for it's key. further calls
   must comply.

 - A value returned from K.v() must be immutable. Care must be taken
   when using custom types.

 - Every object returned by konfiguration, including custom types, MUST 
   implement equals(), otherwise calls to update() will not work properly/

 - KonvigV.v() will return new values if the konfiguration source is updated,
   while konfig key observers are still waiting to be notified (are not 
   notified *yet*).

 - Currently, custom types and ALL the required fields corresponding to those
   read from json string, MUST be public. (This is actually a jackson limitation  
   (feature?) as it wont find private, protected and package local fields AND 
   classes (important: both class and fields, must be public)). This affects 
   list(), map() and custom() methods.

 - Changing a key type (such as from int to String) during an update is not 
   recommended and might cause unexpected behaviour.
   TODO: how to disallow this?

 - TODO: Do not call isUpdatable on a source twice.

 - TODO: Many more unit tests
