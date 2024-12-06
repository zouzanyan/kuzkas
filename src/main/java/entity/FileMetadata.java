package entity;

import java.util.Objects;

public class FileMetadata {
    private  String key;
    private  long size;
    private  long createTime;
    private  long expireTime;

    public FileMetadata(){

    }

    public FileMetadata(String key, long size, long createTime, long expireTime) {
        this.key = key;
        this.size = size;
        this.createTime = createTime;
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > createTime + expireTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
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
}