/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;

/**
 *
 * @author maartyl
 */
public final class Dbl implements Num {

  private final double value;

  private Dbl(double value) {
    this.value = value;
  }

  @Override
  public Dbl inc() {
    return Dbl.of(value + 1);
  }
  @Override
  public Dbl inc(Num diff) {
    return Num.of(value + diff.asDouble());
  }
  @Override
  public Dbl neg() {
    return Num.of(-value);
  }
  @Override
  public Dbl dec() {
    return Dbl.of(value - 1);
  }
  @Override
  public Num dec(Num diff) {
    return Num.super.dec(diff);
  }


  @Override
  public long asLong() {
    return (long) value;
  }
  @Override
  public double asDouble() {
    return value;
  }

  @Override
  public boolean eq(Num other) {
    return value == other.asDouble();
  }
  @Override
  public boolean lt(Num other) {
    return value < other.asDouble();
  }

  @Override
  public Term eval(Context c) {
    return this;
  }
  @Override
  public Term evalMacros(Context c) {
    return this;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  //-- STATIC
  private static final Dbl[] cache = new Dbl[12];
  private static Dbl fromCache(int pos, double val) {
    if (cache[pos] == null)
      //no sync needed: worst case: created twice
      cache[pos] = new Dbl(val);
    return cache[pos];
  }
  public static Dbl of(double val) {
    //cache: -1, 0, 1, ... 10
    if (val >= -1 && val <= 10) {
      if (val == -1.0) return fromCache(11, val);
//      for (int i = 0; i <= 10; ++i) {
//        if (val == i) return fromCache(i, val);
//      }
      if ((int) val == val) return fromCache((int) val, val);
    }
    return new Dbl(val);
  }

  public static Dbl of(float val) {
    return Dbl.of((double) val);
  }

}
