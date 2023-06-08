package code;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FileServer extends UnicastRemoteObject implements FileServerInterface {
    public FileServer() throws RemoteException {
        super();
    }

    public void uploadFile(String clientFilePath, String serverFilePath, byte[] upFileBytes, CallbackInterface callback, long byteCount) throws RemoteException, IOException, FileNotFoundException {
    	
    	InputStream in = null;
    	OutputStream out = null;
    	
        try {
            
        	in = new ByteArrayInputStream(upFileBytes);
            out = new FileOutputStream(serverFilePath, true);
        	
            byte[] buffer = new byte[100];
            int bytesRead;
            //read the bytes sent from client side and write to server output stream
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
                //this serves the purpose of printing upload progress on client console as and when it is being uploaded to server side simultaneously
                callback.printToClientConsole(byteCount+" bytes of the file uploaded");
            }
            
            if(byteCount>0) {
            	callback.printToClientConsole("");
            	callback.printToClientConsole("UPLOAD COMPLETED SUCCESSFULLY");
	        }
            
            out.close();
            in.close();
            return;
            
        }catch (ConnectException e) {
            System.err.println("ERROR103: Loss connection to a client.");
        }catch (SocketException e) {
        	System.err.println("ERROR103: Loss connection to a client.");
        }
        finally {
            try {
                if (out!= null) {
                	out.close();
                }
                if (in != null) {
                	in.close();
                }
            } catch (IOException ex) {
            	ex.printStackTrace();
            }
        }
        
    }

    public byte[] downloadFile(String serverFilePath, String clientFilePath) throws RemoteException, IOException, FileNotFoundException {
    	
    	InputStream in = null;
    	ByteArrayOutputStream out = null;
    	
    	try{
    		
    		File servFile = new File(serverFilePath);
        	in = new FileInputStream(servFile);
        	
            out = new ByteArrayOutputStream();
            
            //checks if partial file exists on client side and skips its portion of the download
            File cliFile = new File(clientFilePath);
	    	if (cliFile.exists() && cliFile.length()>0 && cliFile.length() < servFile.length()) { 
	    		in.skip(cliFile.length());
			}
        	
            byte[] buffer = new byte[25];
            int bytesRead;
            //read bytes from server location and write it to ByteArrayOutputStream and send its bytes to client side
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            
        }catch (ConnectException e) {
            System.err.println("ERROR103: Loss connection to a client.");
        }catch (SocketException e) {
        	System.err.println("ERROR103: Loss connection to a client.");
        }finally {
            try {
                if (out!= null) {
                	out.close();
                }
                if (in != null) {
                	in.close();
                }
            } catch (IOException ex) {
            	ex.printStackTrace();
            }
        }
        return out.toByteArray();
    }
    
    
    public String listFiles(String serverDirectoryPath) throws RemoteException, IOException, FileNotFoundException {
    	
    	StringBuilder fileList = new StringBuilder();
        
    	File directory = new File(serverDirectoryPath);
    	//check if the input directory exists on server location
        if (!directory.exists()) {
        	fileList.append("ERROR105: The directory does not exist on the server.");
        	return fileList.toString();
        }
        
        if(directory.isDirectory()) {
	        File[] files = directory.listFiles();
	        if (files != null) {
	        	if(files.length!=0) {
		            for (File file : files) {
		                fileList.append(file.getName()).append("\n");
		            }
	        	}else {
	        		fileList.append("This folder is empty.");
		        	return fileList.toString();
	        	}
	        }else {
	        	fileList.append("This folder is empty.");
	        	return fileList.toString();
	        }
        }else {
        	fileList.append(directory.getName());
        }
        
        return fileList.toString();
    }
    
    
    public String createDirectory(String serverDirectoryPath) throws RemoteException, IOException, FileNotFoundException {
    	
    	String response = null;
    	
        File directory = new File(serverDirectoryPath);
        //check if input directory already exists on server side
        if (directory.exists()) {
        	response = "ERROR106: This directory already exists.";
        	return response;
        }else {
	        if (!directory.mkdirs()) {
	        	response = "ERROR107: Couldn't create the directory. Please try again.";
	        	return response;
	        }else {
	        	response = "Successfully created the directory";
	        }
        }
	        
        return response;
    }

    
    public String removeDirectory(String serverDirectoryPath) throws RemoteException, IOException, FileNotFoundException {
    	
    	String response = null;

        File directory = new File(serverDirectoryPath);
        //check if input directory exists on server and if it's a directory
        if (!directory.exists()) {
        	response = "ERROR105: The directory does not exist on the server.";
        	return response;
        }else if (!directory.isDirectory()) {
        	response = "ERROR112: Input path is a file, not a directory.";
        	return response;
        }else if (directory.listFiles().length > 0) {
        	response = "ERROR108: The directory is not empty. Cannot remove non-empty directories.";
        	return response;
        }else {
	        if (!directory.delete()) {
	        	response = "ERROR109: couldn't remove directory. Please try again.";
	        	return response;
	        }else {
	        	response = "Successfully removed the directory";
	        }
        }
    	
        return response;
    }

    
    public String deleteFile(String serverFilePath) throws RemoteException, IOException, FileNotFoundException {
    	
    	String response = null;
    	
        File file = new File(serverFilePath);
        if (!file.exists()) {
        	response = "ERROR105: The file does not exist on the server.";
        	return response;
        }else {
        	if(file.isDirectory()) {
        		response = "ERROR110: Path provided is a directory, not a file.";
        		return response;
        	}else {
		        if (!file.delete()) {
		        	response = "ERROR111: Couldn't delete the file. Please try again.";
		        	return response;
		        }else {
		        	response = "Deleted file successfully";
		        }
        	}
        
        }
    	
        return response;
    }
    
    
    public void shutdown() throws RemoteException, InterruptedException {
    	
    	//waiting for 2 seconds to allow current threads to finish execution before server shutdown
    	Thread.sleep(2000);
    	
    	//shutting down server
        System.exit(0);
    }

    
    
	public static void main(String[] args) {
        try {
        	
        	//creating the rmi registry on input port and registering server object to it
        	int port = Integer.parseInt(args[2]);
        	
        	Registry registry = LocateRegistry.createRegistry(port);
        	
        	FileServer server = new FileServer();
        	
        	registry.rebind("FileServer", server);
        	
            System.out.println("File server has started and ready to accept requests...");
            
        } catch (Exception e) {
            System.err.println("Error during start up of the file server.");
            System.err.println("This port may already be in use. Please try on a different port");
        }
    }
    
}
