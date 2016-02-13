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
  public int abilty() {
    return 40;
  }

  @Override
  public Num add(Num other) {
    return Num.arithmetic(this, other, x -> of(value + x.asDouble()), other::add);
  }

  @Override
  public Num sub(Num other) {
    return Num.arithmetic(this, other, x -> of(value - x.asDouble()), other::subR);
  }

  @Override
  public Num subR(Num other) {
    return Num.arithmetic(this, other, x -> of(x.asDouble() - value), other::sub);
  }

  @Override
  public Num mul(Num other) {
    return Num.arithmetic(this, other, x -> of(value * x.asDouble()), other::mul);
  }

  @Override
  public Num div(Num other) {
    return Num.arithmetic(this, other, x -> of(value / x.asDouble()), other::divR);
  }

  @Override
  public Num divR(Num other) {
    return Num.arithmetic(this, other, x -> of(x.asDouble() / value), other::div);
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
  public void show(Writer w) throws IOException {
    w.append(this.toString());
  }

  @Override
  public Term visit(Visitor v) {
    return v.dbl(this);
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }

  @Override
  public int hashCode() {
    return Double.hashCode(value);
    //long dblBits = Double.doubleToLongBits(value);
    //return (int) (dblBits ^ (dblBits >>> 32));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if (getClass() != obj.getClass()) return false;
    final Dbl other = (Dbl) obj;
    return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
  }


  @Override
  public Term eval(Context c) {
    return this;
  }
  @Override
  public Term evalMacros(Context c) {
    return this;
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
