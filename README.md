# TTLHashMap

TTLHashMap is a Java library that provides a thread-safe hash map implementation with support for time-to-live (TTL) entries. This data structure is useful for caching scenarios where you want entries to automatically expire after a specified duration.

## Features

- Thread-safe operations using `ConcurrentHashMap`
- Configurable time-to-live (TTL) for entries
- Automatic removal of expired entries
- Support for custom actions when entries expire
- Flexible TTL units (seconds, minutes, hours, etc.)

## Requirements
Use Java 8+

## Usage
Here's a quick example of how to use TTLHashMap:

```java
import com.athingforcode.TTLHashMap;
import java.util.concurrent.TimeUnit;

public class Example {
    public static void main(String[] args) {
        TTLHashMap<String, String> map = new TTLHashMap<>();

        // Add an entry that expires after 5 seconds
        map.put("key1", "value1", 5, TimeUnit.SECONDS, (k, v) -> System.out.println("Expired: " + k + " = " + v));

        // Add a non-expiring entry
        map.put("key2", "value2", -1, TimeUnit.SECONDS, null);

        // Retrieve a value
        String value = map.get("key1");

        // Remove an entry
        map.remove("key2");

        // Don't forget to shut down the map when you're done
        map.shutdown();
    }
}
```

## API Reference

### `TTLHashMap<K, V>`

- `TTLHashMap()`: Constructs a new TTLHashMap.
- `void put(K key, V value, long ttl, TimeUnit unit, BiConsumer<K, V> actionWhenExpired)`: Puts a key-value pair into the map with an optional expiration time and expiration action.
- `V get(K key)`: Retrieves the value associated with the specified key.
- `void remove(K key)`: Removes the mapping for the specified key from this map if present.
- `void shutdown()`: Shuts down the TTLHashMap and its scheduler.

## License

Apache License 2.0


