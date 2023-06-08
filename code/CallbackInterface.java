package code;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackInterface extends Remote {
    void printToClientConsole(String output) throws RemoteException;
}