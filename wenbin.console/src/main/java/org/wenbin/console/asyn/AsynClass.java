package org.wenbin.console.asyn;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AsynClass {
    //ReentrantLock:重入锁需要自己在finally里释放
    //synchronized内置锁会在块执行完成后由jvm释放
    private static Lock lock = new ReentrantLock();
    private static ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Lock readLock = readWriteLock.readLock();
    private static Lock writeLock = readWriteLock.writeLock();

    public void run(ThreadType threadType) {
        switch (threadType) {
            case Cache:
                threadPoolCache();
                break;
            case Fixed:
                threadPoolFixed();
                break;
            case scheduled:
                threadPoolScheduled();
                break;
            case scheduledRate:
                threadPoolScheduledAtFixedRate();
                break;
            case Single:
                threadPoolSingle();
                break;
            case Task:
                threadFutureTask();
                break;
        }
    }
    /**
     * 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
     * 线程池为无限大，当执行第二个任务时第一个任务已经完成，会复用执行第一个任务的线程，而不用每次新建线程。
     * */
    private void threadPoolCache() {

        ExecutorService cacheThreadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            final int index = 1;
            try {
                Thread.sleep(index * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cacheThreadPool.execute(() -> System.out.println(index));
        }
    }

    /**
     * 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
     * 因为线程池大小为3，每个任务输出index后sleep 2秒，所以每两秒打印3个数字。
     * 定长线程池的大小最好根据系统资源进行设置。如Runtime.getRuntime().availableProcessors()
     * */
    private void threadPoolFixed(){

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        for(int i = 0; i<10;i++){
            final int index = 1;
            fixedThreadPool.execute(()->{
                try{
                    System.out.println(index);

                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 创建一个定长线程池，支持定时及周期性任务执行
     * 表示延迟3秒执行
     * */
    private void threadPoolScheduled(){
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
        scheduledThreadPool.schedule(()->System.out.println("delay 3 seconds"), 3, TimeUnit.SECONDS);
    }

    /**
     * 创建一个定长线程池，支持定时及周期性任务执行
     * 表示延迟1秒后每3秒执行一次
     * */
    private void threadPoolScheduledAtFixedRate(){
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
        scheduledThreadPool.scheduleAtFixedRate(()->System.out.println("delay 1 seconds, and excute every 3 seconds"), 1, 3, TimeUnit.SECONDS);
    }

    /**
     * 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行
     * 结果依次输出，相当于顺序执行各个任务。
     * */
    private void threadPoolSingle(){
        ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
        for(int i = 0; i<10;i++) {
            final int index = 1;
            singleThreadPool.execute(()->
            {
                try {
                    while(true) {
                        System.out.println(index);
                        Thread.sleep(10 * 1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void threadFutureTask(){
        ExecutorService executor = Executors.newSingleThreadExecutor();

        FutureTask<String> task = getThreadFutureTask();
        executor.execute(task);

        long begin = new Date().getTime();
        System.out.println("begin" + begin);
        try {
            // System.out.println(task.get());
            task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("end  " + (new Date().getTime() - begin));
        begin = new Date().getTime();
        System.out.println("begin" + begin);
        try {
            task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("end  " + (new Date().getTime() - begin));
        executor.shutdown();
    }
    private FutureTask<String> getThreadFutureTask(){
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                long b = new Date().getTime();
                System.out.println("call begin " + b);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 10000; i++) {
                    sb.append(i).append(",");
                }
                System.out.println("call end " + (new Date().getTime() - b));
                return sb.toString();
            }
        });
        return task;
    }
}
