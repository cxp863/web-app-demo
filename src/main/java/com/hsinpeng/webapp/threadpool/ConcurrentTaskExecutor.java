package com.hsinpeng.webapp.threadpool;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xinrui.ln
 * @date 2019-08-15
 **/
@Slf4j
public class ConcurrentTaskExecutor {
    private static final int CPU = Runtime.getRuntime().availableProcessors();
    private static final long DEFAULT_TIMEOUT_IN_MILLIS = 3000L;
    private static final String DEFAULT_PREFIX = "ConcurrentTaskExecutor";
    private static final int CORE = CPU * 4, QUEUE = CPU * 32;
    private static final ThreadPoolExecutor SHARED_ASYNC_EXECUTOR = new ThreadPoolExecutor(
            CORE, CORE, 5, TimeUnit.MINUTES, Queues.newArrayBlockingQueue(QUEUE), new CallerRunsPolicy()
    );

    /**
     * 在共享线程池中提交一个任务，并获取Future
     *
     * @param callable
     * @param <V>
     * @return
     */
    public static <V> Future<V> submit(Callable<V> callable) {
        return SHARED_ASYNC_EXECUTOR.submit(callable);
    }

    /**
     * 在共享线程池中提交一个任务
     *
     * @param runnable
     * @return
     */
    public static void execute(Runnable runnable) {
        SHARED_ASYNC_EXECUTOR.execute(runnable);
    }

    /**
     * 处理并发任务
     *
     * @param callableList
     * @param <V>
     * @return
     */
    public static <V> List<V> invoke(List<Callable<V>> callableList, long timeoutInMillis) {
        return invoke(CPU * 2, callableList, null, null, timeoutInMillis);
    }

    /**
     * 处理并发任务
     *
     * @param callableList
     * @param <V>
     * @return
     */
    public static <V> List<V> invoke(List<Callable<V>> callableList) {
        return invoke(CPU * 2, callableList);
    }

    /**
     * 处理并发任务
     *
     * @param core
     * @param callableList
     * @param <V>
     * @return
     */
    public static <V> List<V> invoke(int core, List<Callable<V>> callableList) {
        return invoke(core, callableList, null, null, null);
    }

    /**
     * 处理并发任务
     *
     * @param core
     * @param callableList
     * @param <V>
     * @return
     */
    public static <V> List<V> invoke(int core, List<Callable<V>> callableList, long timeoutInMillis) {
        return invoke(core, callableList, null, null, timeoutInMillis);
    }

    /**
     * 处理并发任务
     *
     * @param core
     * @param callableList
     * @param threadPrefix
     * @param timeoutInMillis
     * @param <V>
     * @return
     */
    public static <V> List<V> invoke(int core, List<Callable<V>> callableList, List<Exception> exceptionList,
                                     String threadPrefix, Long timeoutInMillis) {
        List<Callable<V>> validateCallableList = Optional.ofNullable(callableList).orElse(Lists.newArrayList()).stream()
                .filter(Objects::nonNull).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(validateCallableList)) {
            return Lists.newArrayList();
        }

        if (timeoutInMillis == null || timeoutInMillis < 1) {
            timeoutInMillis = DEFAULT_TIMEOUT_IN_MILLIS;
        }

        threadPrefix = StringUtils.isBlank(threadPrefix) ? DEFAULT_PREFIX : StringUtils.trimToEmpty(threadPrefix);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                core, core, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(validateCallableList.size()),
                new ThreadFactoryBuilder().setNameFormat(threadPrefix + "-%d").build(),
                new CallerRunsPolicy()
        );

        List<Future<V>> futureList = null;
        try {
            futureList = executor.invokeAll(validateCallableList, timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            if (exceptionList == null) {
                executor.shutdown();
                throw new RuntimeException(e.getMessage(), e);
            } else {
                exceptionList.add(e);
            }
        }

        shutdown(executor, timeoutInMillis);

        List<Future<V>> validateFutureList = Optional.ofNullable(futureList).orElse(Lists.newArrayList())
                .stream().filter(Objects::nonNull).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(validateFutureList)) {
            return Lists.newArrayList();
        }

        return getReturnValues(exceptionList, timeoutInMillis, validateFutureList);
    }

    private static void shutdown(ThreadPoolExecutor executor, Long timeoutInMillis) {
        // Disable new tasks from being submitted
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(timeoutInMillis, TimeUnit.MILLISECONDS)) {
                // Cancel currently executing tasks
                executor.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(timeoutInMillis, TimeUnit.MILLISECONDS)) {
                    log.error("shutdown executor failed.", new Throwable());
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private static <V> List<V> getReturnValues(List<Exception> exceptionList, Long timeoutInMillis,
                                               List<Future<V>> futureList) {
        List<V> returnList = new ArrayList<>();
        for (Future<V> future : futureList) {
            if (future.isCancelled()) {
                log.warn("future cancelled, " + future);
                continue;
            }
            try {
                V value = future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
                if (value != null) {
                    returnList.add(value);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                if (exceptionList == null) {
                    throw new RuntimeException(e.getMessage(), e);
                } else {
                    exceptionList.add(e);
                }
            }
        }
        return returnList;
    }
}
