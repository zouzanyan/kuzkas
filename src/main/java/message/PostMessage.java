package message;

import lombok.Data;

@Data
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

}
