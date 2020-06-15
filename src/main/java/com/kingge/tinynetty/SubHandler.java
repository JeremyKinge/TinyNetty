package com.kingge.tinynetty;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: TinyNetty
 * @description: 从线程处理客户端的读写请求
 * @author: JeremyKing
 * @create: 2020-06-15 18:22
 **/
public class SubHandler {

    private final SelectionKey sk;
    private final SocketChannel sc;
    private static final int corePoolSize = 10;
    private static final int maximumPoolSize = 32;
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>()); // 启动线程负责当前从线程的读写逻辑，避免
    //占用当前从线程过长时间，因为我们知道一个从线程可能要处理多个客户端的读写事件。
    //例如，当前从线程在获取客户端的数据后，那么就开个线程去执行。这样当前从线程
    //就可以继续从selector取出下一个事件进行处理。

    HandlerState state; // 标识当前从线程处理的事件是读请求还是写请求

    public SubHandler(SelectionKey sk, SocketChannel sc) {
        this.sk = sk;
        this.sc = sc;
        state = new ReadState(); // 当前从线程初始处理事件肯定是读事件
    }

    public void run() {
        try {
            state.handle(this, sk, sc, pool);
        } catch (IOException e) {
            System.out.println("[Warning!] A client has been closed.");
            closeChannel();
        }
    }

    public void closeChannel() {
        try {
            sk.cancel();
            sc.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void setState(HandlerState state) {
        this.state = state;
    }
}
