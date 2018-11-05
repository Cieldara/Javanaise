package jvn;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/** *
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:
 *
 * Authors:
 */

import java.io.Serializable;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2750888055864804653L;
	private Set<JvnRemoteServer> clients;
    private Map<Integer, JvnObject> objects;
    private Map<String, Integer> table;
    private int lastID;
    private Map<Integer, Set<JvnRemoteServer>> clientsReading;
    private Map<Integer, JvnRemoteServer> clientWriting;

    /**
     * Default constructor
     *
     * @throws JvnException
     *
     */
    public JvnCoordImpl() throws Exception {
        clients = new HashSet<JvnRemoteServer>();
        objects = new HashMap<Integer, JvnObject>();
        table = new HashMap<String, Integer>();
        clientsReading = new HashMap<>();
        clientWriting = new HashMap<>();
        this.lastID = 0;
    }

    /**
     * Allocate a NEW JVN object id (usually allocated to a newly created JVN
     * object)
     *
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public int jvnGetObjectId() throws java.rmi.RemoteException, JvnException {
    	lastID++;
    	saveState();
        return lastID;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        table.put(jon, jo.jvnGetObjectId());
        objects.put(jo.jvnGetObjectId(), jo);
        clients.add(js);
        clientsReading.put(jo.jvnGetObjectId(), new HashSet<JvnRemoteServer>());
        clientWriting.put(jo.jvnGetObjectId(), js);
        saveState();
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {

        JvnObject obj = objects.get(table.get(jon));
        if (obj != null) {
            obj.resetState();
        }
        return obj;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {

        JvnObject obj = objects.get(joi);
        Serializable ret = obj.jvnGetObjectState();
        if (clientWriting.get(joi) != null) {
            ret = clientWriting.get(joi).jvnInvalidateWriterForReader(joi);
            clientsReading.get(joi).add(clientWriting.get(joi));
        }
        obj.jvnSetObjectState(ret);
        // On peut changer le lecteur courant en lecteur
        clientsReading.get(joi).add(js);
        clientWriting.put(joi, null);
        saveState();
        return ret;
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        JvnObject obj = objects.get(joi);
        Serializable ret = obj.jvnGetObjectState();
        if (clientWriting.get(joi) != null) {
            ret = clientWriting.get(joi).jvnInvalidateWriter(joi);
        }
        obj.jvnSetObjectState(ret);

        Iterator<JvnRemoteServer> it = clientsReading.get(joi).iterator();
        while (it.hasNext()) {
            JvnRemoteServer current = it.next();
            if (!current.equals(js)) {
                current.jvnInvalidateReader(joi);
            }
        }
        // Plus personne ne doit pouvoir être en mesure de lire
        clientsReading.get(joi).clear();
        clientWriting.put(joi, js);
        saveState();
        return ret;
    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     *
     */   
    public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        clients.remove(js);
        Set<Integer> keyset = clientWriting.keySet();
        for(Integer i : keyset){
            if(clientWriting.get(i) != null && clientWriting.get(i) == js){
                clientWriting.put(i, null);
            }
            
            Set<JvnRemoteServer> serverSet = clientsReading.get(i);
            Set<JvnRemoteServer> newSet = new HashSet<>();
            for(JvnRemoteServer server : serverSet){
                if(server != js){
                    newSet.add(server);
                }
            }
            clientsReading.put(i, newSet);
        }
        saveState();
    }
    
    private void saveState(){
    	ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream("CoordImpl.ser"));
	    	out.writeObject(this);
	    	out.flush();
	    	out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
