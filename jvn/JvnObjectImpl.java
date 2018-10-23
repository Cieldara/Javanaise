package jvn;

import java.io.Serializable;
import java.rmi.Remote;

public class JvnObjectImpl implements Remote, JvnObject{
	
	private Serializable obj;
	private int id;
    private enum State {NL,R,RC,W,WC,RWC};
    private State state;
	private transient JvnLocalServer localServer;
	
	public JvnObjectImpl(Serializable obj, int id, JvnLocalServer localServer) {
		this.obj = obj;
		this.id = id;
		state = State.W;
		this.localServer = localServer;
	}

	public void jvnLockRead() throws JvnException {
		switch (state){
		case NL:
		case RC:
			obj = localServer.jvnLockRead(id);
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
		switch (state){
		case NL:
		case RC:
		case WC:
			obj = localServer.jvnLockWrite(id);
			state = State.W;
		default:
		}
	}

	public void jvnUnLock() throws JvnException {
		switch (state){
		case W:
			state = State.WC;
			break;
		case R:
			state = State.RC;
		default:
		}
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
	public void jvnInvalidateReader() throws JvnException {
		state = State.NL;
	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		state = State.NL;
		return obj;
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch (state){
		case W:
			state = State.RC;
			break;
		case RWC:
			state = State.R;
		default:
		}
		return obj;
	}

	public void setLocalServer(JvnLocalServer localServer) {
		this.localServer = localServer;
	}

	@Override
	public boolean isStateRead() {
		return (state == State.R || state == State.RC);
	}

	@Override
	public boolean isStateWrite() {
		return (state == State.W || state == State.WC || state == State.RWC);
	}
        
        @Override 
        public String toString(){
            return obj.toString();
        }

}
