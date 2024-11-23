package res;

import lombok.Data;

@Data
public class SetMessage {

    private String key;
    private Object value;
    // 过期毫秒时间
    private Long expireTime;

}
