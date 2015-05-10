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
public final class FnSeq extends InvSeq implements Fn {

  protected FnSeq(Seq fn, Context closure) {
    super(fn, closure);
  }

  @Override
  public void show(Writer w) throws IOException {
    H.cons(fnseqSym, fn).show(w);
  }

  private static final Symbol fnseqSym = H.symbol("#", "fnseq");

  public static FnSeq of(Seq fn, Context closure) {
    return new FnSeq(fn, closure);
  }


}
