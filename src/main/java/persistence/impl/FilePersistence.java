package persistence.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import config.KuzkasConfig;
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
import java.util.concurrent.atomic.AtomicLong;

public class FilePersistence implements CachePersistence {
    private static final Logger logger = LoggerFactory.getLogger(FilePersistence.class);

    private static final KuzkasConfig kuzkasConfig = KuzkasConfig.getInstance();

    private static final String FILE_PATH = kuzkasConfig.getData_persistence_file_path();
    private static final long saveInterval = kuzkasConfig.getData_persistence_interval();

    // 定时器
    Timer timer;
    // 序列化器
    Kryo kryo;

    public FilePersistence() {
        timer = new Timer();
        kryo = new Kryo();
        doKryoConfig();
    }

    private void doKryoConfig() {
        kryo.register(ConcurrentHashMap.class);
        kryo.register(CopyOnWriteArrayList.class);
        kryo.register(BigInteger.class);
        kryo.register(AtomicLong.class);

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
            logger.debug("Data persistence was successful, time consuming: {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            logger.error("Data persistence failed", e);
            return false;
        }
        return true;
    }

    public synchronized Cache executeLoadByKryo() {
        long startTime = System.currentTimeMillis();
        if (!Files.exists(Paths.get(FILE_PATH))) {
            logger.warn("Backup snapshot does not exist, create {} snapshot", FILE_PATH);
            return new Cache();
        }
        try (
                FileInputStream fileIn = new FileInputStream(FILE_PATH);
                Input input = new Input(fileIn)
        ) {
            Cache cache = kryo.readObject(input, Cache.class);
            logger.info("Data recovery successful, time taken: {} ms", System.currentTimeMillis() - startTime);
            return cache;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Cache();
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
                if (System.currentTimeMillis() - CacheSingleton.getInstance().getLastExecWriteTime() > saveInterval) {
                    return;
                }
                save();
            }
        }, 1000, saveInterval);
        logger.info("Scheduled persistence has been started, the time interval is {} ms", saveInterval);
    }
}