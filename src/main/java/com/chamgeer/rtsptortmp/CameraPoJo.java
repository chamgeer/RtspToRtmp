package com.chamgeer.rtsptortmp;

import lombok.Data;
import lombok.ToString;

/**
 * @author chamgeer
 * @Date 2021/9/14
 */
@Data
@ToString
public class CameraPoJo {

    private String username;// 摄像头账号
    private String password;// 摄像头密码
    private String ip;// 摄像头ip
    private String channel;// 摄像头通道号
    private String stream;// 摄像头码流（main为主码流、sub为子码流）
    private String rtsp;// rtsp地址
    private String rtmp;// rtmp地址
    private String startTime;// 回放开始时间
    private String endTime;// 回放结束时间
    private String openTime;// 打开时间
    private int count = 0;// 使用人数
    private String token;//唯一标识token
}
