package entity;

import org.junit.jupiter.api.Test;
import singleton.CacheSingleton;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {


    @Test
    public void testSet() throws Exception{
        CacheProxy cache = CacheSingleton.getInstance();
        cache.set("test", "test1", 1000L);
        Thread.sleep(1000);
        assertEquals("test1", cache.get("test"));
    }

    @Test
    public void testGetAllKeys() throws Exception{
        CacheProxy cache = CacheSingleton.getInstance();
        cache.set("test1", "test1", 1000L);
        cache.set("test2", "test2", 1000L);
        cache.set("test3", "test3", 1000L);
        System.out.println(cache.getAllKeyValues());
        System.out.println(cache.getAllKeys());
    }

}