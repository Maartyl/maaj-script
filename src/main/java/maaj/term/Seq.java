
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.coll.Sexp;
import maaj.coll.traits.SeqLike;
import maaj.lang.Context;
import maaj.exceptions.InvalidOperationException;
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.SeqH;

/**
 * Represents (potentially lazy) singly linked list - base for S-expressions
 * <p>
 * Default ~Monadic and seq operations implementations are lazy
 * - Sexp overrides them to be eager if list is short
 *
 * @author maartyl
 */
public interface Seq extends Monad<Seq>, SeqLike<Seq> {

  @Override //explicit because: diamon inheritance (same name on purpose)
  default boolean isNil() {
    return false;
  }

  @Override
  public default Seq conj(Term t) {
    return H.cons(t, this);
  }

  @Override
  public default Seq retM(Term contents) {
    return Sexp.retM1(contents);
  }

  @Override
  public default Seq bindM(Invocable fn2Monad) {
    return SeqH.concatLazy(this.fmap(fn2Monad));
  }

  @Override
  public default Seq fmap(Invocable mapper) {
    return SeqH.mapLazy(this, mapper);
  }

  @Override
  public default Term eval(Context c) {
    //Core sexp application:
    //rest will be evaluated in apply (or not) based on 'function type' (Fn, macro...)
    return first().eval(c).apply(c, rest());
  }

  @Override
  public default Term evalMacros(Context c) {
    return first().evalMacros(c).applyMacro(c, rest());
  }

  @Override
  public default Term apply(Context cxt, Seq args) {
    throw new InvalidOperationException("Seq cannot be used as a function.");
  }

  @Override
  public default Seq seq() {
    return this;
  }

  /**
   * eagerly evaluated
   */
  @Override
  default Seq foreach(Invocable mapper) {
    for (Seq cur = this; !cur.isNil(); cur = cur.rest())
      mapper.invoke(cur.first());
    return this;
  }

  //--- static
  public static Seq retM1(Term content) {
    return Sexp.retM1(content);
  }

  @Override
  public default void show(Writer w) throws IOException {
    if (isNil())
      w.append("()");
    else {
      w.append('(');
      first().show(w);
      for (Term t : rest()) {
        w.append(' ');
        t.show(w);
      }
      w.append(')');
    }
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.seq(this, arg);
  }

}
