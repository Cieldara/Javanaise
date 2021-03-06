package irc;

/** *
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact:
 *
 * Authors:
 */

public class Sentence implements java.io.Serializable {

    String data;

    public Sentence() {
        data = new String("0");
    }

    public void write(String text) {
        data = text;
    }

    public String read() {
        return data;
    }
    @Override
    public String toString(){
        return data;
    }
}
