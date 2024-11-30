package entity;

public class CacheManager {
    private static Cache cache;

    public static Cache getCache() {
        return cache;
    }

    public static void setCache(Cache cache) {
        CacheManager.cache = cache;
    }
}
