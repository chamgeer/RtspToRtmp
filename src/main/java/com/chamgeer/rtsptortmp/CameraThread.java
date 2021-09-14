package com.chamgeer.rtsptortmp;

import com.chamgeer.rtsptortmp.controller.CameraController;
import com.chamgeer.rtsptortmp.utils.CacheUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author chamgeer
 * @Date 2021/9/14
 */
public class CameraThread {

    public static class MyRunnable implements Runnable {
        // 创建线程池
        public static ExecutorService es = Executors.newCachedThreadPool();

        private CameraPoJo cameraPojo;
        private Thread nowThread;

        public MyRunnable(CameraPoJo cameraPojo) {
            this.cameraPojo = cameraPojo;
        }

        // 中断线程
        public void setInterrupted() {
            nowThread.interrupt();
        }

        @Override
        public void run() {
            // 直播流
            try {
                // 获取当前线程存入缓存
                nowThread = Thread.currentThread();
                CacheUtil.STREAMMAP.put(cameraPojo.getToken(), cameraPojo);
                // 执行转流推流任务
                CameraPush push = new CameraPush(cameraPojo).from();
                if (push != null) {
                    push.to().go(nowThread);
                }
                // 清除缓存
                CacheUtil.STREAMMAP.remove(cameraPojo.getToken());
                CameraController.jobMap.remove(cameraPojo.getToken());
            } catch (Exception e) {
                System.err.println(
                        "当前线程：" + Thread.currentThread().getName() + " 当前任务：" + cameraPojo.getRtsp() + "停止...");
                CacheUtil.STREAMMAP.remove(cameraPojo.getToken());
                CameraController.jobMap.remove(cameraPojo.getToken());
                e.printStackTrace();
            }
        }
    }
}
