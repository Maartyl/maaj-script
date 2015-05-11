/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.coll.traits.RefSet;
import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;
import maaj.util.H;
import maaj.util.MapH;
import maaj.util.Sym;

/**
 *
 * @author maartyl
 */
public final class Var implements Mimic, RefSet<Var> {
  private volatile Map meta;
  private volatile Term value;

  private Var(Map meta, Term value) {
    this.meta = meta;
    this.value = value;
  }

  private Var(Term value) {
    this(MapH.emptyPersistent(), value);
  }

  @Override
  public Term unwrap() {
    if (value == null)
      throw new InvalidOperationException("unwrap unbound var");
    return value;
  }

  @Override
  public synchronized Var addMeta(Map meta) {
    this.meta = MapH.update(this.meta, meta);
    return this;
  }

  @Override
  public Map getMeta() {
    return meta;
  }

  @Override
  public Term transform(Invocable transformer) {
    //Shouldn't change this!
    //options: return new var; return result only...
    //... returning new var is fairly nonsensical...
    //meta belongs to var, not the underlying term
    // // btw. terms in vars loose their metadata
    return unwrap().transform(transformer); // in the end, the most logical is this
  }

  @Override
  public void show(Writer w) throws IOException {
    if (meta.containsKey(Sym.nameSym))
      H.list(Sym.varSym, meta.valAt(Sym.nameSym), unwrap()).show(w);
    else Mimic.super.show(w);
  }

  @Override
  public Term evalMacros(Context c) {
    //needs this : Mimic could do something else...
    return this;
  }

  @Override
  public Term eval(Context c) {
    return unwrap().eval(c);
  }

  @Override
  public Collection unquoteTraverse(Context c) {
    return H.tuple(this);
  }

  @Override
  public Var doSet(Term t) {
    value = t;
    return this;
  }

  @Override
  public Term deref() {
    return unwrap();
  }

  @Override
  public synchronized Term update(Invocable setter) {
    Term s = deref().transform(setter);
    doSet(s);
    return s;
  }

  public static Var empty() {
    return new Var(null);
  }

  public static Var of(Term val) {
    return of(val.unwrap(), val.getMeta());
  }

  public static Var of(Term val, Map meta) {
    return new Var(meta, val);
  }

}
