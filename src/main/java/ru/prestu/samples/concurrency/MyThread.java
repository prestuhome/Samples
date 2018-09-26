package ru.prestu.samples.concurrency;

/*
Создание своего потока, первый способ
*/
public class MyThread extends Thread {

    SynchronizedResource resource;

    @Override
    public void run() {
        System.out.println("Исполнение потока");
        if (resource != null) resource.increaseI();
    }

}
