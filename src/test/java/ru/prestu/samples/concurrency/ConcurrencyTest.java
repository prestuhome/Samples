package ru.prestu.samples.concurrency;

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

}

