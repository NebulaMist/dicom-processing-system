package com.dicom.application;

import javax.swing.JTextArea;

/**
 * 线程测试类，用于向文本区域输出指定的字符串
 * 支持线程同步机制控制
 */
public class ThreadTester extends Thread {
    private JTextArea output;
    private String instr;
    private static final Object lock = new Object(); // 用于线程同步的锁
    private static volatile boolean thread1Turn = true; // 控制线程交替执行

    public ThreadTester(JTextArea out, String in) {
        output = out;
        instr = in;
    }    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (lock) {                // 线程1输出"1"时，需要等待轮到它
                if ("1".equals(instr)) {
                    while (!thread1Turn && !Thread.currentThread().isInterrupted()) {
                        try {
                            lock.wait(50); // 缩短超时时间，提高响应速度
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    if (Thread.currentThread().isInterrupted()) return;
                    // 输出并切换到线程2
                    output.append(instr);
                    thread1Turn = false;
                    lock.notifyAll();
                }
                // 线程2输出"2"时，需要等待轮到它
                else if ("2".equals(instr)) {
                    while (thread1Turn && !Thread.currentThread().isInterrupted()) {
                        try {
                            lock.wait(50); // 缩短超时时间，提高响应速度
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    if (Thread.currentThread().isInterrupted()) return;
                    // 输出并切换到线程1
                    output.append(instr);
                    thread1Turn = true;
                    lock.notifyAll();
                }
            }
              // 添加短暂延迟，让界面有时间更新
            try {
                Thread.sleep(50); // 缩短延迟时间，提高响应速度
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * 重置线程同步状态
     */
    public static void resetSync() {
        synchronized (lock) {
            thread1Turn = true;
            lock.notifyAll();
        }
    }
}
