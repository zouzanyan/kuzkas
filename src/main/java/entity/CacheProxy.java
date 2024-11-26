package entity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// 对Cache提供增强的功能
public class CacheProxy implements ICache{

    private final Cache cache = new Cache();

    // 记录写操作的执行次数
//    private final AtomicLong writeCount = new AtomicLong();
    public Map<String, Cache.CacheEntry> getCacheMap() {
        return cache.getCacheMap();
    }

    public void setCacheMap(Map<String, Cache.CacheEntry> cacheMap) {
        cache.setCacheMap(cacheMap);
    }

    @Override
    public void set(String key, Object value, Long expireTime) {
//        writeCount.incrementAndGet();
        cache.set(key, value, expireTime);
    }

    @Override
    public boolean setIfAbsent(String key, Object value, Long expireTime) {

        return cache.setIfAbsent(key, value, expireTime);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public boolean del(String key) {

        return cache.del(key);
    }

    @Override
    public boolean expire(String key, long expireTime) {
        return cache.expire(key, expireTime);
    }

    @Override
    public Map<String, Object> getAllKeyValues() {
        return cache.getAllKeyValues();
    }

    @Override
    public Set<String> getAllKeys() {
        return cache.getAllKeys();
    }

    @Override
    public boolean rpush(String listName, Object value) {
        return cache.rpush(listName, value);
    }

    @Override
    public boolean lpush(String listName, Object value) {
        return cache.lpush(listName, value);
    }

    @Override
    public Object rpop(String listName) {
        return cache.rpop(listName);
    }

    @Override
    public Object lpop(String listName) {
        return cache.lpop(listName);
    }

    @Override
    public Integer llen(String listName) {
        return cache.llen(listName);
    }

    @Override
    public List<Object> lrange(String listName, Integer start, Integer end) throws Exception {
        return cache.lrange(listName, start, end);
    }

    @Override
    public int size() {
        return cache.size();
    }



}