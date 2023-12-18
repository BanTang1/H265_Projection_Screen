package com.zhanghao.h265receive_player;

import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Socket 客户端，用于接收
 */
public class SocketPull {
    private final String TAG = "zh___SocketPull";

    // 修改为指定的服务器地址
    private final String websocket_url = "ws://192.168.101.111:9521";
    private WebSocketClient webSocket;
    private DeCodeH265 deCodeH265;

    public SocketPull(Surface surface) {
        deCodeH265 = new DeCodeH265(surface);
    }

    public void start() {
        initWebSocket();
    }

    /**
     * 初始化WebSocket客户端
     */
    private void initWebSocket() {
        webSocket = new WebSocketClient(URI.create(websocket_url)) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i(TAG, "onOpen: ");
            }

            @Override
            public void onMessage(String message) {

            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                byte[] data = new byte[bytes.remaining()];
                bytes.get(data);
                deCodeH265.deCode(data);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.e(TAG, "onClose: ");
                initWebSocket();
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError: ex = " + ex.getMessage());
            }
        };
        webSocket.connect();
    }

}
