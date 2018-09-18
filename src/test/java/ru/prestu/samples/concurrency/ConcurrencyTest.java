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
                    //Состояние Sleeping появляется при вызове метода Thread.sleep(time)
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException ex) {
                    //Обработка нужна при прерывании потока во время сна, то есть поток спит, а в другом потоке происходит вызов thread.interrupt()
                }
                //Уступает место другому потоку
                Thread.yield();
            }
        };
        //Приоритет этого потока для Scheduler-а, от 1 до 10, зависит от ОС
        thread.setPriority(1);

    }

}
