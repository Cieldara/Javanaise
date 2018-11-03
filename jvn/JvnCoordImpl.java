
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

    private Set<JvnRemoteServer> clients;
    private Map<Integer, JvnObject> objects;
    private Map<String, Integer> table;
    private int lastID;
    private Set<JvnRemoteServer> clientsReading;
    private JvnRemoteServer clientWriting;

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
        clientsReading = new HashSet<>();
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
        return lastID++;
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
        clientWriting = js;
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
        if (clientWriting != null) {
            ret = clientWriting.jvnInvalidateWriterForReader(joi);
        }
        obj.jvnSetObjectState(ret);
        // On peut changer le lecteur courant en lecteur
        clientsReading.add(clientWriting);
        clientsReading.add(js);
        clientWriting = null;
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
        if (clientWriting != null) {
            ret = clientWriting.jvnInvalidateWriter(joi);
        }
        obj.jvnSetObjectState(ret);

        Iterator<JvnRemoteServer> it = clientsReading.iterator();
        while (it.hasNext()) {

            JvnRemoteServer current = it.next();
            if (!current.equals(js)) {
                System.out.println("Problem");
                current.jvnInvalidateReader(joi);
            }

        }
        // Plus personne ne doit pouvoir être en mesure de lire
        clientsReading.clear();
        clientWriting = js;
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
    }
}
