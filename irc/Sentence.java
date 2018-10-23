/** *
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact:
 *
 * Authors:
 */
package irc;

public class Sentence implements java.io.Serializable {

    String data;

    public Sentence() {
        data = new String("Toto");
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
