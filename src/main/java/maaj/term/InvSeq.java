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
import maaj.util.Sym;

/**
 *
 * @author maartyl
 */
public abstract class InvSeq implements InvocableSeq {

  protected final Seq fn;
  protected final Context closure;

  protected InvSeq(Seq fn, Context closure) {
//    Term t = fn.fmap((Invocable1) x -> x.evalMacros(closure)).evalMacros(closure);
    Seq fn1; // I loose meta
//    if (t.unwrap() instanceof Seq)
//      fn1 = H.cons(doSym, (Seq) t.unwrap());
//    else
//      fn1 = H.list(doSym, t.unwrap());

    fn1 = H.cons(Sym.doSymC, fn).fmap((Invocable1) x -> x.evalMacros(closure));
    this.fn = fn1;
    this.closure = closure;
  }

  @Override
  public Term invokeSeq(Seq args) {
    return fn.eval(closure.addToScope(H.map(Sym.argsSymSpecial, args)));
  }

  @Override
  public void show(Writer w) throws IOException {
    H.cons(getShowName(), fn.rest()).show(w);
  }

  protected abstract Symbol getShowName();
}
