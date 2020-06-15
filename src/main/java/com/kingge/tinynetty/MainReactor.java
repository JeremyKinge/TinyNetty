package com.kingge.tinynetty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @program: TinyNetty
 * @description: 主ractor线程
 * @author: JeremyKing
 * @create: 2020-06-03 17:49
 **/
public class MainReactor implements Runnable{

    private  ServerSocketChannel serverSocketChannel = null;
    private  Selector selector = null;//主reactor负责接收客户端连接的selector

    public MainReactor(int port) throws IOException {
        selector =  Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false); // 设置ServerSocketChannel为非阻塞

        MainAcceptor acceptor= new MainAcceptor(serverSocketChannel);
        SelectionKey sk = serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT); // ServerSocketChannel向selector注册
        // OP_ACCEPT事件，然后返回通道的key
        sk.attach(acceptor); // 給定key一個附加的Acceptor對象，这个对象就是用来处理accept事件的


        InetSocketAddress addr = new InetSocketAddress(port);
        serverSocketChannel.socket().bind(addr); // 在ServerSocketChannel绑定监听的端口
    }

    /**
     * 线程启动后，会监听当前主线程是否存在客户端连接
     */
    @Override
    public void run() {

        while (!Thread.interrupted()) { // 线程未中断时继续selector的轮询
            System.out.println("mainReactor waiting for new event on port: "
                    + serverSocketChannel.socket().getLocalPort() + "...");
            try {
                if (selector.select() == 0) // 如果没有事件就绪（op_accept），那么继续轮询
                    continue;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys(); // 取得所有已经就绪的事件的key集合
            Iterator<SelectionKey> it = selectedKeys.iterator();
            while (it.hasNext()) {
                dispatch((SelectionKey) (it.next())); // 处理事件
                it.remove();
            }
        }

    }


    /**
     * 监听到事件后，那么就交由当前key的附加对象去处理本次事件
     * @param key
     */
    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable) (key.attachment());
        if (r != null)
            r.run();
    }

}
