package handler;


import com.alibaba.fastjson.JSON;
import entity.Cache;
import entity.CacheManager;
import entity.FileMetadata;
import io.netty.handler.codec.http.multipart.*;
import message.PostMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<Object> handleLrangeOperation(String content) throws Exception {
        PostMessage lrangeMessage = JSON.parseObject(content, PostMessage.class);
        return cache.lrange(lrangeMessage.getKey(), lrangeMessage.getListIndexStart(), lrangeMessage.getListIndexEnd());
    }

    public static Integer handleLlenOperation(String content) {
        PostMessage llenMessage = JSON.parseObject(content, PostMessage.class);
        return cache.llen(llenMessage.getKey());
    }

    public static boolean handleFileUploadOperation(HttpPostRequestDecoder requestDecoder) {
        Map<String, Long> fileNameExpiredTime = new HashMap<>(16);
        List<InterfaceHttpData> attributes = requestDecoder.getBodyHttpDatas();
        if (attributes == null || attributes.size() == 0) {
            return false;
        }
        // 获取文件过期时间
        for (InterfaceHttpData data : attributes) {
            if (data instanceof MixedAttribute) {
                Attribute attribute = (Attribute) data;
                String name = attribute.getName();
                String value = null;
                try {
                    value = attribute.getValue();
                    fileNameExpiredTime.put(name, Long.valueOf(value));
                } catch (IOException e) {
                    logger.warn("File expired time set failed");
                    return false;
                }
            }
        }
        // 获取文件
        for (InterfaceHttpData data : attributes) {
            if (data instanceof FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    try {
                        byte[] bytes = fileUpload.get();
                        Long fileExpiredTime = fileNameExpiredTime.getOrDefault(fileUpload.getFilename(),-1L);
                        cache.uploadFile("uploadDir", fileUpload.getFilename(), bytes, fileExpiredTime);
                    } catch (IOException e) {
                        logger.error("File upload failed", e);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static byte[] handleFileGetOperation(String key) {
        return cache.getFile("uploadDir", key);
    }

    public static boolean handleFileDeleteOperation(String key) {
        return cache.deleteFile("uploadDir", key);
    }

    // 文件续期
    public static boolean handleFileExpireOperation(String key) {
        PostMessage expireMessage = JSON.parseObject(key, PostMessage.class);
        Long expire = expireMessage.getExpireTime();
        String fileKey = expireMessage.getKey();
        return cache.expireFile("uploadDir", fileKey, expire);
    }

    public static Map<String, FileMetadata> handleFileListOperation() {
        return cache.getAllFileMetadata();
    }
}