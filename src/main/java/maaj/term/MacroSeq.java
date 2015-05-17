/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.util.Sym;

/**
 * Macros are functions that operate on unevaluated arguments and
 * return expression trees to be evaluated instead of itself.
 * - in this case, implemented through InvSeq
 * <p>
 * @author maartyl
 */
public final class MacroSeq extends InvSeq implements Macro {

  protected MacroSeq(Seq fn, Context closure) {
    super(fn, closure);
  }

  @Override
  protected Symbol getShowName() {
    return Sym.macroseqSymC;
  }

  public static MacroSeq of(Seq fn, Context closure) {
    return new MacroSeq(fn, closure);
  }
}
