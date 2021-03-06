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
 * Mimics underlying term, possibly adding some functionality.
 * All calls are by default delegated on unwrap().
 * Implementing classes ~must overwrite .toString(), .hash, equals...
 * <p>
 * @author maartyl
 */
public interface Mimic extends Term {

  @Override
  public Term unwrap();


  @Override
  public default Term eval(Context c) {
    return unwrap().eval(c);
  }

  @Override
  public default Term evalMacros(Context c) {
    return unwrap().evalMacros(c);
  }

  @Override
  public default Term apply(Context cxt, Seq args) {
    return unwrap().apply(cxt, args);
  }

  @Override
  public default Monad unquoteTraverse(Context c) {
    return unwrap().unquoteTraverse(c);
  }

  @Override
  public default void show(Writer w) throws IOException {
    unwrap().show(w);
  }

  @Override
  public default void serialize(Writer w) throws IOException {
    unwrap().serialize(w);
  }

  @Override
  public default String print() {
    return unwrap().print();
  }

  @Override
  public default Str show() {
    return unwrap().show();
  }

  @Override
  public default Term transform(Invocable transformer) {
    return unwrap().transform(transformer);
  }

  @Override
  public default boolean isRecur() {
    return unwrap().isRecur();
  }

  @Override
  public default Object getContent() {
    return unwrap().getContent();
  }

  @Override
  public default Class getType() {
    return unwrap().getType();
  }

  @Override
  public default Term applyMacro(Context cxt, Seq args) {
    return unwrap().applyMacro(cxt, args);
  }

  @Override
  public default Map getMeta() {
    return unwrap().getMeta();
  }

  @Override
  public default boolean isNil() {
    return unwrap().isNil();
  }

  @Override
  public default Term addMeta(maaj.term.Map meta) {
    return unwrap().addMeta(meta);
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    throw new UnsupportedOperationException(".visit must be called from within .transform ; (use .run)"); //TODO: implement
  }

  @Override
  public String toString();

  @Override
  public boolean equals(Object obj);

  @Override
  public int hashCode();

}
