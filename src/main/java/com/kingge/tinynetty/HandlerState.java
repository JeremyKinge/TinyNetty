package com.kingge.tinynetty;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

public interface HandlerState {

    void changeState(SubHandler h);

    void handle(SubHandler h, SelectionKey sk, SocketChannel sc,
                ThreadPoolExecutor pool) throws IOException ;
}