package jvn;

/** *
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:
 *
 * Authors:
*/

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Proxy;



public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;
    private JvnRemoteCoord coord;
    private Map<Integer, JvnObject> objects;
    private int id;

    /**
     * Default constructor
     *
     * @throws JvnException
     *
     */
    private JvnServerImpl() throws Exception {
        super();
        Registry registry = LocateRegistry.getRegistry(1099);
        try {
            coord = (JvnRemoteCoord) registry.lookup("RemoteCoord");
            id = coord.jvnGetObjectId();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        objects = new HashMap<Integer, JvnObject>();
    }

    /**
     * Static method allowing an application to get a reference to a JVN server
     * instance
     *
     * @throws JvnException
     *
     */
    public static JvnServerImpl jvnGetServer() {
        if (js == null) {
            try {
                js = new JvnServerImpl();
            } catch (Exception e) {
                return null;
            }
        }
        return js;
    }

    /**
     * The JVN service is not used anymore
     *
     * @throws JvnException
     *
     */
    public void jvnTerminate() throws JvnException {
        try {
            coord.jvnTerminate(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * creation of a JVN object
     *
     * @param o : the JVN object state
     * @throws JvnException
     *
     */
    public JvnObject jvnCreateObject(Serializable o) throws JvnException {
        int id = 0;
        try {
            id = coord.jvnGetObjectId();
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        JvnObject obj = new JvnObjectImpl(o, id, this);
        ProxyInvocationHandler handler = new ProxyInvocationHandler(obj);
        JvnObject object = (JvnObject) Proxy.newProxyInstance(JvnObject.class.getClassLoader(),new Class[] { JvnObject.class }, handler);

        
        objects.put(obj.jvnGetObjectId(), object);
        return object;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     *
     */
    public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException {
        try {
            coord.jvnRegisterObject(jon, jo, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     *
     */
    public synchronized JvnObject jvnLookupObject(String jon) throws JvnException {
        JvnObject obj = null;
        try {
            obj = coord.jvnLookupObject(jon, this);
            if (obj != null) {
                obj.setLocalServer(this);
                objects.put(obj.jvnGetObjectId(), obj);

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     *
     */
    public Serializable jvnLockRead(int joi) throws JvnException {
        Serializable obj = this.objects.get(joi);
        int connected = 0;
        while (connected < 10) {
            try {
                obj = coord.jvnLockRead(joi, this);
                connected = 11;
            } catch (RemoteException e) {
                //e.printStackTrace();
                connected++;
                connectToCoord();
            }
        }
        return obj;
    }

    private void connectToCoord() {  
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            coord = (JvnRemoteCoord) registry.lookup("RemoteCoord");
        } catch (RemoteException | NotBoundException e) {
        }

    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     *
     */
    public Serializable jvnLockWrite(int joi) throws JvnException {
        Serializable obj = this.objects.get(joi);
        int connected = 0;
        while (connected < 10) {
            try {
                obj = coord.jvnLockWrite(joi, this);
                connected = 11;
            } catch (RemoteException e) {
                //e.printStackTrace();
                connected++;
                connectToCoord();
            }
        }
        return obj;
    }

    /**
     * Invalidate the Read lock of the JVN object identified by id called by the
     * JvnCoord
     *
     * @param joi : the JVN object id
     * @return void
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, JvnException {
        objects.get(joi).jvnInvalidateReader();
    }

    ;

    /**
     * Invalidate the Write lock of the JVN object identified by id
     * 
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, JvnException {
        return objects.get(joi).jvnInvalidateWriter();
    }

    ;

    /**
     * Reduce the Write lock of the JVN object identified by id
     * 
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, JvnException {
        return objects.get(joi).jvnInvalidateWriterForReader();
    };
    
    @Override
    public void jvnInvalidateFailure() throws RemoteException, JvnException {
    	objects.forEach((i,o) -> o.jvnInvalidateFailure());  	
    }

    @Override
    public boolean equals(Object j) {
        return this.id == ((JvnServerImpl) j).id;
    }

}
