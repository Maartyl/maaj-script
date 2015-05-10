/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.util.H;

/**
 * basic functions that operate on evaluated arguments
 * <p>
 * @author maartyl
 */
public final class MacroSeq extends InvSeq implements Macro {

  protected MacroSeq(Seq fn, Context closure) {
    super(fn, closure);
  }

  @Override
  protected Symbol getShowName() {
    return macroseqSym;
  }

  private static final Symbol macroseqSym = H.symbol("#", "macroseq");

  public static MacroSeq of(Seq fn, Context closure) {
    return new MacroSeq(fn, closure);
  }
}
