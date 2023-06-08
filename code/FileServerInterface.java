package code;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileServerInterface extends Remote {
	void uploadFile(String clientFilePath, String serverFilePath, byte[] upFileBytes, CallbackInterface callback, long byteCount) throws RemoteException, IOException;
	byte[] downloadFile(String serverFilePath, String clientFilePath) throws RemoteException, IOException;
    String listFiles(String serverDirectoryPath) throws RemoteException, IOException;
    String createDirectory(String serverDirectoryPath) throws RemoteException, IOException;
    String removeDirectory(String serverDirectoryPath) throws RemoteException, IOException;
    String deleteFile(String serverFilePath) throws RemoteException, IOException;
    void shutdown() throws RemoteException, InterruptedException;
}