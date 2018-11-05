package jvn;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JvnObjectImpl implements Remote, JvnObject {

    private Serializable obj;
    private int id;

    @Override
    public String read() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void write(String s) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    public enum State {
        NL, R, RC, W, WC, RWC
    };

    private State state;
    private transient JvnLocalServer localServer;

    public JvnObjectImpl(Serializable obj, int id, JvnLocalServer localServer) {
        this.obj = obj;
        this.id = id;
        state = State.W;
        this.localServer = localServer;
    }

    public void jvnLockRead() throws JvnException {

        System.out.println("Read " + state);
        switch (state) {
        case NL:
            state = State.R;
            obj = localServer.jvnLockRead(id);

            break;
        case RC:
            state = State.R;
            break;
        case WC:
            state = State.RWC;
            break;
        default:
            break;
        }
    }

    public void jvnLockWrite() throws JvnException {
        switch (state) {
        case NL:
        case RC:
            state = State.W;
            obj = localServer.jvnLockWrite(id);

        default:
        }
    }

    public synchronized void jvnUnLock() throws JvnException {

        switch (state) {
        case W:
            state = State.WC;
            break;
        case R:
            state = State.RC;
        default:
        }
        this.notify();
    }

    public int jvnGetObjectId() throws JvnException {
        return id;
    }

    public Serializable jvnGetObjectState() throws JvnException {
        return obj;
    }

    public void jvnSetObjectState(Serializable obj) {
        this.obj = obj;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        while (this.state == State.R) {
            try {
                this.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(JvnObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        state = State.NL;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        while (state == State.W) {
            try {
                this.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(JvnObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        state = State.NL;
        return obj;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        switch (state) {
        case W:
            while (this.state == State.W) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(JvnObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            state = State.RC;
            break;
        case WC:
        case RWC:
            state = State.RC;
        default:
        }
        return obj;
    }

    @Override
    public synchronized void jvnInvalidateFailure() {
    	state = State.NL;
    }

    public void setLocalServer(JvnLocalServer localServer) {
        this.localServer = localServer;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    public void resetState() {
        this.state = State.NL;
    }


}
