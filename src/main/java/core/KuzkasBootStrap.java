package core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import config.KuzkasConfig;
import entity.Cache;
import entity.CacheCore;
import entity.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class KuzkasBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(KuzkasBootStrap.class);
    private static final KuzkasConfig kuzkasConfig = KuzkasConfig.getInstance();
    private static final String Persistent_file_path = kuzkasConfig.getPersistent_file_path();
    private static final long saveInterval = kuzkasConfig.getPersistent_interval();
    private final Kryo kryo;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
    private Cache cache;

    public KuzkasBootStrap() {
        // sync保证kryo线程安全
        kryo = new Kryo();
        kryoSeriConfig(kryo);
    }

    public void KuzkasStart() {
        initCache();
        scheduleTaskStart();
        serverStart();
    }


    // 初始化缓存
    void initCache() {
        long startTime = System.currentTimeMillis();
        cache = initGlobalCache();
        // cache设置为项目全局共享
        CacheManager.setCache(cache);
        logger.info("Cache data recovery successful, time taken: {} ms", System.currentTimeMillis() - startTime);
    }

    // 启动定时任务
    void scheduleTaskStart() {
        long startTime = System.currentTimeMillis();
        scheduleSaveCacheTask();
        logger.info("Scheduled persistence has been started, time taken: {} ms", System.currentTimeMillis() - startTime);
    }

    // 启动服务
    void serverStart() {
        long startTime = System.currentTimeMillis();
        NettyServer nettyServer = new NettyServer();
        nettyServer.httpServerStart();
        logger.info("Kuzkas Server started at port " + kuzkasConfig.getPort() + ", time taken: {} ms", System.currentTimeMillis() - startTime);
    }


    private synchronized Cache initGlobalCache() {
        if (kuzkasConfig.isPersistent_file_read()) {
            if (!Files.exists(Paths.get(Persistent_file_path))) {
                logger.warn("Backup snapshot does not exist, will create {} snapshot backup", Persistent_file_path);
                return new Cache();
            }
            try (FileInputStream fileIn = new FileInputStream(Persistent_file_path); Input input = new Input(fileIn)) {
                // 反序列化获取快照数据
                cache = kryo.readObject(input, Cache.class);
                return cache;
            } catch (IOException e) {
                logger.error("Cache data recovery failed, will auto-create null Cache", e);
            }
        } else {
            logger.info("Because Persistent_file_read disabled, will auto-create null Cache");
        }
        return new Cache();
    }

    private void scheduleSaveCacheTask() {

        if (kuzkasConfig.isPersistent_file_write()) {
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                try {
                    if (System.currentTimeMillis() - cache.getLastExecWriteTime() > saveInterval) {
                        return;
                    }
                    cacheDataSave();
                } catch (Exception e) {
                    logger.error("scheduled Data persistence failed", e);
                }
            }, 1000, saveInterval, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void cacheDataSave() {
        long startTime = System.currentTimeMillis();
        try (FileOutputStream fileOut = new FileOutputStream(Persistent_file_path); Output output = new Output(fileOut)) {
            kryo.writeObject(output, cache);
            logger.info("Data persistence was successful, time consuming: {} ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            logger.error("Data persistence failed", e);
        }
    }


    private void kryoSeriConfig(Kryo kryo) {
        // 使用的JDK自带数据结构
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
    }
}