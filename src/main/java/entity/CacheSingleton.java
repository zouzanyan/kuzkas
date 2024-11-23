package entity;

// 缓存单例
public class CacheSingleton {
    private static volatile Cache instance;

    private CacheSingleton() {
    }

    public static Cache getInstance() {
        if (instance == null) {
            synchronized (CacheSingleton.class) {
                if (instance == null) {
                    instance = new Cache();
                }
            }
        }
        return instance;
    }
}