package entity;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class CacheCore implements ICache {

    // 对象缓存
    private final Map<String, CacheEntry> cacheMap = new ConcurrentHashMap<>();
    // 文件元数据缓存
    private final Map<String, FileMetadata> fileMetadataMap = new ConcurrentHashMap<>();

    public CacheCore() {

    }

    /**
     * 设置键值对，其中包含值、开始时间和过期时间
     *
     * @param key        键，用于标识缓存中的数据
     * @param value      值，存储的数据对象
     * @param expireTime 过期时间，如果大于等于0，则表示从当前时间起数据的有效期（毫秒）,小于0表示永不过期
     */
    @Override
    public void set(String key, Object value, Long expireTime) {
        long currentTimeMillis = System.currentTimeMillis();
        // 如果过期时间未设置，则默认为-1，表示永不过期
        if (expireTime == null) {
            expireTime = -1L;
        }
        if (expireTime > 0) {
            expireTime = currentTimeMillis + expireTime;
        }
        CacheEntry cacheEntry = new CacheEntry(value, expireTime);
        cacheMap.put(key, cacheEntry);
    }

    // setIfAbsent 操作, 如果键不存在,则设置键值对返回true,否则设置失败,返回false
    @Override
    public boolean setIfAbsent(String key, Object value, Long expireTime) {
        if (expireTime == null) {
            expireTime = (long) -1;
        }
        if (expireTime > 0) {
            expireTime = System.currentTimeMillis() + expireTime;
        }
        CacheEntry cacheEntry = new CacheEntry(value, expireTime);
        return cacheMap.putIfAbsent(key, cacheEntry) == null;
    }

    // 从缓存获取值
    @Override
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
    @Override
    public boolean del(String key) {
        return cacheMap.remove(key) != null;
    }

    // 缓存时间修改
    @Override
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
    @Override
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
            if (expireTimestamp >= 0 && expireTimestamp < currentTimeMillis) {
                iterator.remove();
            }
        }
    }

    // 获取缓存中所有的键
    @Override
    public Set<String> getAllKeys() {
        return cacheMap.keySet();
    }

    // 向列表中添加数据.始终使用rpush添加元素的列表保证线程安全，使用set添加的列表(JSONArray)不保证线程安全
    @Override
    @SuppressWarnings("unchecked")
    public boolean rpush(String listName, Object value) {
        Object data = get(listName);
        if (data == null) {
            CopyOnWriteArrayList<Object> objects = new CopyOnWriteArrayList<>();
            objects.add(value);
            set(listName, objects, -1L);
            return true;
        }
        if (data instanceof List) {
            ((List<Object>) data).add(value);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean lpush(String listName, Object value) {
        Object data = get(listName);
        if (data == null) {
            List<Object> objects = new CopyOnWriteArrayList<>();
            objects.add(0, value);
            set(listName, objects, -1L);
            return true;
        }
        if (data instanceof List) {
            ((List<Object>) data).add(0, value);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object rpop(String listName) {
        Object data = get(listName);
        if (data == null) {
            return null;
        }
        // 从右边删除
        if (data instanceof List) {
            List<Object> list = (List<Object>) data;
            if (list.size() == 0) {
                return null;
            }
            return (list.remove(list.size() - 1));
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object lpop(String listName) {
        Object data = get(listName);
        if (data == null) {
            return null;
        }
        // 从左边删除
        if (data instanceof List) {
            List<Object> list = (List<Object>) data;
            if (list.size() == 0) {
                return null;
            }
            return (list.remove(0));
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Integer llen(String listName) {
        Object data = get(listName);
        if (data == null) {
            return null;
        }
        if (data instanceof List) {
            return ((List<Object>) data).size();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> lrange(String listName, Integer start, Integer end) throws Exception {

        Object data = get(listName);
        if (data == null) {
            return null;
        }
        if (data instanceof List) {
            return ((List<Object>) data).subList(start, end);
        }
        return null;
    }

    // 获取键值对数量
    @Override
    public int size() {
        return cacheMap.size();
    }


    // 存入二进制字节数组数据
    // 上传文件
//   TODO . ..正则校验防止恶意构造文件路径
    public void uploadFile(String uploadDir, String key, byte[] content, long expireTime) {
        File file = new File(uploadDir + File.separator + key);
        try (FileChannel fileChannel = new FileOutputStream(file).getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(content.length);
            buffer.put(content);
            buffer.flip();
            fileChannel.write(buffer);
            long createTime = System.currentTimeMillis();
            fileMetadataMap.put(key, new FileMetadata(key, file.length(), createTime, expireTime));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    // 获取文件
    public byte[] getFile(String uploadDir, String key) {
        FileMetadata metadata = fileMetadataMap.get(key);
        if (metadata == null || metadata.isExpired()) {
            return null;
        }
        File file = new File(uploadDir + File.separator + key);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            int read = fis.read(content);
            return content;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    // 文件续期
    public boolean expireFile(String uploadDir, String key, long expireTime) {
        FileMetadata metadata = fileMetadataMap.get(key);
        if (metadata == null) {
            return false;
        }
        metadata.setExpireTime(expireTime);
        return true;
    }

    // 获取所有文件元数据
    public Map<String, FileMetadata> getAllFileMetadata() {
        return fileMetadataMap;
    }

    // 删除文件
    public boolean deleteFile(String uploadDir, String key) {
        FileMetadata metadata = fileMetadataMap.remove(key);
        if (metadata == null) {
            return false;
        }
        File file = new File(uploadDir + File.separator + key);
        return file.delete();
    }

    // 清理所有过期文件
    public void clearExpiredFiles(String uploadDir) {
        fileMetadataMap.entrySet().removeIf(entry -> {
            FileMetadata metadata = entry.getValue();
            if (metadata.isExpired()) {
                File file = new File(uploadDir + File.separator + entry.getKey());
                return file.delete();
            }
            return false;
        });
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheCore)) return false;
        CacheCore cacheCore = (CacheCore) o;
        return Objects.equals(cacheMap, cacheCore.cacheMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cacheMap);
    }

    public static class CacheEntry {
        private Object value;
        // 失效时间
        private long expireTimestamp;

        public CacheEntry() {
        }

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

        public void setExpireTimestamp(long expireTimestamp) {
            this.expireTimestamp = expireTimestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheEntry)) return false;
            CacheEntry that = (CacheEntry) o;
            return expireTimestamp == that.expireTimestamp && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, expireTimestamp);
        }
    }

}
