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
import entity.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resources;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class KuzkasBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(KuzkasBootStrap.class);
    private static final KuzkasConfig kuzkasConfig = KuzkasConfig.getInstance();
    private static final String Persistent_file_path = kuzkasConfig.getPersistent_file_path();
    private static final long saveInterval = kuzkasConfig.getPersistent_interval();
    private final Kryo kryo;

    private final ScheduledExecutorService scheduledExecutorService;
    private Cache cache;

    public KuzkasBootStrap() {
        ThreadFactory schedulerThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Kuzkas-scheduler-sava-" + threadNumber.getAndIncrement());
                t.setDaemon(false); // 设置为非守护线程
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };
        scheduledExecutorService = Executors.newScheduledThreadPool(3, schedulerThreadFactory);
        // sync保证kryo线程安全
        kryo = new Kryo();
        kryoSeriConfig(kryo);
    }

    public void KuzkasStart() {
        initCache();
        scheduleTaskStart();
        serverStart();
    }

    public static void initBanner() {
        // 从resource文件夹下读取banner.txt文件
        try (FileInputStream fis = new FileInputStream("src/main/resources/banner.txt")) {
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            String banner = new String(bytes);
            System.out.println(banner);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            // 每隔saveInterval时间，将缓存数据持久化
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                try {
                    // 缓存数据saveInterval时间间隔未更新则不执行落盘
                    if (System.currentTimeMillis() - cache.getLastExecWriteTime() > saveInterval) {
                        return;
                    }
                    cacheDataSave();
                } catch (Exception e) {
                    logger.error("Scheduled Data persistence failed", e);
                }
            }, 1000, saveInterval, TimeUnit.MILLISECONDS);
        }

    }
    private void scheduleRemoveExpiredCacheTask() {
        // 每10秒全盘遍历清理一次过期数据
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                // 缓存数据saveInterval时间间隔未更新则不执行定期删除
                if (System.currentTimeMillis() - cache.getLastExecWriteTime() > saveInterval) {
                    return;
                }
                cache.clearExpiredKeys();
            } catch (Exception e) {
                logger.error("Scheduled remove expired data error", e);
            }
        }, 1000, 10000, TimeUnit.MILLISECONDS);
    }

    public synchronized void cacheDataSave() {
        long startTime = System.currentTimeMillis();
        try (FileOutputStream fileOut = new FileOutputStream(Persistent_file_path); Output output = new Output(fileOut)) {
            kryo.writeObject(output, cache);
            logger.debug("Data persistence was successful, time consuming: {} ms", System.currentTimeMillis() - startTime);
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
        kryo.register(FileMetadata.class);
        // FastJSON的数据类型
        kryo.register(JSONObject.class);
        kryo.register(JSONArray.class);
    }
}