package by.citech.server.network.util;

/**
 * Represents a simple factory
 * 
 * @author LordFokas
 * @param <T>
 *            The Type of object to create
 */
public interface IFactory<T> {

    T create();
}
