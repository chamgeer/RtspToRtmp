package com.chamgeer.rtsptortmp.utils;

import com.chamgeer.rtsptortmp.CameraPoJo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chamgeer
 * @Date 2021/9/14
 */
public final class CacheUtil {

    /*
     * 保存已经开始推的流
     */
    public static Map<String, CameraPoJo> STREAMMAP = new ConcurrentHashMap<String, CameraPoJo>();

    /*
     * 保存服务启动时间
     */
    public static long STARTTIME;
}
