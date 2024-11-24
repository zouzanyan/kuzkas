package entity;


import com.alibaba.fastjson.JSONArray;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class Cache {

    // TODO 目前是多线程，可增加串行化处理逻辑，保证一致性

    private final Map<String, CacheEntry> cacheMap = new ConcurrentHashMap<>();

    static class CacheEntry {

        private Object value;
        // 失效时间
        private long expireTimestamp;


        public CacheEntry(Object value, long expireTimestamp) {
            this.value = value;
            this.expireTimestamp = expireTimestamp;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }


        public long getExpireTimestamp() {
            return expireTimestamp;
        }

        public void setExpireTimestamp(long expireTime) {
            this.expireTimestamp = expireTime;
        }


    }

    /**
     * 设置键值对，其中包含值、开始时间和过期时间
     *
     * @param key        键，用于标识缓存中的数据
     * @param value      值，存储的数据对象
     * @param expireTime 过期时间，如果大于等于0，则表示从当前时间起数据的有效期（毫秒）,小于0表示永不过期
     */
    public void set(String key, Object value, Long expireTime) {
        long currentTimeMillis = System.currentTimeMillis();
        // 如果过期时间未设置，则默认为-1，表示永不过期
        if (expireTime == null){
            expireTime = -1L;
        }
        if (expireTime > 0) {
            expireTime = currentTimeMillis + expireTime;
        }
        CacheEntry cacheEntry = new CacheEntry(value, expireTime);
        cacheMap.put(key, cacheEntry);
    }

    // setIfAbsent 操作, 如果键不存在,则设置键值对返回true,否则设置失败,返回false
    public boolean setIfAbsent(String key, Object value, Long expireTime) {
        if (expireTime == null){
            expireTime = (long) -1;
        }
        if (expireTime > 0) {
            expireTime = System.currentTimeMillis() + expireTime;
        }
        CacheEntry cacheEntry = new CacheEntry(value, expireTime);
        return cacheMap.putIfAbsent(key, cacheEntry) == null;
    }

    // 从缓存获取值
    public Object get(String key) {
        long currentTimeMillis = System.currentTimeMillis();
        CacheEntry cacheEntry = cacheMap.get(key);
        // 键值对不存在
        if (cacheEntry == null) {
            return null;
        }
        long expireTimestamp = cacheEntry.getExpireTimestamp();
        // ** 惰性删除 **
        if (expireTimestamp >= 0 && expireTimestamp < currentTimeMillis) {
            cacheMap.remove(key);
            return null;
        }
        return cacheEntry.value;
    }

    // 从缓存删除键值对
    public boolean del(String key) {
        return cacheMap.remove(key) != null;
    }

    // 缓存时间修改
    public boolean expire(String key, long expireTime) {
        long currentTimeMillis = System.currentTimeMillis();
        CacheEntry cacheEntry = cacheMap.get(key);
        if (cacheEntry == null) {
            return false;
        }
        if (expireTime > 0) {
            expireTime = currentTimeMillis + expireTime;
        }
        cacheEntry.setExpireTimestamp(expireTime);
        return true;
    }

    // 获取缓存中所有的键值对
    public Map<String, Object> getAllKeyValues() {
        // 主动清理过期键
        clearExpiredKeys();
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, CacheEntry> entry : cacheMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue().getValue();
            result.put(key, value);
        }
        return result;
    }

    // 清理所有过期的键
    public void clearExpiredKeys() {
        long currentTimeMillis = System.currentTimeMillis();
        Iterator<Map.Entry<String, CacheEntry>> iterator = cacheMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CacheEntry> entry = iterator.next();
            CacheEntry cacheEntry = entry.getValue();
            long expireTimestamp = cacheEntry.getExpireTimestamp();
            if (expireTimestamp > 0 && expireTimestamp < currentTimeMillis) {
                iterator.remove();
            }
        }
    }

    // 获取缓存中所有的键
    public Set<String> getAllKeys() {
        return cacheMap.keySet();
    }

    // 向列表中添加数据
    @SuppressWarnings("unchecked")
    public boolean Rpush(String listName, Object value) {
        Object data = get(listName);
        if (data == null) {
            ArrayList<Object> objects = new ArrayList<>();
            objects.add(value);
            set(listName, objects, -1L);
            return true;
        }
        // 已经存在列表了，可能是set进去的(set进去的列表会被 FastJSON deserialize 为JSONArray,##JSONArray extends List##).也可能是lpush进去的
        if (data instanceof List){
            ((List<Object>) data).add(value);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Object Lpop(String listName){
        Object data = get(listName);
        if (data == null) {
            return null;
        }
        // 从左边删除
        if (data instanceof List){
            List<Object> list = (List<Object>) data;
            if (list.size() == 0){
                return null;
            }
            return (list.remove(0));
        }
        return null;
    }
}
