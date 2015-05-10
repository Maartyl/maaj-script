/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * wrapped longs
 * <p>
 * @author maartyl
 */
public final class Int implements Num {
  private final long value;

  private Int(long value) {
    this.value = value;
  }

  @Override
  public Int inc() {
    return Int.of(value + 1);
  }

  @Override
  public Int inc(Num diff) {
    //todo: 'plus' variant that will add mathematically: Int + Dbl => Dbl
    return Int.of(value + diff.asLong());
  }

  @Override
  public Int neg() {
    return Num.of(-value);
  }

  @Override
  public Int dec() {
    return Num.of(value - 1);
  }

  @Override
  public Int dec(Num diff) {
    return Int.of(value - diff.asLong());
  }

  @Override
  public int abilty() {
    return 20;
  }

  @Override
  public Num plus(Num other) {
    return Num.arithmetic(this, other, x -> of(value + x.asLong()), other::plus);
  }

  @Override
  public Num minus(Num other) {
    return Num.arithmetic(this, other, x -> of(value - x.asLong()), other::minusR);
  }

  @Override
  public Num minusR(Num other) {
    return Num.arithmetic(this, other, x -> of(x.asLong() - value), other::minus);
  }

  @Override
  public Num mul(Num other) {
    return Num.arithmetic(this, other, x -> of(value * x.asLong()), other::mul);
  }

  @Override
  public Num div(Num other) {
    return Num.arithmetic(this, other, x -> of(value / x.asLong()), other::divR);
  }

  @Override
  public Num divR(Num other) {
    return Num.arithmetic(this, other, x -> of(x.asLong() / value), other::div);
  }

  @Override
  public long asLong() {
    return value;
  }

  @Override
  public double asDouble() {
    return value;
  }

  @Override
  public boolean eq(Num other) {
    if (other.getClass() == Int.class) {
      return value == other.asLong();
    }
    return value == other.asDouble();
  }

  @Override
  public boolean lt(Num other) {
    if (other.getClass() == Int.class) {
      return value < other.asLong();
    }
    return value < other.asDouble();
  }

  @Override
  public void show(Writer w) throws IOException {
    w.append(this.toString());
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @Override
  public Object getContent() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public boolean equals(Object obj) {
//    if (value <= 256 && value >= -255)
//      return this == obj;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Int other = (Int) obj;
    return this.value == other.value;
  }




  //---- STATIC
  private static final Int[] cache = new Int[512];

  public static Int of(long val) {
    final int startOffset = 255;
    if (val >= -255 && val <= 256) {
      final int cachePos = (int) (startOffset + val);
      if (cache[cachePos] == null) {
        //no sync needed: worst case: it will be created twice
        cache[cachePos] = new Int(val);
      }
      return cache[cachePos];
    }
    return new Int(val);
  }

  public static Int of(int val) {
    return Int.of((long) val);
  }
}
