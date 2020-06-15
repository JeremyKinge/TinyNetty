package com.kingge.tinynetty;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @program: TinyNetty
 * @description:  从线程负责处理读写事件
 * @author: JeremyKing
 * @create: 2020-06-03 18:08
 **/
public class SubReactor implements Runnable {

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private boolean restart = false;
    int num;

    public SubReactor(Selector selector, ServerSocketChannel serverSocketChannel, int i) {
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
        this.num = num;
    }

    public void run() {
        while (!Thread.interrupted()) {

            while (!Thread.interrupted() && !restart) { //在线程被中断前以及被指定重启前持续运行
                try {
                    if (selector.select() == 0)
                        continue; // 若沒有事件就绪則不往下执行
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys(); // 取得所有已就緒事件的key集合
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {
                    dispatch((SelectionKey) (it.next())); // 根据事件的key，获取key对应的附加对象进行处理本次事件
                    it.remove();
                }
            }
        }
    }


    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable) (key.attachment());
        if (r != null)
            r.run();
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }
}
