package ru.prestu.samples.concurrency;

public class SynchronizedStaticResource {

    private static int i = 0;

    public static int getI() {
        return i;
    }

    //Синхронизированный метод
    public synchronized static void setI(int i) {
        SynchronizedStaticResource.i = i;
    }

    public static void increaseI() {
        System.out.println(Thread.currentThread().getName() + " in queue");
        //Синхронизированный блок в методе, синхронизация на уровне класса
        synchronized(SynchronizedStaticResource.class) {
            System.out.println(Thread.currentThread().getName() + " started");
            i++;
            System.out.println(Thread.currentThread().getName() + " finished");
        }
    }

}
