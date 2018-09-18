package ru.prestu.samples.concurrency;

/*
Создание своего потока, первый способ
*/
public class MyThread extends Thread {

    @Override
    public void run() {
        System.out.println("Исполнение потока");
    }

}
