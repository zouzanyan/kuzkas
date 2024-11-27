package singleton;

import entity.Cache;
import persistence.impl.FilePersistence;

// 缓存单例
public class CacheSingleton {

    private static final boolean isPersistence = true;
    private static volatile Cache instance;

    private CacheSingleton() {
    }

    public static Cache getInstance() {
        if (instance == null) {
            synchronized (CacheSingleton.class) {
                if (instance == null) {
                    if (isPersistence) {
                        // 默认最多丢失 10s 数据
                        FilePersistence filePersistence = new FilePersistence(10_000L);
                        //从持久化文件中加载数据
                        instance = filePersistence.executeLoadByKryo();
                        // 定时任务保存数据
                        filePersistence.scheduleSave();
                        return instance;
                    }
                    instance = new Cache();
                }
            }
        }
        return instance;
    }
}