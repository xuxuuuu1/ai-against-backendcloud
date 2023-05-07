package com.kob.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    // 线程不安全，但我们会加锁使线程安全
    private final Queue<Bot> bots = new LinkedList<>();

    public void addBot(Integer botId, String botCode, String input) {
        lock.lock();
        try {
            bots.add(new Bot(botId, botCode, input));
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void consume(Bot bot) {
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);
    }

    // 生产者消费者模型
    @Override
    public void run() {
        while (true) {
            lock.lock();
            if (bots.isEmpty()) {
                try {
                    // 包含解锁
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = bots.remove();
                lock.unlock();
                consume(bot);  // 可能执行时间比较久
            }
        }
    }
}
