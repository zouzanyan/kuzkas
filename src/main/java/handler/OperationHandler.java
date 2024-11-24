package handler;


import com.alibaba.fastjson.JSON;
import entity.Cache;
import singleton.CacheSingleton;
import message.DelMessage;
import message.ExpireMessage;
import message.SetMessage;

public class OperationHandler {
    private static final Cache cache = CacheSingleton.getInstance();

    public static Object handleGetOperation(String key) {
        return cache.get(key);
    }

    public static boolean handleSetOperation(String content) {
        SetMessage setMessage = JSON.parseObject(content, SetMessage.class);
        if (setMessage == null || setMessage.getKey() == null) {
            return false;
        }
        if (Boolean.TRUE.equals(setMessage.getSetIfAbsent())){
            return cache.setIfAbsent(setMessage.getKey(), setMessage.getValue(), setMessage.getExpireTime());
        }
        cache.set(setMessage.getKey(), setMessage.getValue(), setMessage.getExpireTime());
        return true;
    }

    public static boolean handleDelOperation(String content) {

        DelMessage delMessage = JSON.parseObject(content, DelMessage.class);
        return cache.del(delMessage.getKey());

    }
    public static boolean handleExpireOperation(String content) {
        ExpireMessage expireMessage = JSON.parseObject(content, ExpireMessage.class);
        return cache.expire(expireMessage.getKey(), expireMessage.getExpireTime());
    }

    public static Object handleAllKeysOperation() {
        return cache.getAllKeys();
    }

    public static Object handleAllKeyValuesOperation() {
        return cache.getAllKeyValues();
    }

    public static boolean handleSetIfAbsentOperation(String content) {
        SetMessage setMessage = JSON.parseObject(content, SetMessage.class);
        return cache.setIfAbsent(setMessage.getKey(), setMessage.getValue(), setMessage.getExpireTime());
    }


    public static boolean handleRpushOperation(String content) {
        SetMessage setMessage = JSON.parseObject(content, SetMessage.class);
        return cache.Rpush(setMessage.getKey(), setMessage.getValue());
    }

    public static Object handleLpopOperation(String content) {
        SetMessage setMessage = JSON.parseObject(content, SetMessage.class);
        return cache.Lpop(setMessage.getKey());
    }
}
