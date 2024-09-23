package com.athingforcode;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TTLHashMap<String, String> map = new TTLHashMap<>();
        map.put("key1", "value1", 5, TimeUnit.SECONDS, null);
        map.put("key2", "value2", 10, TimeUnit.SECONDS, null);

        map.put("key3", "value3", 5, TimeUnit.SECONDS, (key,value) -> System.out.println("Action executed when key-"+ key + " expired, its value is : " + value));
        map.put("key4", "value4", 10, TimeUnit.SECONDS, (key,value) -> System.out.println("Action executed when key-"+ key + " expired, its value is : " + value));

        System.out.println(map.get("key1")); // prints "value1"
        Thread.sleep(12000);
        System.out.println(map.get("key1")); // prints null
        System.out.println(map.get("key2")); // prints null
        map.remove("key2");
        System.out.println(map.get("key2")); // prints "null"

        map.shutdown();
    }
}