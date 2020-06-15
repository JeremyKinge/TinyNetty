package com.kingge.tinynetty;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

public class WorkState implements HandlerState {

    public WorkState() {
    }

    @Override
    public void changeState(SubHandler h) {
        // TODO Auto-generated method stub
        h.setState(new WriteState());
    }

    @Override
    public void handle(SubHandler h, SelectionKey sk, SocketChannel sc,
            ThreadPoolExecutor pool) throws IOException {
        // TODO Auto-generated method stub

    }

}