package com.hsinpeng.webapp.threadpool;

import com.google.common.collect.Queues;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadPool {
    private static final ThreadPoolExecutor TPE = new ThreadPoolExecutor(1, 1000,
            5, TimeUnit.DAYS, Queues.newSynchronousQueue(), new ThreadPoolExecutor.DiscardPolicy());
}
