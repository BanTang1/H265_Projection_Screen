package com.zhanghao.h265receive_player;

import android.app.Activity;
import android.media.MediaCodec;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhanghao.h265receive_player.databinding.LayoutBinding;

import java.io.IOException;

/**
 * 接收端，播放H265视频
 */
public class MainActivity extends Activity {

    private LayoutBinding binding;
    private Surface surface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                surface = holder.getSurface();
                SocketPull socketPull = new SocketPull(surface);
                socketPull.start();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }
}
