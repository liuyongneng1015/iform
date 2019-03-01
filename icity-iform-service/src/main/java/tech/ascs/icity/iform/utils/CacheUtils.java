package tech.ascs.icity.iform.utils;

import java.util.Map;
import java.util.concurrent.*;

/** java本地缓存 */
public class CacheUtils {
    //键值对集合
    private final static Map<String, Object> keyMap = new ConcurrentHashMap<>();
    private final static Map<String, Future> futureMap = new ConcurrentHashMap<>();

    //定时器线程池，用于清除过期缓存
    private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /** 添加缓存 */
    public synchronized static void put(String key, Object data) {
        CacheUtils.put(key, data, 0);
    }

    /** 添加缓存
     * @param expire 过期时间，单位：毫秒， 0表示无限长
     */
    public synchronized static void put(String key, Object data, long expire) {
        //清除原键值对
        CacheUtils.remove(key);
        //设置过期时间
        if (expire > 0) {
            Future future = executor.schedule(()->{
                //过期后清除该键值对
                synchronized (CacheUtils.class) {
                    keyMap.remove(key);
                }
            }, expire, TimeUnit.MILLISECONDS);
            keyMap.put(key, data);
            futureMap.put(key, future);
        } else { //不设置过期时间
            keyMap.put(key, data);
        }
    }

    /** 读取缓存 */
    public synchronized static <T> T get(String key) {
        return (T)keyMap.get(key);
    }

    /** 清除缓存 */
    public synchronized static void remove(String key) {
        //清除原缓存数据
        Object object = keyMap.remove(key);
        if (object != null) {
            //清除原键值对定时器
            Future future = futureMap.remove(key);
            if (future != null) {
                future.cancel(true);
            }
        }
    }
}