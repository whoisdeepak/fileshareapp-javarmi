package code;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CallbackImpl extends UnicastRemoteObject implements CallbackInterface {
    public CallbackImpl() throws RemoteException {
    	
    }

    @Override
    public void printToClientConsole(String output) throws RemoteException {
        System.out.println(output);
    }
}