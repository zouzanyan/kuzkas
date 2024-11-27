package entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

// 对Cache提供增强的功能
public class Cache implements ICache{

    private final CacheCore cacheCore = new CacheCore();

    // 最后一次写入时间戳
    private long lastExecWriteTime;

    public Cache() {
    }

    public long getLastExecWriteTime() {
        return lastExecWriteTime;
    }

    public void setLastExecWriteTime(long lastExecWriteTime) {
        this.lastExecWriteTime = lastExecWriteTime;
    }

    public void updateLastExecWriteTime() {
        this.lastExecWriteTime = System.currentTimeMillis();
    }

    public void clearExpiredKeys() {
        cacheCore.clearExpiredKeys();
    }

    @Override
    public void set(String key, Object value, Long expireTime) {
        updateLastExecWriteTime();
        cacheCore.set(key, value, expireTime);
    }

    @Override
    public boolean setIfAbsent(String key, Object value, Long expireTime) {
        boolean b = cacheCore.setIfAbsent(key, value, expireTime);
        if (b){
            updateLastExecWriteTime();
        }
        return b;
    }

    @Override
    public Object get(String key) {
        return cacheCore.get(key);
    }

    @Override
    public boolean del(String key) {
        updateLastExecWriteTime();
        return cacheCore.del(key);
    }

    @Override
    public boolean expire(String key, long expireTime) {
        updateLastExecWriteTime();
        return cacheCore.expire(key, expireTime);
    }

    @Override
    public Map<String, Object> getAllKeyValues() {
        return cacheCore.getAllKeyValues();
    }

    @Override
    public Set<String> getAllKeys() {
        return cacheCore.getAllKeys();
    }

    @Override
    public boolean rpush(String listName, Object value) {
        updateLastExecWriteTime();
        return cacheCore.rpush(listName, value);
    }

    @Override
    public boolean lpush(String listName, Object value) {
        updateLastExecWriteTime();
        return cacheCore.lpush(listName, value);
    }

    @Override
    public Object rpop(String listName) {
        updateLastExecWriteTime();
        return cacheCore.rpop(listName);
    }

    @Override
    public Object lpop(String listName) {
        updateLastExecWriteTime();
        return cacheCore.lpop(listName);
    }

    @Override
    public Integer llen(String listName) {
        return cacheCore.llen(listName);
    }

    @Override
    public List<Object> lrange(String listName, Integer start, Integer end) throws Exception {
        return cacheCore.lrange(listName, start, end);
    }

    @Override
    public int size() {
        return cacheCore.size();
    }

    // 使用netty堆外内存保存字节数组
    public void saveBytes(){

    }




}