package message;

import java.util.Objects;

public class PostMessage {

    private String key;
    private Object value;
    // 过期毫秒时间
    private Long expireTime;
    // 是否是setnx操作
    private Boolean setIfAbsent;
    // list操作起始索引
    private int listIndexStart;
    // list操作结束索引
    private int listIndexEnd;

    public PostMessage() {
    }

    public PostMessage(String key, Object value, Long expireTime, Boolean setIfAbsent, int listIndexStart, int listIndexEnd) {
        this.key = key;
        this.value = value;
        this.expireTime = expireTime;
        this.setIfAbsent = setIfAbsent;
        this.listIndexStart = listIndexStart;
        this.listIndexEnd = listIndexEnd;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Boolean getSetIfAbsent() {
        return setIfAbsent;
    }

    public void setSetIfAbsent(Boolean setIfAbsent) {
        this.setIfAbsent = setIfAbsent;
    }

    public int getListIndexStart() {
        return listIndexStart;
    }

    public void setListIndexStart(int listIndexStart) {
        this.listIndexStart = listIndexStart;
    }

    public int getListIndexEnd() {
        return listIndexEnd;
    }

    public void setListIndexEnd(int listIndexEnd) {
        this.listIndexEnd = listIndexEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostMessage)) return false;
        PostMessage that = (PostMessage) o;
        return listIndexStart == that.listIndexStart && listIndexEnd == that.listIndexEnd && Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(expireTime, that.expireTime) && Objects.equals(setIfAbsent, that.setIfAbsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, expireTime, setIfAbsent, listIndexStart, listIndexEnd);
    }
}
