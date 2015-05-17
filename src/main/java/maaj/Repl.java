/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import maaj.exceptions.ReaderException;
import maaj.lang.Context;
import maaj.lang.Glob;
import maaj.reader.ReaderContext;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.util.H;
import maaj.util.MapH;

/**
 * Interactive read evaluate print loop. <br/>
 * You can also how Maaj Script can be used from Java.
 * - It might be better to provide some object with these methods instead, but
 * I will do that with Java scripting API directly.
 * <p>
 * @author maartyl
 */
public class Repl {

  private final Symbol ns;
  private final Context cxt;

  Glob glob = Glob.create();

  public Repl() {
    this.ns = H.symbol("repl");
    this.cxt = glob.start(ns);
    //H.eval("(def " + PRINTSTACKTRACE + " ())", cxt);
    cxt.def(PRINTSTACKTRACE, H.NIL, MapH.emptyPersistent());
  }

  public void run(Reader r, Writer w) throws IOException {
    w.append(">");
    w.flush();
    for (Term t : H.read(r, new ReaderContext(ns, "<?>")))
      try {
        //t.eval(cxt).show(w);
        H.eval(t, cxt).show(w);
        w.append("\n>");
        w.flush();
      } catch (Exception e) {
        if (!H.eval(PRINTSTACKTRACE, cxt).isNil())
          e.printStackTrace(System.err);
        else System.err.println(e);
        w.append(">");
        w.flush();
        //throw e;
      }
      
  }

  public void runStd() {
    while (true) try {
      Reader r = new InputStreamReader(System.in);
      Writer w = new OutputStreamWriter(System.out);
      run(r, w);
      break; //if runs to end fine : exit; otherwise repeat after exception (in reader)
    } catch (ReaderException e) {
      if (!H.eval(PRINTSTACKTRACE, cxt).isNil())
        e.printStackTrace(System.err);
      else System.err.println(e);
    } catch (IOException  e) {
      System.err.println(e);
      break;
    }
  }
  /**
   * This variable is in REPL interpreted as whether stack trace of exceptions should be printed or not
   */
  private static final Symbol PRINTSTACKTRACE = H.symbol("*print-stack-trace*");

  public static void main(String[] args) {
    new Repl().runStd();
  }

}
