package singleton;

import java.util.HashMap;
import java.util.Map;

public class SingletonPool {
    private static final Map<String, Object> singletonPool = new HashMap<>();

    // 将单例对象放入单例池
    public static void addSingleton(String key, Object singleton) {
        singletonPool.put(key, singleton);
    }

    // 从单例池中获取单例对象
    public static Object getSingleton(String key) {
        return singletonPool.get(key);
    }
}
