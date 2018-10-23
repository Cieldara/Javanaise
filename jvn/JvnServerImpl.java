/** *
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:
 *
 * Authors:
 */
package jvn;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.io.*;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

    // A JVN server is managed as a singleton 
    private static JvnServerImpl js = null;
    private JvnRemoteCoord coord;

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
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
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
    public void jvnTerminate() throws jvn.JvnException {
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
    
    //TODO Decide Id (récupérer un id unique dans le serveur)
    public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
    	JvnObject obj = new JvnObjectImpl(o, 1, this);
        return obj;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo : the JVN object
     * @throws JvnException
	*
     */
    public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
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
    public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
    	JvnObject obj = null;
    	try {
			obj = coord.jvnLookupObject(jon, this);
			if (obj != null)
				obj.setLocalServer(this);
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
    	Serializable obj = null;
    	try {
			obj = coord.jvnLockRead(joi, this);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
        return obj;
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
    	Serializable obj = null;
    	try {
			obj = coord.jvnLockWrite(joi, this);
		} catch (RemoteException e) {
			e.printStackTrace();
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
    public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        
    }

    ;
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed 
        return null;
    }

    ;
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed 
        return null;
    }
;

}
