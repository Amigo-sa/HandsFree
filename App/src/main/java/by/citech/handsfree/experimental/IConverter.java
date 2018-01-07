package by.citech.handsfree.experimental;

public interface IConverter<F, T> {
    T getConverted(F f);
}