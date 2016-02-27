/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.term.visitor.Visitor;

/**
 * A Fn that doesn't evaluate it's args and
 * returns an expression tree to be evaluated instead of itself.
 * <p>
 * @author maartyl
 */
@FunctionalInterface
public interface Macro extends Invocable {

  @Override
  public default Term apply(Context cxt, Seq args) {
    return applyMacro(cxt, args).eval(cxt);//.eval(cxt);
  }

  @Override
  public default Term applyMacro(Context cxt, Seq args) {
    //return invokeSeq(args.fmap((Invocable1) x -> x.evalMacros(cxt))).evalMacros(cxt);
    return invokeSeq(args).evalMacros(cxt);//.eval(cxt);
  }


  @Override
  public Term invokeSeq(Seq args);

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.macro(this, arg);
  }

}
