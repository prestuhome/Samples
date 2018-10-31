package ru.prestu.samples.concurrency;

import static java.lang.Thread.sleep;
import static java.lang.Thread.yield;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyTest {

    @Test
    public void testThreadsCreation() {
        //Создание через Thread
        Thread thread = new MyThread();
        //Создание через Runnable
        Thread runnable = new Thread(new MyRunnable());
        //Старт потока начинается с метода start(), не путать с run()
        thread.start();
        //Старт потока второй раз выбросит исключение
        try {
            thread.start();
        } catch(Exception e) {
            assertEquals(e.getClass(), IllegalThreadStateException.class);
        }
    }

    @Test
    public void testThreadLifeCycle() {
        //New - новый поток, который можно запустить. Переходит в состояние Runnable
        //После вызова метода start() поток попадает в пул потоков, и может иметь 3 состояния: Runnable, Running, Wainting/Blocked/Sleeping
        //Runnable - поток пока не исполняется, Scheduler (планировщик запуска потоков) еще не выбрала этот поток для исполнения. Может перейти в состояние Running
        //Running - поток исполняется. Может перейти в состояния Runnable, Wainting/Blocked/Sleeping, Dead
        //Wainting/Blocked/Sleeping - поток ожидает какое-либо событие, спит или заблокирован. Может перейти в состояния Runnable
        //Dead - поток исполнился и удаляется из пула потоков.
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Переводит состояние потока из Running в Sleeping
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException ex) {
                    //Обработка нужна при прерывании потока во время сна, то есть поток спит, а в другом потоке происходит вызов thread.interrupt()
                }
                //Переводит состояние потока из Running в Runnable
                Thread.yield();
                try {
                    //Переводит состояние потока из Running в Wainting, поток может ожидать меньше указанного времени, если получает notify() или notifyAll().
                    wait(5 * 1000);
                } catch (InterruptedException ex) {
                    //Обработка нужна при прерывании потока во время ожидания, то есть поток ожидает, а в другом потоке происходит вызов thread.interrupt()
                }
            }
        };
        //Приоритет этого потока для Scheduler-а, от 1 до 10, зависит от ОС
        thread.setPriority(1);
        try {
            //Применяется, чтобы дождаться завершения потока thread, а уже затем продолжить исполнение родительского потока.
            thread.join();
        } catch (InterruptedException ex) {

        }

    }

    @Test
    public void testSynchronization() throws InterruptedException {
        SynchronizedResource resource = new SynchronizedResource();
        resource.setI(5);
        MyThread firstThread = new MyThread();
        MyThread secondThread = new MyThread();
        firstThread.resource = resource;
        secondThread.resource = resource;
        firstThread.setName("firstThread");
        secondThread.setName("secondThread");
        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();
        System.out.println(resource.getI());
        assertEquals(7, resource.getI());
    }

    @Test
    public void testStaticSynchronization() throws InterruptedException {
        SynchronizedStaticResource.setI(5);
        Thread firstThread = new Thread(new MyRunnable());
        Thread secondThread = new Thread(new MyRunnable());
        firstThread.setName("firstThread");
        secondThread.setName("secondThread");
        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();
        System.out.println(SynchronizedStaticResource.getI());
        assertEquals(7, SynchronizedStaticResource.getI());
    }

    //Для переменных, помеченных volatile, кэширование не происходит, все пишется/читается в/из основной памяти
    private volatile static int volatileVar;

    @Test
    public void testVolatileVars() throws InterruptedException {
        Thread write = new Thread() {
            @Override
            public void run() {
                while (volatileVar < 5) {
                    System.out.println("increment i to " + (++volatileVar));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        Thread read = new Thread() {
            @Override
            public void run() {
                int localVar = volatileVar;
                while (localVar < 5) {
                    if (localVar != volatileVar) {
                        System.out.println("new value is " + volatileVar);
                        localVar = volatileVar;
                    }
                }
            }
        };
        write.start();
        read.start();
        write.join();
        read.join();
    }

    //Атомарные переменные, все операции происходят последовательно
    private static AtomicInteger atomicVar = new AtomicInteger(0);

    @Test
    public void testAtomicVars() throws InterruptedException {
        class MyThread extends Thread {
            @Override
            public void run() {
                atomicVar.incrementAndGet();
            }
        }
        int expected = 10000;
        for (int i = 0; i < expected; i++) {
            MyThread thread = new MyThread();
            thread.start();
            thread.join();
        }
        assertEquals(expected, atomicVar.get());
    }

    private final List<String> list = Collections.synchronizedList(new ArrayList<>());

    @Test
    public void testWaitAndNotify() throws InterruptedException {
        class Operator extends Thread {
            @Override
            public void run() {
                yield();
                int i = 0;
                while (i < 10) {
                    synchronized (list) {
                        System.out.println("Operator add new string: " + (++i));
                        list.add(String.valueOf(i));
                        list.notify();
                        try {
                            list.wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        class Machine extends Thread {
            @Override
            public void run() {
                while (list.isEmpty()) {
                    synchronized (list) {
                        try {
                            list.wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("Machine show new string: " + list.remove(0));
                        list.notify();
                    }
                }
            }
        }
        Machine machine = new Machine();
        Operator operator = new Operator();
        machine.start();
        operator.start();
        operator.join();
    }

    @Test
    public void testLock() throws InterruptedException {
        Lock lock = new ReentrantLock();

        lock.lock();
        //блок кода, который выполняется синхронизированно
        lock.unlock();
        //Имеет ту же функцию, что и synchronized; преимущество в том, что метод lock() можно вызвать в одном методе, а метод unlock() - в другом
        class FirstThread extends Thread {
            @Override
            public void run() {
                lock.lock();
                System.out.println(getName() + " began to work");
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println(getName() + " finished work");
                lock.unlock();
                System.out.println(getName() + " lock is released");
            }
        }
        class SecondThread extends Thread {
            @Override
            public void run() {
                System.out.println(getName() + " began to work");
                while (true) {
                    if (lock.tryLock()) {
                        System.out.println(getName() + " is working");
                        lock.unlock();
                        break;
                    } else {
                        System.out.println(getName() + " is waiting");
                        try {
                            sleep(50);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        FirstThread firstThread = new FirstThread();
        SecondThread secondThread = new SecondThread();
        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();
    }

    @Test
    public void testCondition() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        class Operator extends Thread {
            @Override
            public void run() {
                yield();
                int i = 0;
                while (i < 10) {
                    lock.lock();
                    System.out.println("Operator add new string: " + (++i));
                    list.add(String.valueOf(i));
                    condition.signal();
                    try {
                        condition.await();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    lock.unlock();
                }
            }
        }
        class Machine extends Thread {
            @Override
            public void run() {
                while (list.isEmpty()) {
                    lock.lock();
                    try {
                        condition.await();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("Machine show new string: " + list.remove(0));
                    condition.signal();
                    lock.unlock();
                }
            }
        }
        Machine machine = new Machine();
        Operator operator = new Operator();
        machine.start();
        operator.start();
        operator.join();
    }

    @Test
    public void testCallable() throws Exception {
        int expected = 10;
        Callable<Integer> callable = new MyCallable(expected);
        FutureTask futureTask = new FutureTask(callable);
        new Thread(futureTask).start();
        assertEquals(expected, futureTask.get());
    }

}

