/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.Lookup;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.Generators;
import maaj.util.H;
import maaj.util.SeqH;

/**
 * interface representing all numbers in MaajScript. ~numeric
 * <p>
 * @author maartyl
 */
public interface Num extends Ground {

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.num(this, arg);
  }

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

  Num add(Num other);

  Num sub(Num other);

  Num subR(Num other);

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

  static interface NumOp extends Fn {
    Num op(Num other);

    @Override
    public default Term invokeSeq(Seq args) {
      if (!SeqH.isSingle(args))
        throw new IllegalArgumentException("core arithmetic operator: requires 1 arg; got: " + args.boundLength(30));
      return op(H.requireNum(args.first()));
    }

  }

  static interface Num2Op extends Fn {
    Num op(Num l, Num r);

    @Override
    public default Term invokeSeq(Seq args) {
      if (args.boundLength(2) != 2)
        throw new IllegalArgumentException("core arithmetic operator: requires 2 args; got: " + args.boundLength(30));
      return op(H.requireNum(args.first()), H.requireNum(args.rest().first()));
    }

  }

  static interface NumPred extends Fn {

    boolean op(Num l, Num r);

    @Override
    public default Term invokeSeq(Seq args) {
      if (args.boundLength(2) != 2)
        throw new IllegalArgumentException("core arithmetic predicate: requires 2 args; got: " + args.boundLength(30));
      return H.wrap(op(H.requireNum(args.first()), H.requireNum(args.rest().first())));
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
    Term firstOrNil = args.firstOrNil().unwrap();
    //apply would reevaluate arguments
    if (firstOrNil instanceof Lookup) {
      switch (args.boundLength(2)) {
      case 1: return ((Lookup) firstOrNil).valAt(this);
      case 2: return ((Lookup) firstOrNil).valAt(this, args.rest().first());
      default:
      }
    }
    if (firstOrNil instanceof Invocable) {
      return ((Invocable) firstOrNil).invokeSeq(H.cons(this, args.restOrNil()));
    }
    switch (args.boundLength(3)) {
    case 0: return Generators.range(asInteger());
    }
    Seq types = SeqH.mapLazy(args, (Invocable1) x -> H.symbol(x.getType().getName()));
    throw new IllegalArgumentException("Don't know how to apply Num to: " + types.print());
  }

}
