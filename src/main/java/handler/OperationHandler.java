package handler;


import com.alibaba.fastjson.JSON;
import entity.Cache;
import entity.CacheManager;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import message.PostMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(OperationHandler.class);
    private static final Cache cache = CacheManager.getCache();

    public static Object handleGetOperation(String key) {
        return cache.get(key);
    }

    public static boolean handleSetOperation(String content) {
        PostMessage setMessage = JSON.parseObject(content, PostMessage.class);
        if (setMessage == null || setMessage.getKey() == null) {
            return false;
        }
        if (Boolean.TRUE.equals(setMessage.getSetIfAbsent())) {
            return cache.setIfAbsent(setMessage.getKey(), setMessage.getValue(), setMessage.getExpireTime());
        }
        cache.set(setMessage.getKey(), setMessage.getValue(), setMessage.getExpireTime());
        return true;
    }

    public static boolean handleDelOperation(String content) {

        PostMessage delMessage = JSON.parseObject(content, PostMessage.class);
        return cache.del(delMessage.getKey());

    }

    public static boolean handleExpireOperation(String content) {
        PostMessage expireMessage = JSON.parseObject(content, PostMessage.class);
        return cache.expire(expireMessage.getKey(), expireMessage.getExpireTime());
    }

    public static Object handleAllKeysOperation() {
        return cache.getAllKeys();
    }

    public static Object handleAllKeyValuesOperation() {
        return cache.getAllKeyValues();
    }

    public static boolean handleSetIfAbsentOperation(String content) {
        PostMessage setifAbsentMessage = JSON.parseObject(content, PostMessage.class);
        return cache.setIfAbsent(setifAbsentMessage.getKey(), setifAbsentMessage.getValue(), setifAbsentMessage.getExpireTime());
    }


    public static boolean handleRpushOperation(String content) {
        PostMessage rpushMessage = JSON.parseObject(content, PostMessage.class);
        return cache.rpush(rpushMessage.getKey(), rpushMessage.getValue());
    }

    public static Object handleLpopOperation(String content) {
        PostMessage lpopMessage = JSON.parseObject(content, PostMessage.class);
        return cache.lpop(lpopMessage.getKey());
    }

    public static boolean handleLpushOperation(String content) {
        PostMessage lpushMessage = JSON.parseObject(content, PostMessage.class);
        return cache.lpush(lpushMessage.getKey(), lpushMessage.getValue());
    }


    public static Object handleRpopOperation(String content) {
        PostMessage rpopMessage = JSON.parseObject(content, PostMessage.class);
        return cache.rpop(rpopMessage.getKey());
    }

    public static Object handleLrangeOperation(String content) throws Exception {
        PostMessage lrangeMessage = JSON.parseObject(content, PostMessage.class);
        return cache.lrange(lrangeMessage.getKey(), lrangeMessage.getListIndexStart(), lrangeMessage.getListIndexEnd());
    }

    public static Integer handleLlenOperation(String content) {
        PostMessage llenMessage = JSON.parseObject(content, PostMessage.class);
        return cache.llen(llenMessage.getKey());
    }

    public static boolean handleFileUploadOperation(HttpPostRequestDecoder requestDecoder) {
        while (requestDecoder.hasNext()) {
            InterfaceHttpData data = requestDecoder.next();
            if (data != null) {
                try {
                    // 只处理文件类型的formdata
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        if (fileUpload.isCompleted()) {
                            byte[] bytes = fileUpload.get();
                            cache.uploadFile("uploadDir", fileUpload.getFilename(), bytes, -1);
                        }
                    }
                } catch (IOException e) {
                    logger.error(data.getName() + "上传异常");
                    logger.error("File upload failed", e);
                    return false;
                } finally {
                    data.release();
                }
            }
        }
        return true;
    }
}
