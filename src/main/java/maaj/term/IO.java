/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;

/**
 * Represents computation dependent on state of world.
 * Can mutate more than just current namespace in about any way.
 * One has to consider time and state when invoking using 'io! .
 * <p>
 * Transients and 'def are only other stateful constructs,
 * but compared to IO they are very limited (to the collection and Vars in current namespace).
 * There will be a few more stateful constructs, but all very local and safe. (like Atom, ...)
 * <p>
 * @author maartyl
 */
@FunctionalInterface
public interface IO extends Monad<IO>, Ground {

  Term run(Context c);

  @Override
  public default IO retM(Term contents) {
    return c -> contents;
  }

  @Override
  public default IO bindM(Invocable fn2Monad) {
    return c -> H.requireIO(fn2Monad.invoke(run(c))).run(c);
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.io(this, arg);
  }

  @Override
  public default Monad unquoteTraverse(Context c) {
    return Ground.super.unquoteTraverse(c);
  }

  @Override
  public default Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Cannot apply an IO monad.");
  }

  @Override
  public default void show(Writer w) throws IOException {
    w.append("#<IO>");
  }

  public static IO retM1(Term cnt) {
    return c -> cnt;
  }

  public static IO make(IO io) {
    return io;
  }

}
