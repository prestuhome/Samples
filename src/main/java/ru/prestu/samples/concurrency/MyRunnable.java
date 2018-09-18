package ru.prestu.samples.concurrency;

/*
Создание своего потока, второй способ
*/
public class MyRunnable implements Runnable {

    @Override
    public void run() {
        System.out.println("Исполнение потока");
    }

}
