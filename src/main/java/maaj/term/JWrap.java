/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.term.visitor.Visitor;

/**
 *
 * @author maartyl
 */
public class JWrap implements JObj, Ground {

  Object obj;

  protected JWrap(Object obj) {
    this.obj = obj;
  }

  @Override
  public Object getContent() {
    return obj;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Not supported yet."); // wrong?
  }

  @Override
  public Term visit(Visitor v) {
    return v.jwrap(this);
  }

  public static JWrap of(Object obj) {
    if (obj == null)
      throw new IllegalArgumentException("wrapped object cannot be null");
    return new JWrap(obj);
  }

}
