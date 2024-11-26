package persistence.impl;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import entity.Cache;
import entity.CacheProxy;
import entity.ICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.CachePersistence;
import singleton.CacheSingleton;
import sun.rmi.server.DeserializationChecker;

import java.io.*;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class FilePersistence implements CachePersistence {
    public static final String FILE_PATH = "kuzkas_db";
    private static final Logger logger = LoggerFactory.getLogger(FilePersistence.class);
    private CacheProxy cache = new CacheProxy();
    // 定时器
    Timer timer = new Timer();
    // 序列化器
    Kryo kryo = new Kryo();

    public FilePersistence() {
        kryo.register(CacheProxy.class);
        kryo.register(Cache.CacheEntry.class);
        kryo.register(Cache.class);
        kryo.register(ConcurrentHashMap.class);
    }


    @Override
    public synchronized boolean save() {

        return executeSaveByKryo();

    }

    public synchronized boolean executeSaveByKryo() {
        try (
                FileOutputStream fileOut = new FileOutputStream(FILE_PATH);
                Output output = new Output(fileOut)
        ) {
            kryo.writeObject(output, cache);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        logger.info("数据持久化成功");
        return true;
    }

    public synchronized CacheProxy executeLoadByKryo() {
        if (!new File(FILE_PATH).exists()){
            logger.warn("数据文件不存在");
            return new CacheProxy();
        }
        try (
                FileInputStream fileIn = new FileInputStream(FILE_PATH);
                Input input = new Input(fileIn)
        ) {
            return kryo.readObject(input, CacheProxy.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.warn("数据恢复失败");
        return new CacheProxy();
    }

    public synchronized boolean executeSaveByJson() {
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

        return true;

    }

    // 定时落盘
    public void scheduleSave() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executeSaveByKryo();
            }
        }, 0, 10 * 1000);
        logger.info("定时持久化已启动");
    }


}