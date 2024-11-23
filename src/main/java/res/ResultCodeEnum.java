package res;

import lombok.Getter;

@Getter
public enum ResultCodeEnum{
    /**
     * success
     */
    SUCCESS(0,"操作成功"),
    /**
     * fail
     */
    FAIL(-1,"操作失败"),
    /**
     * 参数错误：1001-1999
     */
    PARAM_IS_INVALID(1001,"参数无效"),
    PARAM_TYPE_ERROR(1002,"参数类型错误"),
    REQUESTBODY_IS_ERROR(1003,"请求体错误");
    /**
     * 状态码
     */
    private final int code;
    /**
     * 提示信息
     */
    private final String message;
    ResultCodeEnum(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}