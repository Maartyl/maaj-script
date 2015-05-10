/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import java.util.function.BiFunction;
import maaj.term.*;

/**
 * Function helpers
 * <p>
 * @author maartyl
 */
public class FnH {
  public static Invocable compose(Term fn1, Term fn2) {
//    if (fn1 instanceof Invocable)
//      if (fn2 instanceof Invocable)
    //return x -> ((Invocable)fn2).
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * assumes first arg to by an invocable
   * will apply first argument with second
   * <p>
   * @return
   */
  public static Invocable2 invoke1() {
    return (fn, arg) -> ((Invocable) fn).invoke(arg);
  }

  public static Term invoke1l(Term fn, Term arg) {
    return ((Invocable) fn).invoke(arg);
  }

  @SuppressWarnings("unchecked")
  public static <T, U, V> Invocable2 liftTypeUncheched2(BiFunction<T, U, V> fn) {
    return (t, u) -> (Term) fn.apply((T) t, (U) u);
  }
}
