package com.kingge.tinynetty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

public class ReadState implements HandlerState{

    private SelectionKey sk;

    public ReadState() {
    }

    public void changeState(SubHandler h) {
        // TODO Auto-generated method stub
        h.setState(new WorkState());
    }

    public void handle(SubHandler h, SelectionKey sk, SocketChannel sc,
            ThreadPoolExecutor pool) throws IOException { // read()
        this.sk = sk;
        // non-blocking下不可用Readers，因為Readers不支援non-blocking
        byte[] arr = new byte[1024];
        ByteBuffer buf = ByteBuffer.wrap(arr);

        int numBytes = sc.read(buf); // 读取数据
        if(numBytes == -1)
        {
            System.out.println("[Warning!] A client has been closed.");
            h.closeChannel();
            return;
        }
        String str = new String(arr); //
        if ((str != null) && !str.equals(" ")) {
            h.setState(new WorkState()); // 因为处理文读事件后，那么接着就是需要注册op_write事件
            //那么需要设置写事件的处理类
            pool.execute(new WorkerThread(h, str)); // do process in worker thread
            System.out.println(sc.socket().getRemoteSocketAddress().toString()
                    + " > " + str);
        }

    }

    /*
     * 執行邏輯處理之函數
     */
    synchronized void process(SubHandler h, String str) {
        // do process(decode, logically process, encode)..
        // ..
        h.setState(new WriteState()); // 改變狀態(WORKING->SENDING)
        this.sk.interestOps(SelectionKey.OP_WRITE); // 通過key改變通道註冊的事件
        this.sk.selector().wakeup(); // 使一個阻塞住的selector操作立即返回
    }

    /*
     * 工作者線程
     */
    class WorkerThread implements Runnable {

        SubHandler h;
        String str;

        public WorkerThread(SubHandler h, String str) {
            this.h = h;
            this.str=str;
        }

        public void run() {
            process(h, str);
        }

    }
}