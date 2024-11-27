package persistence.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import entity.CacheCore;
import entity.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.CachePersistence;
import singleton.CacheSingleton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FilePersistence implements CachePersistence {
    private static final Logger logger = LoggerFactory.getLogger(FilePersistence.class);

    // 完整快照备份文件名
    public static final String FILE_PATH = "dump.kuzkas";

    // 定时器
    Timer timer;
    // 序列化器
    Kryo kryo;

    // 快照备份间隔时间, 默认10s
    private long saveInterval = 10 * 1000;

    public FilePersistence() {
        timer = new Timer();
        kryo = new Kryo();
        doKryoConfig();
    }

    public FilePersistence(long saveInterval) {
        timer = new Timer();
        kryo = new Kryo();
        doKryoConfig();
        this.saveInterval = saveInterval;
    }

    private void doKryoConfig() {
        kryo.register(ConcurrentHashMap.class);
        kryo.register(CopyOnWriteArrayList.class);
        kryo.register(BigInteger.class);

        // 自定义的缓存数据类型
        kryo.register(Cache.class);
        kryo.register(CacheCore.CacheEntry.class);
        kryo.register(CacheCore.class);

        // FastJSON的数据类型
        kryo.register(JSONObject.class);
        kryo.register(JSONArray.class);
//        kryo.setRegistrationRequired(false);
    }


    @Override
    public synchronized void save() {

        executeSaveByKryo(CacheSingleton.getInstance());

    }

    public synchronized boolean executeSaveByKryo(Cache cache) {
        long startTime = System.currentTimeMillis();
        try (
                FileOutputStream fileOut = new FileOutputStream(FILE_PATH);
                Output output = new Output(fileOut)
        ) {
            kryo.writeObject(output, cache);
            logger.info("数据持久化成功，耗时：{}ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            logger.error("数据持久化失败", e);
            return false;
        }
        return true;
    }

    public synchronized Cache executeLoadByKryo() {
        long startTime = System.currentTimeMillis();
        if (!Files.exists(Paths.get(FILE_PATH))) {
            logger.warn("备份快照不存在, 创建 " + FILE_PATH + " 快照");
            return new Cache();
        }
        try (
                FileInputStream fileIn = new FileInputStream(FILE_PATH);
                Input input = new Input(fileIn)
        ) {
            Cache cache = kryo.readObject(input, Cache.class);
            logger.info("数据加载成功，耗时：{}ms", System.currentTimeMillis() - startTime);
            return cache;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Cache();
    }

//    public synchronized boolean executeSaveByJson() {
//        Map<String, Cache.CacheEntry> cacheMap = cache.getCacheMap();
//        try (FileWriter writer = new FileWriter(FILE_PATH)) {
//            boolean first = true;
//            writer.write("{");
//            for (Map.Entry<String, Cache.CacheEntry> entry : cacheMap.entrySet()) {
//                if (!first) {
//                    writer.write(",");
//                }
//                writer.write(JSON.toJSONString(entry));
//                first = false;
//            }
//            writer.write("}");
//        } catch (IOException e) {
//            // 记录异常信息
//            logger.error("Failed to write cache to file: " + e.getMessage(), e);
//            return false;
//        }
//        return true;
//    }

    @Override
    public synchronized boolean load() {

        return true;

    }

    // 定时落盘
    public void scheduleSave() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 10s 未执行过写操作不落盘
                if (System.currentTimeMillis() - saveInterval > CacheSingleton.getInstance().getLastExecWriteTime()) {
                    return;
                }
                save();
            }
        }, 1000, saveInterval);
        logger.info("定时持久化已启动");
    }
}