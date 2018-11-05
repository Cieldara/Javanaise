package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import irc.Sentence;

public class ProxyInvocationHandler implements InvocationHandler, Serializable{

    private final JvnObject target;

    public ProxyInvocationHandler(JvnObject obj){
        this.target = obj;
    }
 
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) 
      throws Throwable {
          Object retour = null;

        if(method.getName().equals("read")){
            target.jvnLockRead();
            retour = ((Sentence) (target.jvnGetObjectState())).read();
            target.jvnUnLock();
        }
        else if(method.getName().equals("write")){
            target.jvnLockWrite();
            ((Sentence) (target.jvnGetObjectState())).write((String)args[0]);
            target.jvnUnLock();
        }
        else{
            retour = method.invoke(target, args);
        }

        return retour;
    }
}