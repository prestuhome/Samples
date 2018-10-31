package ru.prestu.samples.concurrency;

import java.util.concurrent.Callable;

//Выполняет те же функции, что и интерфейс Runnable, плюс к этому возвращает значение
public class MyCallable implements Callable<Integer> {

    private final int val;

    public MyCallable(int val) {
        this.val = val;
    }

    @Override
    public Integer call() throws Exception {
        return val;
    }

}
