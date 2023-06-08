package code;

public class Controller {

	public static void main(String[] args) {
		
		if("server".equalsIgnoreCase(args[0])) {
			
			FileServer.main(args);
        
		}else if("client".equalsIgnoreCase(args[0])) {
			
			FileClient.main(args);
			
		}else {
			System.err.println("ERR300: Invalid command. Valid command syntax is as follows: ");
            System.err.println("java -jar pa2.jar 'server/client' 'start/upload/download/dir/mkdir/rmdir/rm' 'file path(s)' ");
		}

	}

}
