package ru.prestu.samples.concurrency;

//Пока один из потоков исполняет синхронизированный блок кода, другие потоки, подошедшие к исполнению этого кода, будут залочены
public class SynchronizedResource {

    private int i = 0;

    public int getI() {
        return i;
    }

    //Синхронизированный метод
    public synchronized void setI(int i) {
        this.i = i;
    }

    public void increaseI() {
        System.out.println(Thread.currentThread().getName() + " in queue");
        //Синхронизированный блок в методе, синхронизация на уровне объекта
        synchronized(this) {
            System.out.println(Thread.currentThread().getName() + " started");
            i++;
            System.out.println(Thread.currentThread().getName() + " finished");
        }
    }

}
