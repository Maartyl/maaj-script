/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.SeqH;

/**
 * string wrapper
 * <p>
 * @author maartyl
 */
public class Str implements JObj, Collection<Str> {

  private final String value;

  public Str(String value) {
    this.value = value;
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
    //implement Associative and for numbers: return character at that pos
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Int getCount() {
    return Int.of(value.length());
  }

  @Override
  public Seq seq() {
    return SeqH.incremental2lazySeq(i -> H.wrap(value.charAt(i)), i -> i >= value.length());
  }

  @Override
  public Str retM(Term contents) {
    return contents.show(); //why not; works correctly for Chars... 
  }

  @Override
  public Str bindM(Invocable fn2Monad) {
    StringBuilder sb = new StringBuilder();
    foreach((Invocable1) ch -> {
      Str rslt = fn2Monad.invoke(ch).show();
      sb.append(rslt.value);
      return H.NIL;
    });
    String str = sb.toString();
    if (value.equals(str)) return this;
    return Str.of(str);
  }

  @Override
  public Str fmap(Invocable mapper) {
    //TODO: possibly optimize
    StringBuilder sb = new StringBuilder();
    for (char c : value.toCharArray())
      sb.append(H.requireChar(mapper.invoke(H.wrap(c))).asCharacter());
    return Str.of(sb.toString());
  }

  @Override
  public Term reduce(Term acc, Invocable reducer) {
    //TODO: possibly optimize
    for (char c : value.toCharArray()) 
      acc = reducer.invoke(acc, H.wrap(c));
    return acc;
  }

  @Override
  public Str foreach(Invocable mapper) {
    //TODO: possibly optimize
    for (char c : value.toCharArray())
      mapper.invoke(H.wrap(c));
    return this;
  }

  @Override
  public Str conj(Term t) {
    return Str.of(asString() + t.toString());
  }

  @Override
  public void show(Writer w) throws IOException {
    serialize(w);
  }

  @Override
  public void serialize(Writer w) throws IOException {
    w.append("\"");
    escapeAppend(value, w);
    w.append("\"");
  }

  private void escapeAppend(String s, Writer w) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
      case '\n': w.append("\\n");
        break;
      case '\\': w.append("\\\\");
        break;
      case '"': w.append("\\\"");
        break;
      case '\r': w.append("\\\r");
        break;
      case '\b': w.append("\\\b");
        break;
      case '\f': w.append("\\\f");
        break;
      default:
        if (s.charAt(i) < 32) { //unprintable and not above
          w.append("\\x");
          w.append(Integer.toHexString((byte) s.charAt(i)));
        } else {
          w.append(s.charAt(i));
        }
      }

    }
  }

  @Override
  public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.str(this, arg);
  }

  public String asString() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Str other = (Str) obj;
    return Objects.equals(this.value, other.value);
  }

  @Override
  public Object getContent() {
    return value;
  }

  //--- STATIC
  public static final Str EMPTY = new Str("");

  public static Str of(String value) {
    return new Str(value);
  }

}
