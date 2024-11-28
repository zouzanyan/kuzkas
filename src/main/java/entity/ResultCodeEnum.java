package entity;


public enum ResultCodeEnum{
    /**
     * success
     */
    SUCCESS(0,"Operation successful"),
    /**
     * fail
     */
    FAIL(-1,"Operation failed"),
    /**
     * 参数错误：1001-1999
     */
    PARAM_IS_INVALID(1001,"Invalid parameter"),
    PARAM_TYPE_ERROR(1002,"Parameter type error"),
    REQUESTBODY_IS_ERROR(1003,"Request body error");
    /**
     * 状态码
     */
    private final int code;
    /**
     * 提示信息
     */
    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


    ResultCodeEnum(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}