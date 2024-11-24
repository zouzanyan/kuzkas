package entity;

public enum UriOperationEnum {
    GET("get"),
    SET("set"),
    KEYS("keys"),
    DEL("del"),
    EXPIRE("expire"),
    ALL("all"),
    RPUSH("rpush"),
    LPOP("lpop"),
    LLEN("llen");




    private final String uri;

    UriOperationEnum(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
