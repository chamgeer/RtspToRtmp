package com.chamgeer.rtsptortmp;

import com.chamgeer.rtsptortmp.utils.CacheUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.util.Date;

@SpringBootApplication
public class RtsptortmpApplication {

    public static void main(String[] args) {
        //将服务启动时间存入缓存
        CacheUtil.STARTTIME = new Date().getTime();
        SpringApplication.run(RtsptortmpApplication.class, args);
    }

    @PreDestroy
    public void destory() {
        System.err.println("释放空间...");
        // 关闭线程池
        CameraThread.MyRunnable.es.shutdownNow();
        // 销毁定时器
        TimerUtil.timer.cancel();
    }
}
