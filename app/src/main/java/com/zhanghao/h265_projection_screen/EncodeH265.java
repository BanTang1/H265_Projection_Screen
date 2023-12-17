package com.zhanghao.h265_projection_screen;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 将录屏的画面编码为H265码流
 */
public class EncodeH265 extends Thread {

    private final String TAG = "zh___EncodeH265";
    private MediaCodec mediaCodec;
    private MediaProjection mediaProjection;
    private SocketPush socketPush;
    private volatile boolean isRunning = true;

    private VirtualDisplay virtualDisplay;

    private int width = 720;
    private int height = 1080;

    private byte[] configData;      // VPS SPS PPS

    public EncodeH265(MediaProjection mediaProjection, SocketPush socketPush) {
        this.mediaProjection = mediaProjection;
        this.socketPush = socketPush;
    }


    /**
     * 创建编码器，开始编码线程
     */
    public void startEncodeThread() {
        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mediaCodec = MediaCodec.createEncoderByType("video/hevc");
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mediaCodec.createInputSurface();

            virtualDisplay = mediaProjection.createVirtualDisplay("-display",
                    width, height, 1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null);

            mediaCodec.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isRunning = true;
        start();
    }


    /**
     * 停止编码线程，并停止录屏
     */
    public void stopEncodeThread() {
        mediaCodec.stop();
        isRunning = false;
        mediaProjection.stop();
    }

    /**
     * 编码线程，将录屏的画面编码为H265码流
     */
    @Override
    public void run() {
        while (isRunning) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (outputBufferId >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
//                writeOutputBufferToPrivateDirectory(outputBuffer);
                handleData(bufferInfo, outputBuffer);
                mediaCodec.releaseOutputBuffer(outputBufferId, false);
            }
        }
        Log.i(TAG, "run: exit!");
    }

    /**
     * 处理码流数据：保证VPS SPS PPS 在I帧之前一定存在
     *
     * @param bufferInfo   存储有关媒体数据缓冲区的信息
     * @param outputBuffer 操作原始字节的缓冲区
     */
    private void handleData(MediaCodec.BufferInfo bufferInfo, ByteBuffer outputBuffer) {
        switch (bufferInfo.flags) {
            // 保存VPS SPS PPS 信息
            case MediaCodec.BUFFER_FLAG_CODEC_CONFIG:
                configData = new byte[bufferInfo.size];
                outputBuffer.rewind();
                outputBuffer.get(configData);
                break;
            // I帧
            case MediaCodec.BUFFER_FLAG_KEY_FRAME:
                ByteBuffer configAndIFrame = ByteBuffer.allocate(bufferInfo.size + configData.length);
                configAndIFrame.put(configData);
                outputBuffer.rewind();
                configAndIFrame.put(outputBuffer);
                socketPush.sendData(configAndIFrame.array());
                break;
            // 其余信息正常发送
            default:
                byte[] otherData = new byte[bufferInfo.size];
                outputBuffer.rewind();
                outputBuffer.get(otherData);
                socketPush.sendData(otherData);
                break;
        }
    }

    /**
     * 将录屏的画面编码为H265码流，并写入私有目录
     *
     * @param outputBuffer 码流缓冲数据
     */
    private void writeOutputBufferToPrivateDirectory(ByteBuffer outputBuffer) {
        try {
            if (outputBuffer == null) return;
            outputBuffer.rewind();
            // 获取应用程序私有目录
            File privateDir = MainActivity.context.getExternalFilesDir(null);
            // 创建一个新的文件，你可以自定义文件名和格式
            File outputFile = new File(privateDir, "outputData.h265");

            // 创建文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile, true);

            // 将ByteBuffer的数据写入文件
            byte[] buffer = new byte[outputBuffer.remaining()];
            outputBuffer.get(buffer);
            fileOutputStream.write(buffer);

            // 关闭文件输出流
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}