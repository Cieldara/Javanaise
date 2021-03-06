package jvn;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JvnCoordMain {

    public static void main(String[] args) {

        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            JvnRemoteCoord coordImpl = null;
            Boolean debug = true;
            if (debug) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream("CoordImpl.ser"));
                    coordImpl = (JvnCoordImpl) in.readObject();
                    in.close();
                } catch (IOException | ClassNotFoundException e) {
                }
            }
            if (coordImpl == null) {
                coordImpl = new JvnCoordImpl();
            } else {
                ((JvnCoordImpl) coordImpl).jvnInvalideFailure();
            }
            
            registry.bind("RemoteCoord", coordImpl);
            System.out.println("Coord ready");

        } catch (Exception e) {
            System.err.println("Error on server :" + e);
            e.printStackTrace();
        }
    }
}
