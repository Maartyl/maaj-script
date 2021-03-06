/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.SeqH;

/**
 * basic functions that operate on evaluated arguments
 * <p>
 * @author maartyl
 */
@FunctionalInterface
public interface Fn extends Invocable {

  @Override
  public default Term apply(Context cxt, Seq args) {
    Term rslt = invokeSeq(SeqH.mapEval(H.ret1(args, args = null), cxt));
    while (rslt.isRecur())
      rslt = invokeSeq(((Recur) H.ret1(rslt, rslt = null)).getArgs());
    return rslt;
  }

  @Override
  public Term invokeSeq(Seq args);

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.fn(this, arg);
  }

}
