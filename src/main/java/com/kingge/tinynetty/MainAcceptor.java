package com.kingge.tinynetty;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @program: TinyNetty
 * @description: 主线程真正负责客户端连接的类
 * @author: JeremyKing
 * @create: 2020-06-03 18:02
 **/
public class MainAcceptor  implements  Runnable{

    private final ServerSocketChannel serverSocketChannel;
    private final int cores = Runtime.getRuntime().availableProcessors(); // 取得CPU核心數
    private final Selector[] selectors = new Selector[cores]; // 根据cpu核心数，创建相应的主从模式下的从线程组对应的selector
    private int freeIndex = 0; // 当前可使用的subReactor索引
    private SubReactor[] r = new SubReactor[cores]; // subReactor线程，每个线程都会传入一个上面创建的selector
    private Thread[] t = new Thread[cores]; // subReactor线程

    public MainAcceptor(ServerSocketChannel serverSocketChannel) throws IOException {
        this.serverSocketChannel = serverSocketChannel;
        // 创建多个从线程，并给每个从线程分配一个selector
        // 然后启动从线程
        for (int i = 0; i < cores; i++) {
            selectors[i] = Selector.open();
            r[i] = new  SubReactor(selectors[i], serverSocketChannel, i);
            t[i] = new Thread(r[i]);
            t[i].start();
        }
    }

    @Override
    public synchronized void run() {
        try {
            SocketChannel sc = serverSocketChannel.accept(); // 获取客户端的连接请求
            System.out.println(sc.socket().getRemoteSocketAddress().toString()
                    + " is connected.");

            if (sc != null) {//从上面的从线程组中取出一个从线程负责处理客户端后续的读写事件
                sc.configureBlocking(false); // 设置為非阻塞
                r[freeIndex].setRestart(true); // 暂停线程
                selectors[freeIndex].wakeup(); // 使一個阻塞住的selector操作立即返回
                SelectionKey sk = sc.register(selectors[freeIndex],
                        SelectionKey.OP_READ); // SocketChannel向selector[freeIndex]注册OP_READ事件，然后返回该通道的key
                selectors[freeIndex].wakeup(); // 使一個阻塞住的selector操作立即返回
                r[freeIndex].setRestart(false); // 重启线程
                sk.attach(new SubHandler(sk, sc)); // 給定key一个附加的SubHandler对象，负责处理本次的事件

                if (++freeIndex == selectors.length)//如果从线程已经从第一个使用到最后一个，那么
                    //接着从0开始取从线程
                    freeIndex = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
