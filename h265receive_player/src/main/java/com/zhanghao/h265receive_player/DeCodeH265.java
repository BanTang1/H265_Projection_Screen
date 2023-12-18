package com.zhanghao.h265receive_player;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 解码H265视频
 */
public class DeCodeH265 {


    private Surface surface;
    private MediaCodec mediaCodec;

    private int width = 720;
    private int height = 1280;

    public DeCodeH265(Surface surface) {
        this.surface = surface;
        initDecoder();
    }

    /**
     * 初始化解码器
     */
    private void initDecoder() {
        try {
            final MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
            format.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            mediaCodec.configure(format,
                    surface,
                    null, 0);
            mediaCodec.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 开始解码
     *
     * @param data
     */
    public void deCode(byte[] data) {
        // 提交数据到解码器
        int inputBufferID = mediaCodec.dequeueInputBuffer(10000);
        if (inputBufferID >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferID);
            assert inputBuffer != null;
            inputBuffer.clear();
            inputBuffer.put(data);
            mediaCodec.queueInputBuffer(inputBufferID, 0, data.length, System.currentTimeMillis(), 0);
        }

        // 从解码器获取数据
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferID = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
        // 即使只提交了一份数据到解码器的输入缓冲区，但解码器的输出并不一定是一一对应的，它可能在一次解码操作中产生多个输出缓冲区
        while (outputBufferID >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferID, true);
            outputBufferID = mediaCodec.dequeueOutputBuffer(bufferInfo,1000);
        }
    }
}
