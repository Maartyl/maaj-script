/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

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

  //--- static - delegates to instances : abstraction level
  static Int of(long val) {
    return Int.of(val);
  }

  static Dbl of(double val) {
    return Dbl.of(val);
  }

}
