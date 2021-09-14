package com.chamgeer.rtsptortmp;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import static org.bytedeco.ffmpeg.global.avcodec.av_packet_unref;

/**
 * @author chamgeer
 * @Date 2021/9/14
 */
public class CameraPush {

    protected FFmpegFrameGrabber grabber = null;// 解码器
    protected FFmpegFrameRecorder recorder= null;// 编码器
    int width;// 视频像素宽
    int height;// 视频像素高

    // 视频参数
    protected int codecid;
    protected double framerate;// 帧率
    protected int bitrate;// 比特率

    // 音频参数
    // 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
    protected int audiocodecid;
    private int audioChannels;
    private int audioBitrate;
    private int sampleRate;
    // 设备信息
    private CameraPoJo cameraPojo;

    public CameraPush(CameraPoJo cameraPojo) {
        this.cameraPojo = cameraPojo;
    }

    /**
     * 选择视频源 
     * @return
     * @throws Exception
     */
    public CameraPush from() throws Exception {
        // 采集/抓取器
        System.out.println(cameraPojo.getRtsp());
        grabber = new FFmpegFrameGrabber(cameraPojo.getRtsp());
        if (cameraPojo.getRtsp().indexOf("rtsp") >= 0) {
            grabber.setOption("rtsp_transport", "tcp");// tcp用于解决丢包问题
        }
        // 设置采集器构造超时时间
        grabber.setOption("stimeout", "2000000");
        grabber.start();// 开始之后ffmpeg会采集视频信息，之后就可以获取音视频信息
        width = grabber.getImageWidth();
        height = grabber.getImageHeight();
        // 若视频像素值为0，说明采集器构造超时，程序结束
        if (width == 0 && height == 0) {
            System.err.println("[ERROR]   拉流超时...");
            return null;
        }
        // 视频参数
        codecid = grabber.getVideoCodec();
        System.err.println("视频编码：" + codecid);
        framerate = grabber.getVideoFrameRate();// 帧率
        bitrate = grabber.getVideoBitrate();// 比特率
        // 音频参数
        // 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
        audiocodecid = grabber.getAudioCodec();
        System.err.println("音频编码：" + audiocodecid);
        audioChannels = grabber.getAudioChannels();
        audioBitrate = grabber.getAudioBitrate();
        sampleRate = grabber.getSampleRate(); //采样频率
        if (audioBitrate < 1) {
            audioBitrate = 128 * 1000;// 默认音频比特率
        }
        return this;
    }

    /**
     * 选择输出
     * @return
     * @throws Exception
     */
    public CameraPush to() throws Exception {
        // 录制/推流器
       recorder = new FFmpegFrameRecorder(cameraPojo.getRtmp(), width, height);
       recorder.setVideoOption("crf", "28");// 画面质量参数，0~51；18~28是一个合理范围
       recorder.setGopSize(2);
       recorder.setFrameRate(framerate);
       recorder.setVideoBitrate(bitrate);

       recorder.setAudioChannels(audioChannels);
       recorder.setAudioBitrate(audioBitrate);
       recorder.setSampleRate(sampleRate);
       AVFormatContext fc = null;
        if (cameraPojo.getRtmp().indexOf("rtmp") >= 0 || cameraPojo.getRtmp().indexOf("flv") > 0) {
            // 封装格式flv
           recorder.setFormat("flv");
           recorder.setAudioCodecName("aac");
           recorder.setVideoCodec(codecid);
           fc = grabber.getFormatContext();
        }
        recorder.start(fc);
        return this;
    }

    /**
     * 转封装
     * @return
     * @throws org.bytedeco.javacv.FrameGrabber.Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     * @throws InterruptedException
     */
    public CameraPush go(Thread nowThread)
            throws org.bytedeco.javacv.FrameGrabber.Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        long err_index = 0;// 采集或推流导致的错误次数，连续五次没有采集到帧则认为视频采集结束，程序错误次数超过5次即中断程序
        //将探测时留下的数据帧释放掉，以免因为dts，pts的问题对推流造成影响
        grabber.flush();
        for (int no_frame_index = 0; no_frame_index < 5 || err_index < 5;) {
            try {
                // 用于中断线程时，结束该循环
                nowThread.sleep(1);
                AVPacket pkt = null;
                // 获取没有解码的音视频帧
                pkt = grabber.grabPacket();
                if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
                    // 空包记录次数跳过
                    no_frame_index++;
                    err_index++;
                    continue;
                }
                // 不需要编码直接把音视频帧推出去
                err_index += (recorder.recordPacket(pkt) ? 0 : 1);
                av_packet_unref(pkt);
            } catch (InterruptedException e) {
                // 当需要结束推流时，调用线程中断方法，中断推流的线程。当前线程for循环执行到
                // nowThread.sleep(1);这行代码时，因为线程已经不存在了，所以会捕获异常，结束for循环
                // 销毁构造器
                grabber.close();
                recorder.close();
                System.err.println("设备中断推流成功...");
                break;
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
                err_index++;
            } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                err_index++;
            }
        }
        // 程序正常结束销毁构造器
        grabber.close();
        recorder.close();
        System.err.println("设备推流完毕...");
        return this;
    }

}
