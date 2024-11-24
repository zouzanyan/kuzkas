package message;

import lombok.Data;

@Data
public class ExpireMessage {

    private String key;
    // 过期毫秒时间
    private Long expireTime;
}
