package com.athingforcode;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class TTLHashMap<K, V> {
    private final ConcurrentHashMap<K, Entry<K, V>> map;
    private final ScheduledExecutorService scheduler;


    /**
     * Creates a new instance of TTLHashMap.
     * This constructor initializes a new ConcurrentHashMap to store entries and a single-threaded ScheduledExecutorService to manage time-to-live (TTL) expiration.
     */
    public TTLHashMap() {
        this.map = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Shuts down the ScheduledExecutorService used by this TTLHashMap, terminating any pending TTL expiration tasks.
     * This method should be called when the TTLHashMap is no longer needed to prevent memory leaks and ensure proper cleanup.
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * Puts a key-value pair into the map with an optional expiration time.
     *
     * @param key                 the key with which the specified value is to be associated
     * @param value               the value to be associated with the specified key
     * @param ttl                 the time to live for the entry
     * @param unit                the time unit of the ttl argument
     * @param actionWhenExpired   the action to be performed when the entry expires. Pass java.util.function.BiConsumer that receives key and values as its inputs
     * @throws NullPointerException if the key or unit is null
     *
     * @implNote If ttl is negative, the entry is considered non-expiring.
     *           If ttl is zero or positive, a removal schedule is set for the entry.
     */
    public void put(K key, V value, long ttl, TimeUnit unit, BiConsumer<K, V> actionWhenExpired) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(unit);
        Entry<K, V> entry = new Entry<>(key, value, ttl, unit);
        map.put(key, entry);
        /*
            if ttl is negative then entry is considered non expiring
        * */
        if(ttl >= 0) {
            setRemovalSchedule(entry,actionWhenExpired);
        }
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key, or null if the key is not found
     *         or the entry has expired
     *
     * @implNote If the entry associated with the key has expired, it is removed from the map
     *           before returning null.
     */
    public V get(K key) {
        Entry<K, V> entry = map.get(key);
        if (entry == null || entry.isExpired()) {
            map.remove(key);
            return null;
        }
        return entry.getValue();
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key the key whose mapping is to be removed from the map
     */
    public void remove(K key) {
        map.remove(key);
    }

    /**
     * Sets up a schedule for removing an entry after its time-to-live has elapsed.
     *
     * @param entry the entry to be scheduled for removal
     * @param actionWhenExpired the action to be performed when the entry expires
     *
     * @implNote This method uses a scheduler to remove the entry from the map after its TTL.
     *           If an action is provided, it will be executed with the entry's key and value
     *           after removal.
     */
    private void setRemovalSchedule(Entry<K, V> entry, BiConsumer<K, V> actionWhenExpired) {
        scheduler.schedule(() -> {
            map.remove(entry.getKey());
            if(actionWhenExpired != null) {
                actionWhenExpired.accept(entry.getKey(), entry.getValue());
            }
        }, entry.getTTL(), entry.getTtlUnit());
    }

    private static class Entry<K, V> {
        private final K key;
        private final V value;
        private final long ttl;
        private final TimeUnit ttlUnit;
        private final long creationTime;

        /**
         * Creates a new Entry with the specified key, value, time-to-live (TTL), and TTL unit.
         * The creation time of the entry is set to the current system time.
         *
         * @param key the key of the entry
         * @param value the value of the entry
         * @param ttl the time-to-live value of the entry
         * @param ttlUnit the unit of the TTL value (e.g., seconds, minutes, hours)
         */
        public Entry(K key, V value, long ttl, TimeUnit ttlUnit) {
            this.key = key;
            this.value = value;
            this.ttl = ttl;
            this.ttlUnit = ttlUnit;
            this.creationTime = System.currentTimeMillis();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public long getTTL() {
            return ttl;
        }

        public TimeUnit getTtlUnit() {
            return ttlUnit;
        }

        /**
         * Checks if this entry has expired based on its time-to-live (TTL) value.
         * An entry is considered expired if the current time is greater than or equal to the entry's creation time plus its TTL.
         * An entry with negative ttl is never expired
         *
         * @return true if this entry has expired, false otherwise
         */
        public boolean isExpired() {
            if(ttl < 0) {
                return false;
            }
            long now = System.currentTimeMillis();
            long expirationTime = creationTime + ttlUnit.toMillis(ttl);
            return now >= expirationTime;
        }
    }

}
