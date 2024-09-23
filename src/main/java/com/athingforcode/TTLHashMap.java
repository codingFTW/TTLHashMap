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

    public TTLHashMap() {
        this.map = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    public void put(K key, V value, long ttl, TimeUnit unit, BiConsumer<K, V> actionWhenExpired) {
        Objects.requireNonNull(key);
        Entry<K, V> entry = new Entry<>(key, value, ttl, unit);
        map.put(key, entry);
        //if ttl is negative then entry is cosidered non expiring
        if(ttl >= 0) {
            setRemovalSchedule(entry,actionWhenExpired);
        }
    }

    public V get(K key) {
        Entry<K, V> entry = map.get(key);
        if (entry == null || entry.isExpired()) {
            map.remove(key);
            return null;
        }
        return entry.getValue();
    }

    public void remove(K key) {
        map.remove(key);
    }

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

        public Entry(K key, V value, long ttl, TimeUnit unit) {
            this.key = key;
            this.value = value;
            this.ttl = ttl;
            this.ttlUnit = unit;
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

        public boolean isExpired() {
            long now = System.currentTimeMillis();
            long expirationTime = creationTime + ttlUnit.toMillis(ttl);
            return now >= expirationTime;
        }
    }

}
