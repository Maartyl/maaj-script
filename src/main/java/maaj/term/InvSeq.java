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
 * Base for FnSeq and MacroSeq.<br/>
 * This class becomes a function, from storing an an evaluable seq and closure.
 * Invocing essentially injects arguments and evaluates the saved seq.
 * <p>
 * @author maartyl
 */
public abstract class InvSeq implements InvocableSeq {

  protected final Seq fn;
  protected final Context closure;

  protected InvSeq(Seq fn, Context closure) {
    //.fmap((Invocable1) x -> x.evalMacros(closure)); //unnecessary
    if (fn.isNil()) {
      this.fn = H.list(Sym.doSymC);
      this.closure = closure;
      return;
    } 
    Term fst = fn.first();
    Map meta = fst.getMeta();
    this.fn = H.cons(Sym.doSymC, fst.unwrap(), fn.rest());
    Term selfName = meta.valAt(Sym.nameSym);
    if (!selfName.isNil())
      closure = closure.addToScope(selfName, this);
    //System.err.println(fn.firstOrNil().getMeta());
    this.closure = closure;
  }

  @Override
  public Term invokeSeq(Seq args) {
    return fn.eval(closure.addToScope(Sym.argsSymSpecial, args));
  }

  @Override
  public void show(Writer w) throws IOException {
    H.cons(getShowName(), fn.rest()).show(w);
  }

  protected abstract Symbol getShowName();
}
