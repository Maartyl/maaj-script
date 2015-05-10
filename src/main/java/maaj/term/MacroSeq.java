/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;
import maaj.util.H;

/**
 * basic functions that operate on evaluated arguments
 * <p>
 * @author maartyl
 */
public final class MacroSeq implements Macro {

  private final Seq fn;
  private final Context closure;

  protected MacroSeq(Seq fn, Context closure) {
    this.fn = fn;
    this.closure = closure;
  }

  @Override
  public Term invokeSeq(Seq args) {
    return fn.eval(closure.addToScope(H.map(argsSym, args)));
  }

  @Override
  public void show(Writer w) throws IOException {
    H.list(H.symbol("#/macro"), fn).show(w);
  }

  protected static final Symbol argsSym = H.symbol("$args");

  public static MacroSeq of(Seq fn, Context closure) {
    return new MacroSeq(fn.fmap((Invocable1) x -> x.evalMacros(closure)), closure);
  }


}
