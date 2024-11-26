package entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ICache {
    void set(String key, Object value, Long expireTime);

    // setIfAbsent 操作, 如果键不存在,则设置键值对返回true,否则设置失败,返回false
    boolean setIfAbsent(String key, Object value, Long expireTime);

    // 从缓存获取值
    Object get(String key);

    // 从缓存删除键值对
    boolean del(String key);

    // 缓存时间修改
    boolean expire(String key, long expireTime);

    // 获取缓存中所有的键值对
    Map<String, Object> getAllKeyValues();

    // 获取缓存中所有的键
    Set<String> getAllKeys();

    // 向列表中添加数据.始终使用rpush添加元素的列表保证线程安全，使用set添加的列表(JSONArray)不保证线程安全
    boolean rpush(String listName, Object value);


    boolean lpush(String listName, Object value);


    Object rpop(String listName);

    Object lpop(String listName);

    Integer llen(String listName);


    List<Object> lrange(String listName, Integer start, Integer end) throws Exception;

    // 获取键值对数量
    int size();


}
