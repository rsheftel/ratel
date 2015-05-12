package malbec.util;

/**
 * Utility interface that helps with testing when we have to wait for a result.
 * 
 *
 * @param <T>
 */
public interface IWaitFor<T> {
    T waitFor();
}