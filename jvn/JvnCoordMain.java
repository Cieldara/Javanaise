package jvn;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JvnCoordMain {

	public static void main(String[] args) {
		
		try{
		JvnRemoteCoord coordImpl = null;
		Boolean debug = true;
		if (debug){
	    	try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream("CoordImpl.ser"));
				coordImpl = (JvnCoordImpl) in.readObject();
				in.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
    	if (coordImpl == null){
    		coordImpl = new JvnCoordImpl();
    	}
		Registry registry = LocateRegistry.createRegistry(1099);
		registry.bind("RemoteCoord", coordImpl);
		System.out.println ("Coord ready");

		} catch (Exception e) {
			System.err.println("Error on server :" + e) ;
			e.printStackTrace();
		}
	}
}


