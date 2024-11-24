package message;

import lombok.Data;

@Data
public class SetMessage {

    private String key;
    private Object value;
    // 过期毫秒时间
    private Long expireTime;
    // 是否是setnx操作
    private Boolean setIfAbsent;

}
