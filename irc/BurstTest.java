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

        if (jo == null) {
            jo = js.jvnCreateObject((Serializable) new Sentence());
            // after creation, I have a write lock on the object
            jo.jvnUnLock();
            js.jvnRegisterObject("IRC", jo);
        }

        for (int i = 0; i < 10; i++) {
            Integer entier = Integer.parseInt(jo.read());
            System.out.println(args[0] + ":" + entier++ + " " +i);
            jo.write(entier.toString());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(args[0] + ": Fin de boucle");
        js.jvnTerminate();
        System.out.println(args[0] + ": Fin du processus");
        System.exit(0);
    }
}
