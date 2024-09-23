package com.athingforcode;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TTLHashMap<String, String> map = new TTLHashMap<>();
        map.put("key1", "value1", 5, TimeUnit.SECONDS);
        map.put("key2", "value2", 10, TimeUnit.SECONDS);

        System.out.println(map.get("key1")); // prints "value1"
        Thread.sleep(6000);
        System.out.println(map.get("key1")); // prints null
        System.out.println(map.get("key2")); // prints "value2"
        map.remove("key2");
        System.out.println(map.get("key2")); // prints "null"
    }
}