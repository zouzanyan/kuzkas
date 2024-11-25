package persistence.impl;

import com.alibaba.fastjson.JSON;
import com.sun.deploy.cache.CacheEntry;
import entity.Cache;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.CachePersistence;
import singleton.CacheSingleton;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FilePersistence implements CachePersistence {
    private static final Logger logger = LoggerFactory.getLogger(FilePersistence.class);

    public static final String FILE_PATH = "kuzkas_db.json";

    private final Cache cache = CacheSingleton.getInstance();
    @Override
    public synchronized boolean save() {
        Map<String, Cache.CacheEntry> cacheMap = cache.getCacheMap();
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            boolean first = true;
            writer.write("{");
            for (Map.Entry<String, Cache.CacheEntry> entry : cacheMap.entrySet()) {
                if (!first) {
                    writer.write(",");
                }
                writer.write(JSON.toJSONString(entry));
                first = false;
            }
            writer.write("}");
        } catch (IOException e) {
            // 记录异常信息
            logger.error("Failed to write cache to file: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public synchronized boolean load() {
        return false;
    }
}
