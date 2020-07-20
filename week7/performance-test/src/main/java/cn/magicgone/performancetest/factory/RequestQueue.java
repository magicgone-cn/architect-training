package cn.magicgone.performancetest.factory;

public interface RequestQueue {
    void deposit(Request request);
    Request withdraw();
    int size();
}
