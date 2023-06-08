package code;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class FileClient{

    protected FileClient() throws RemoteException {
		super();
	}


	public static void main(String[] args) {
        
		//this is the environment variable set by the client in cmd and contains rmi registry host and port
		String envVar = System.getenv("PA2_SERVER");
		System.out.println("PA2_SERVER value is " + envVar);
		
		//check if environment variable is null or empty 
		if(envVar==null) {
			System.err.println("ERR200: Please define host:port values as an env variable 'PA2_SERVER' before running client"); 
			System.err.println("NOTE: Environment variable set in a given cmd window won't be accessible in another cmd window"); 
			System.exit(1);
		}

        try {
        	
        	//retrieve host and port from environment variable
        	String[] hostPort = envVar.split(":"); 
    		String host = hostPort[0]; 
    		int port = Integer.parseInt(hostPort[1]);
    		
            String requestType = args[1];
            String sourcePath = null;
            String destPath = null;
            String serverDirectoryPath = null;
        	
            //connect to the "FileServer" object
            String serverUrl = "rmi://" + host + ":" + port + "/FileServer";
            FileServerInterface server = (FileServerInterface) Naming.lookup(serverUrl);
            FileClient client = new FileClient();

            switch (requestType) {
                case "upload":
                	sourcePath = args[2];
                    destPath = args[3];
                    client.uploadFile(server, sourcePath, destPath);
                    break;
                case "download":
                	sourcePath = args[2];
                    destPath = args[3];
                    client.downloadFile(server,sourcePath, destPath);
                    break;
                case "dir":
                    serverDirectoryPath = args[2];
                    String dirResp = server.listFiles(serverDirectoryPath);
                    System.out.println(dirResp);
                    break;
                case "mkdir":
                    serverDirectoryPath = args[2];
                    String mkdirResp = server.createDirectory(serverDirectoryPath);
                    System.out.println(mkdirResp);
                    break;
                case "rmdir":
                	serverDirectoryPath = args[2];
                	String rmdirResp = server.removeDirectory(serverDirectoryPath);
                	System.out.println(rmdirResp);
                    break;
                case "rm":
                	serverDirectoryPath = args[2];
                    String rmResp = server.deleteFile(serverDirectoryPath);
                    System.out.println(rmResp);
                    break;
                case "shutdown":
                	System.out.println("Shutting down server in 2 seconds to allow current threads to finish execution");
                    server.shutdown();
                    break;
                default:
                	System.err.println("ERR300: Invalid command. Valid command syntax is as follows: ");
                    System.err.println("java -jar pa2.jar 'server/client' 'start/upload/download/dir/mkdir/rmdir/rm' 'file path(s)' ");
                    System.exit(1);
            }
        }catch (ConnectException e) {
            System.err.println("ERROR103: Lost connection to server.");
        }catch (SocketException e) {
        	System.err.println("ERROR103: Lost connection to server.");
        }catch(RemoteException e) {
        	System.err.println("ERROR100: Lost connection to server.");
        }catch (NotBoundException | IOException e) {
        	System.err.println("ERROR102: IO Exception");
        }catch (InterruptedException e) {
        	System.err.println("ERROR104: Server shutdown was interrupted");
		}catch(ArrayIndexOutOfBoundsException e) {
			System.err.println("ERROR105: Either the PA2_SERVER env variable has not been set correctly or the command syntax is wrong.");
			System.err.println("Valid command syntax is as follows: ");
            System.err.println("java -jar pa2.jar 'server/client' 'start/upload/download/dir/mkdir/rmdir/rm' 'file path(s)' ");
		}
    
    }
    
    
    public void uploadFile(FileServerInterface server, String clientFilePath, String serverFilePath) {
    	
    	
    	InputStream in = null;
    	ByteArrayOutputStream out = null;
    	
    	File cliFile = new File(clientFilePath);
        //check if file to be uploaded exists on client side
        if (!cliFile.exists()) {
        	System.err.println("ERROR001: Requested file does not exist on client side.");
        	System.exit(1);
        }
        
        try {
        
	    	in = new FileInputStream(cliFile);
	    	
	    	long byteCount = 0;
	    	File serFile = new File(serverFilePath);
	    	
	    	//check if server already has the file to be uploaded
	    	if(cliFile.length()==serFile.length()) {
	    		System.err.println("ERROR002: This file already exists on server side.");
	        	System.exit(1);
	    	}
	    	
	    	//check if destination path already has a partially uploaded file and skip reupload of that portion.
	    	if (serFile.exists() && serFile.length()>0 && serFile.length() < cliFile.length()) { 
	    		in.skip(serFile.length());
	    		System.out.println("***********************");
	    		System.out.println("SKIPPING RE-UPLOAD OF " + serFile.length() + " BYTES");
	    		System.out.println("RESUMING UPLOAD FROM THE NEXT PORTION");
	    		System.out.println("***********************");
				byteCount = serFile.length();
			}
	    	
	    	//read data from client path and send those bytes to server side
	    	out = new ByteArrayOutputStream();
	    	byte[] byteArray = new byte[100];
	        int read = 0;
	    	while ((read = in.read(byteArray)) != -1)
	        {
	        	out.write(byteArray, 0, read);
	        }
	    	
	    	byte[] upFileBytes = out.toByteArray();
	    	
	    	//this serves the purpose of printing upload progress on client console as and when it is being uploaded to server side simultaneously
	    	CallbackImpl callback = new CallbackImpl();
	    	
	    	//calling the upload logic on server side
	    	server.uploadFile(clientFilePath, serverFilePath, upFileBytes, callback, byteCount);
	    	
	    	out.close();
	    	in.close();
	    	return;
	    	
        }catch(RemoteException e) {
        	System.out.println("ERROR100: Lost connection to server during upload.");
        }catch(FileNotFoundException e) {
        	System.out.println("ERROR101: Either the source or the destination paths provided cannot be found.");
        }catch(IOException e) {
        	System.out.println("ERROR102: Error during upload. Please try again");
        }finally {
            try {
                if (out!= null) {
                	out.close();
                }
                if (in != null) {
                	in.close();
                }
                System.exit(0);
            } catch (IOException ex) {
            	System.out.println("ERROR102: IO Exception");
            }
        }
    	
    }
    
    
    public void downloadFile(FileServerInterface server, String serverFilePath, String clientFilePath) {
    	
    	
    	OutputStream fileOutStream = null;
    	ByteArrayInputStream in = null;
    	byte[] downFileBytes = null; 
    	
    	//check if file to be downloaded exists on server side
    	File servFile = new File(serverFilePath);
        if (!servFile.exists()) {
            System.err.println("ERROR105: The file does not exist on the server.");
            System.exit(1);
        }
        
        try {
        	
        	File cliFile = new File(clientFilePath);
        	
        	//check if client already has the file to be downloaded
        	if(cliFile.length()==servFile.length()) {
	    		System.err.println("ERROR002: This file already exists on client side.");
	        	System.exit(1);
	    	}
        	
        	//calling the download logic present on server side
        	downFileBytes = server.downloadFile(serverFilePath, clientFilePath);
        	
        	in = new ByteArrayInputStream(downFileBytes);
        	
        	long byteCount = 0;
	        
	        //logic for skipping redownload is on server file. but here we checking it again only to print skipping message on client console.
	    	if (cliFile.exists() && cliFile.length()>0 && cliFile.length() < servFile.length()) { 
				System.out.println("***********************");
				System.out.println("SKIPPING RE-DOWNLOAD OF " + cliFile.length() + " BYTES");
				System.out.println("RESUMING DOWNLOAD FROM THE NEXT PORTION");
				System.out.println("***********************");
				byteCount = cliFile.length();
			}
	    	
	    	//Initializing the output stream
	        fileOutStream = new FileOutputStream(cliFile, true);
	        
	        //read the data sent from server and write it to client output stream and print progress.
	        byte[] servResponse = new byte[25];
	        int read=0;
	        while ((read = in.read(servResponse)) > 0) {
	        	fileOutStream.write(servResponse, 0, read);
	        	byteCount += read;
	            System.out.println(byteCount+" bytes of the file downloaded");
	        }
	        
	        if(byteCount>0) {
	        	System.out.println("");
	        	System.out.println("DOWNLOAD COMPLETED SUCCESSFULLY");
	        }
        	
        }catch(RemoteException e) {
        	System.out.println("ERROR100: Lost connection to server during download.");
        }catch(FileNotFoundException e) {
        	System.out.println("ERROR101: Either the source or the destination paths provided cannot be found.");
        }catch(IOException e) {
        	System.out.println("ERROR102: Error during download. Please try again.");
        }finally {
            try {
                if (fileOutStream!= null) {
                	fileOutStream.close();
                }
                if (in != null) {
                	in.close();
                }
                System.exit(0);
            } catch (IOException ex) {
            	System.out.println("ERROR102: IO Exception");
            }
        }
    	
    }

}