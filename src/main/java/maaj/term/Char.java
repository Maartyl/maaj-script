/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;

/**
 * wrapped longs
 * <p>
 * @author maartyl
 */
public final class Char implements Num {
  private final char value;

  private Char(char value) {
    this.value = value;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Won't be ever supported."); //TODO: implement
  }

  @Override
  public Char inc() {
    return Char.of((char) (value + 1));
  }

  @Override
  public Char inc(Num diff) {
    //todo: 'add' variant that will add mathematically: Int + Dbl => Dbl
    return Char.of((char) (value + diff.asLong()));
  }

  @Override
  public Char neg() {
    throw new InvalidOperationException("Char cennot be negated.");
  }

  @Override
  public Char dec() {
    return Char.of((char) (value - 1));
  }

  @Override
  public Char dec(Num diff) {
    return Char.of((char) (value - diff.asLong()));
  }

  @Override
  public int abilty() {
    return 10;
  }

  @Override
  public Num add(Num other) {
    return Num.arithmetic(this, other, x -> of(value + x.asLong()), other::add);
  }

  @Override
  public Num sub(Num other) {
    return Num.arithmetic(this, other, x -> of(value - x.asLong()), other::subR);
  }

  @Override
  public Num subR(Num other) {
    return Num.arithmetic(this, other, x -> of(x.asLong() - value), other::sub);
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

  public char asCharacter() {
    return value;
  }

  @Override
  public boolean eq(Num other) {
    if (other.getClass() == Char.class) 
      return value == ((Char) other).value;
    
    return value == other.asDouble();
  }

  @Override
  public boolean lt(Num other) {
    if (other.getClass() == Char.class) {
      return value < ((Char) other).value;
    }
    return value < other.asDouble();
  }

  @Override
  public void show(Writer w) throws IOException {
    w.append(toString());
  }

  @Override
  public String toString() {
    //TODO: when doing chars: also return special as \newline etc.
    return "\\" + value;
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
    if (obj == null) return false;
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if (getClass() != obj.getClass()) return false;
    final Char other = (Char) obj;
    if (this.value != other.value) return false;
    return true;
  }




  //---- STATIC (ASCII + ... 128-255)
  private static final Char[] cache = new Char[256];

  public static Char of(char val) {
    if (val < 256) {
      if (cache[val] == null) {
        //no sync needed: worst case: it will be created twice
        cache[val] = new Char(val);
      }
      return cache[val];
    }
    return new Char(val);
  }

  private static Char of(long val) {
    return of((char) val);
  }

}
