package singleton;

import config.KuzkasConfig;
import entity.Cache;
import persistence.impl.FilePersistence;

// 缓存单例
public class CacheSingleton {

    private static final KuzkasConfig kuzkasConfig = KuzkasConfig.getInstance();
    private static volatile Cache instance;

    private CacheSingleton() {
    }

    public static Cache getInstance() {
        if (instance == null) {
            synchronized (CacheSingleton.class) {
                if (instance == null) {
                    if (kuzkasConfig.isPersistence()) {
                        return getCacheFromFile();
                    }
                    instance = new Cache();
                }
            }
        }
        return instance;
    }

    private static Cache getCacheFromFile() {
        FilePersistence filePersistence = new FilePersistence();
        //从持久化文件中加载数据
        instance = filePersistence.executeLoadByKryo();
        // 定时任务保存数据
        filePersistence.scheduleSave();
        return instance;
    }
}