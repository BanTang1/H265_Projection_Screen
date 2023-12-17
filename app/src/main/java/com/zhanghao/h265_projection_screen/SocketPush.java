package com.zhanghao.h265_projection_screen;

import android.media.projection.MediaProjection;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 服务端Socket，用于推流
 */
public class SocketPush {

    private final String TAG = "zh___SocketPush";
    private WebSocket webSocket;
    private EncodeH265 encodeH265;


    private WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(9521)) {
        @Override
        public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
            SocketPush.this.webSocket = webSocket;
        }

        @Override
        public void onClose(WebSocket webSocket, int i, String s, boolean b) {
            Log.i(TAG, "onClose: 关闭 socket ");
        }

        @Override
        public void onMessage(WebSocket webSocket, String s) {
        }

        @Override
        public void onError(WebSocket webSocket, Exception e) {
            Log.i(TAG, "onError:  " + e.toString());
        }

        @Override
        public void onStart() {

        }
    };

    /**
     * 开始推流，初始化 webSocketServer ，并启动编码线程
     *
     * @param mediaProjection 媒体投影
     */
    public void start(MediaProjection mediaProjection) {
        webSocketServer.start();
        encodeH265 = new EncodeH265(mediaProjection, this);
        encodeH265.startEncodeThread();
    }

    /**
     * 发送码流数据
     *
     * @param bytes 码流数据
     */
    public void sendData(byte[] bytes) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(bytes);
        }
    }

    /**
     * 关闭闭 socket 服务 , 同时停止编码线程
     */
    public void close() {
        try {
            webSocket.close();
            webSocketServer.stop();
            encodeH265.stopEncodeThread();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
