package jvn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * *
 * JAVANAISE Implementation JvnServerImpl class Contact:
 *
 * Authors:
 */
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
    private Map<String, Boolean> objectExisting;
    private final Integer lock = new Integer(5);
    private int termine = 0;

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
        objectExisting = new HashMap<>();
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
     * @param jo : the JVN object
     * @param joi : the JVN object identification
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        synchronized (lock) {
            if (objectExisting.get(jon).equals(Boolean.TRUE)) {
                table.put(jon, jo.jvnGetObjectId());
                objects.put(jo.jvnGetObjectId(), jo);
                clients.add(js);
                clientsReading.put(jo.jvnGetObjectId(), new HashSet<JvnRemoteServer>());
                clientWriting.put(jo.jvnGetObjectId(), js);
                objectExisting.put(jon, Boolean.FALSE);
                lock.notifyAll();
                saveState();
            }
        }
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        JvnObject obj = null;
        synchronized (lock) {
            obj = objects.get(table.get(jon));

            if (obj == null) {
                if (objectExisting.get(jon) == null) {
                    objectExisting.put(jon, Boolean.TRUE);
                } else {
                    while (objectExisting.get(jon).equals(Boolean.TRUE)) {
                        try {
                            lock.wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    obj = objects.get(table.get(jon));
                }

            }
            if (obj != null) {
                clients.add(js);
                saveState();
                obj.resetState();
            }
        }
        return obj;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        JvnObject obj = null;
        Serializable ret = null;
        JvnRemoteServer server = null;
        synchronized (lock) {
            obj = objects.get(joi);
            ret = obj.jvnGetObjectState();
            server = clientWriting.get(joi);
        }

        if (server != null) {
            ret = server.jvnInvalidateWriterForReader(joi);
            synchronized (lock) {
                //Si le Writer récupéré est encore le même, le passer dans les readers, sinon ne rien faire
                if (server.equals(clientWriting.get(joi))) {
                    clientsReading.get(joi).add(server);
                }
            }
        }
        synchronized (lock) {
            obj.jvnSetObjectState(ret);
            saveState();
            clientsReading.get(joi).add(js);
            clientWriting.put(joi, null);
        }
        return ret;
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        JvnObject obj = null;
        Serializable ret = null;
        JvnRemoteServer server = null;
        synchronized (lock) {
            obj = objects.get(joi);
            ret = obj.jvnGetObjectState();
            server = clientWriting.get(joi);
        }

        if (server != null) {
            ret = server.jvnInvalidateWriter(joi);
            synchronized (lock) {
                if (server.equals(clientWriting.get(joi))) {
                    clientsReading.get(joi).add(server);
                }
            }
        }

        ArrayList<JvnRemoteServer> readerList = new ArrayList<>();
        synchronized (lock) {
            obj.jvnSetObjectState(ret);
            saveState();
            Iterator<JvnRemoteServer> it = clientsReading.get(joi).iterator();
            while (it.hasNext()) {
                JvnRemoteServer current = it.next();
                if (!current.equals(js)) {
                    readerList.add(current);
                }
            }
        }

        for (JvnRemoteServer s : readerList) {
            s.jvnInvalidateReader(joi);
        }

        // Plus personne ne doit pouvoir être en mesure de lire
        synchronized (lock) {
            clientsReading.get(joi).clear();
            clientWriting.put(joi, js);
        }
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
        synchronized (lock) {
            clients.remove(js);
            Set<Integer> keyset = new HashSet<>();
            keyset = clientWriting.keySet();
            for (Integer i : keyset) {
                if (clientWriting.get(i) != null && clientWriting.get(i).equals(js)) {
                    clientWriting.put(i, null);
                }
                Set<JvnRemoteServer> serverSet = clientsReading.get(i);
                Set<JvnRemoteServer> newSet = new HashSet<>();
                for (JvnRemoteServer server : serverSet) {
                    if (!server.equals(js)) {
                        newSet.add(server);
                    }
                }
                clientsReading.put(i, newSet);
                saveState();
            }

        }
    }

    public void jvnInvalideFailure() throws JvnException {
        for (JvnRemoteServer client : clients) {
            Boolean connect = false;
            for (int i = 0; i < 10 && !connect; i++) {
                try {
                    client.jvnInvalidateFailure();
                    connect = true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if (!connect) {
                clients.remove(client);
            }
        }
        Set<Integer> keyset = clientsReading.keySet();
        for (Integer i : keyset) {
            clientsReading.get(i).clear();
            clientWriting.put(i, null);
        }
    }

    private void saveState() {
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
