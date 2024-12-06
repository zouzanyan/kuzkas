package entity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

// 对Cache提供增强的功能
public class Cache implements ICache {

    // 缓存数据管理
    private final CacheCore cacheCore = new CacheCore();

    // 文件缓存


    // 最后一次写入时间戳
    private final AtomicLong lastExecWriteTime = new AtomicLong(0);

    public Cache() {
    }

    public long getLastExecWriteTime() {
        return lastExecWriteTime.get();
    }

    public void setLastExecWriteTime(long time) {
        lastExecWriteTime.set(time);
    }

    public void updateLastExecWriteTime() {
        lastExecWriteTime.set(System.currentTimeMillis());
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
        if (b) {
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

    public void uploadFile(String uploadDir, String key, byte[] content, long expireTime) {
        updateLastExecWriteTime();
        cacheCore.uploadFile(uploadDir, key, content, expireTime);
    }

    public byte[] getFile(String uploadDir, String key) {
        return cacheCore.getFile(uploadDir, key);
    }

    public boolean deleteFile(String uploadDir, String key) {
        updateLastExecWriteTime();
        return cacheCore.deleteFile(uploadDir, key);
    }

    public void clearExpiredFiles(String uploadDir) {
        cacheCore.clearExpiredFiles(uploadDir);
    }

    public boolean expireFile(String uploadDir, String key, long expireTime) {
        updateLastExecWriteTime();
        return cacheCore.expireFile(uploadDir, key, expireTime);
    }

    public Map<String, FileMetadata> getAllFileMetadata() {
        return cacheCore.getAllFileMetadata();
    }

}