package irc;

import java.io.Serializable;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gontardb
 */
public class BurstTest {

    public static void main(String[] args) throws JvnException {

        JvnServerImpl js = JvnServerImpl.jvnGetServer();
        // look up the IRC object in the JVN server
        // if not found, create it, and register it in the JVN server
        JvnObject jo = js.jvnLookupObject("IRC");
        
        int id = 0;
        
        if(args.length >= 1){
            id = Integer.parseInt(args[0]);
        }
        if (jo == null) {
            jo = js.jvnCreateObject((Serializable) new Sentence());
            // after creation, I have a write lock on the object
            jo.jvnUnLock();
            js.jvnRegisterObject("IRC", jo);
        }
        for (int i = 0; i < 1000; i++) {
            Integer entier = Integer.parseInt(jo.read());
            System.out.println(id + ":" + (entier++));
            jo.write(entier.toString());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        js.jvnTerminate();
        System.out.println(id + ": Fin du processus");
        System.exit(0);
    }
}
