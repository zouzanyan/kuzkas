package singleton;

import entity.Cache;
import entity.CacheProxy;
import persistence.impl.FilePersistence;

// 缓存单例
public class CacheSingleton {

    private static final boolean isPersistence = true;
    private static volatile CacheProxy instance;

    private CacheSingleton() {
    }

    public static CacheProxy getInstance() {
        if (instance == null) {
            synchronized (CacheSingleton.class) {
                if (instance == null) {
                    if (isPersistence){
                        FilePersistence filePersistence = new FilePersistence();
                        // 从持久化文件中加载数据
//                        instance = filePersistence.executeLoadByKryo();
                        // 定时任务保存数据
                        filePersistence.scheduleSave();
                        instance = new CacheProxy();
                        return instance;
                    }
                    instance = new CacheProxy();
                }
            }
        }
        return instance;
    }
}