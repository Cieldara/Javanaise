package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JvnCoordMain {

	public static void main(String[] args) {
		
		try{
		JvnRemoteCoord coordImpl = new JvnCoordImpl();
		
		Registry registry = LocateRegistry.createRegistry(1099);
		registry.bind("RemoteCoord", coordImpl);
		System.out.println ("Coord ready");

		} catch (Exception e) {
			System.err.println("Error on server :" + e) ;
			e.printStackTrace();
		}
	}
}
