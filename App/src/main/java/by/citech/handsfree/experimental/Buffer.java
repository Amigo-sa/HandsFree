package by.citech.handsfree.experimental;

import java.util.Collection;

public abstract class Buffer<T, FB> {

    private BufferConfig config;
    private ITx<Collection<T>> dst;

    abstract void buffer(T t);
    abstract void release();

}