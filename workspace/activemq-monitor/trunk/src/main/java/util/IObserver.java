package util;

public interface IObserver<N> {

    void onUpdate(N notificationObject);
}
