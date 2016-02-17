/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import maaj.term.Ground;
import maaj.term.Invocable1;
import maaj.term.Monad;
import maaj.term.Term;

/**
 *
 * @author maartyl
 * @param <TA> argument type
 */
public interface VisitorTerm<TA> extends VisitorRecursive<Term, TA> {

  ///starting point
  @Override
  default Term run(Term t, TA arg) {
    return visitTransformed(this, t, arg);
  }

  @Override
  default Term id(Term t, TA arg) {
    return t;
  }

  @Override
  default Term ground(Ground t, TA arg) {
    return id(t, arg);
  }

  @Override
  default Term monad(Monad m, TA arg) {
    return m.fmap((Invocable1) t -> visitTransformed(this, t, arg));
  }

  static <TA> Term visitTransformed(VisitorTerm<TA> v, Term t, TA arg) {
    return t.transform((Invocable1) x -> x.visit(v, arg));
  }
}
