/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;
import maaj.util.H;
import maaj.util.SeqH;

/**
 *
 * @author maartyl
 */
public interface Num extends Ground {

  Num neg();

  Num inc();

  Num inc(Num diff);

  Num dec();

  default Num dec(Num diff) {
    return inc(diff.neg());
  }

  /**
   * the most 'able' part does the arithmetics
   * - Dbl is more able then Int
   * Char : 10
   * Int : 20
   * Dbl : 40
   * <p>
   * - in case the other is more able, -R variant is used (reversed order)
   * @return
   */
  int abilty();

  Num plus(Num other);

  Num minus(Num other);

  Num minusR(Num other);

  Num mul(Num other);

  Num div(Num other);

  Num divR(Num other);

//  Num exp(Num other);
//
//  Num expR(Num other);

  /**
   * returns smaller of the 2 numbers
   * @param other
   * @return
   */
  public default Num min(Num other) {
    return lt(other) ? this : other;
  }

  public default Num max(Num other) {
    return lt(other) ? other : this;
  }


  long asLong();

  double asDouble();

  default int asInteger() {
    return (int) asLong();
  }

  boolean eq(Num other);

  boolean lt(Num other);

  default boolean lteq(Num other) {
    return lt(other) || eq(other);
  }

  default boolean gt(Num other) {
    return !lteq(other);
  }

  default boolean gteq(Num other) {
    return !lt(other);
  }

  static Num arithmetic(Num l, Num r, NumOp onL, NumOp onR) {
    if (l.abilty() >= r.abilty()) {
      return onL.op(r);
    } else {
      return onR.op(l);
    }
  }

  static interface NumOp {
    Num op(Num other);
  }

  static interface Num2Op extends Fn {

    Num op(Num t, Num a);

    @Override
    public default Term invokeSeq(Seq args) {
      if (args.boundLength(2) != 2)
        throw new IllegalArgumentException("core arithmetic operator: requires 2 args");
      return op(H.requireNum(args.first()), H.requireNum(args.rest().first()));
    }

  }

  //--- static - delegates to instances : abstraction level
  static Int of(long val) {
    return Int.of(val);
  }

  static Dbl of(double val) {
    return Dbl.of(val);
  }

  @Override
  public default Term apply(Context cxt, Seq args) {
    args = SeqH.mapEval(args, cxt);
    if (args.first().getContent() instanceof Num) {
      //infinite recursion
      throw new InvalidOperationException("applying Num to a Num");
    }
    return args.first().apply(cxt, H.cons(this, args.rest()));
  }

}
