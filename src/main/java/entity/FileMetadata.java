package entity;

import java.util.Objects;

public class FileMetadata {
    private final String key;
    private final long size;
    private final long createTime;
    private final long expireTime;

    public FileMetadata(String key, long size, long createTime, long expireTime) {
        this.key = key;
        this.size = size;
        this.createTime = createTime;
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > createTime + expireTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMetadata)) return false;
        FileMetadata that = (FileMetadata) o;
        return size == that.size && createTime == that.createTime && expireTime == that.expireTime && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, size, createTime, expireTime);
    }

    public String getKey() {
        return key;
    }

    public long getSize() {
        return size;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getExpireTime() {
        return expireTime;
    }
}