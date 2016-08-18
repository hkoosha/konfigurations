
## Java Configuration Library

Similar to apache commons configuration but strives to be simple and have as
little exteranl dependencies as possible.

Example usage:

```java

// Plain 'ol constructor, create a new konfiguration:
Konfiguration konfig = new JsonKonfiguration("{...JSON STRING...}");

// Get the value, notice the .v()
boolean b = konfig.bool   ("some.konfig.key.deeply.nested").v()
int     i = konfig.int_   ("some.int").v()
long    l = konfig.long_  ("some.long").v()
String  s = konfig.string ("aString").v()

List<String>         list = konfig.list("a.nice.string.list", String.class).v()
Map<String, Integer> map  = konfig.map ("my.map", int.class).v()

```


### Kombiner

A JsonKonfiguration does not do much on it's own (or any other 
<b>konfiguration source</b>). Fun things happen when you put them in a 
KonfigurationKombiner. You can subscribe to value changes, or mix multiple
configuration sources.


### Overrides

When having multiple sources of configuration, say one from local disk and
one read from some web URL, they can override each others values. The first
configuration source has the highest priority.

If a configuration key is removed from one source and added to another source
(moved between different sources) the Kombiner will find out and set the values 
properly and accordingly.

```json
{
   "only": {
     "local": "hello",
   },
   "myName": "I'm local",
}
{
   "only": {
     "web": "goodbye",
   }, 
   "myName": "I'm web",
}
```

```java
Konfiguration local  = new JsonKonfiguration(stringFromDisk);
Konfiguration web    = new JsonKonfiguration(stringFromWeb);
Konfiguration konfig = new KonfigurationKombiner(local, web); // Kombine!

assert "hello" == konfig.string("only.local").v(); // only in local
assert "goodbye" == konfig.string("only.web").v(); // only in web
assert "I'm local" == konfig.string("myName").v(); // local takaes priority

```

### Live updates

Konfiguration value can be updated during runtime. A konfiguration instance
does not return a value directly but returns a wrapper. The wrapper has a method
v() which returns the actual value. 
A simple source such as JsonKonfiguration does not support registering and cool
stuff, but when it's wrapped in a KonfigurationKombiner, all sort of thing are
possible.

```java

public class KonfigDemo {

    static String theJsonKonfigString; // From disk, classpath, web or ...
    static Konfiguration konfig;

    private static void setup() {
        theJsonKonfigString = "{ \"isAllowed\": true }";
        Konfiguration _jsonKonfig = new JsonKonfiguration(() -> theJsonKonfigString);
        konfig = new KonfigurationKombiner(_jsonKonfig);
    }
    
    // Update the configuration. You would want to do the updaing and 
    // calling the update() on Kombiner periodrically in a separate thread.
    // The string supplier we gave to JsonKonfiguration, will read this
    // new string and update everything accordingly.
    private static void updateConfig() {
        theJsonKonfigString = "{ \"isAllowed\": false }";
        konfig.update(); // Must be called!
    }

    public static void main(final String[] args) {

        setup();

        // Initial value, true as set above in the string.
        KonfigV<Boolean> amIAllowed = konfig.bool("isAllowed");
        assert amIAllowed.v();

        // Get notified when the key <something> changes.
        amIAllowed.register(updatedKey -> {
            System.out.println("\n Hey! the key <something> was updated!";
            boolean newSettings = konfig.bool(updatedKey);
            System.out.println("\n Now it is: " + newSettings);
            // also possible: boolean newSettings = amIAllowed.v();
        });

        updateConfig();

        // By now, the System.out.println(...) thing we wrote above is also called.
        assert !amIAllowed.v();
    }
}

```


### Lists and Maps:

Getting a list or map is possible, as long as the underlying source can parse
it from the actual configuration. The JsonKonfiguration uses Jackson for 
parsing json, so if the Jackson parser can parse the map / list, so can the 
configuration.

```java

Konfiguration konfig = new JsonKonfiguration(...);

List<Integer> ints = konfig.list("some.int.array", Integer.class);
List<String> strings = konfig.list("array.of.string", String.class);
```

### Custom types:

todo

### JsonKonfiguration notes

a String can span multiple lines if it's declared in an array

```json
{
    "someLongStr": [
        "I'm ",
        "very "
        "multi "
        "line."
    ]
}
```

```java
Konfiguration konfig = new JsonKonfiguration(...); // or put in a Kombiner
assert "I'm very multi line." == konfig.string("someLongStr");
```


### Assumptions / Limitaions:

 - Updates of configuration sources, and calling the update() method on a 
   Konfiguration,MUST, MUST, MUST take place in a single thread. 
   Also, they should not be too frequesnt (too frequent as in constantly 
   calling update() in a while loop with no delay. A few milli-seconds would do).
 
 - Currently, custom types and ALL the required fields corresponding to those 
   read from json string, MUST be public. (jackson wont find private, protected
   and package local fields AND classes (important: both class and fields, must
   be public)). This affects list(), map() and custom() methods.
