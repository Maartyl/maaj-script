/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import maaj.exceptions.ReaderException;
import maaj.reader.ReaderContext;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.util.H;

/**
 *
 * @author maartyl
 */
public class Repl {

  private final Symbol ns;
  private final Context cxt;

  Glob glob = Glob.create();

  public Repl() {
    this.ns = H.symbol("repl");
    this.cxt = glob.start(ns);
  }

  public void run(Reader r, Writer w) throws IOException {
    for (Term t : H.read(r, new ReaderContext(ns, "<?>")))
      try {
        //t.show(w);
        t.eval(cxt).show(w);
        w.append('\n');
        w.flush();
      } catch (Exception e) {
        System.err.println(e);
        e.printStackTrace(System.err);
        //throw e;
      }
      
  }

  public void runStd() {
    while (true) try {
      Reader r = new InputStreamReader(System.in);
      Writer w = new OutputStreamWriter(System.out);
      run(r, w);
    } catch (IOException e) {
      System.err.println(e);
    } catch (ReaderException e) {
      System.err.println(e);
    }
  }

  public static void main(String[] args) {
    new Repl().runStd();
  }
}
