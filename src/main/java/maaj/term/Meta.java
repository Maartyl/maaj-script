/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.util.H;
import maaj.util.MapH;

/**
 *
 * @author maartyl
 */
public class Meta implements Mimic {

  private final Term val;
  private Map meta;

  private Meta(Term val, Map meta) {
    this.val = val.unwrap();
    this.meta = MapH.update(val.getMeta(), meta);
  }

  @Override
  public Term unwrap() {
    return val;
  }

  @Override
  public synchronized Term addMeta(Map meta) {
    this.meta = MapH.update(this.meta, meta);
    return this;
  }

  @Override
  public Map getMeta() {
    return meta;
  }

  @Override
  public Term transform(Invocable transformer) {
    Term u = unwrap();
    Term t = u.transform(transformer);
    if (u == t)
      return this;
    return of(t, meta);
  }

  @Override
  public Term evalMacros(Context c) {
    return of(Mimic.super.evalMacros(c), meta);
  }

  @Override
  public Term eval(Context c) {
    Term t = Mimic.super.eval(c);
    return of(t, meta);
  }


  public static Meta of(Term val) {
    //they generally shouldn't have any meta
    //this /ctor probably shouldn't be ever called...
    return of(val.unwrap(), val.getMeta());
  }

  public static Meta of(Term val, Map meta) {
    //called from default: withMeta
    return new Meta(val == null ? H.NIL : val, meta);
  }

  @Override
  public int hashCode() {
    return unwrap().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return unwrap().equals(obj);
  }

  @Override
  public String toString() {
    return unwrap().toString();
  }



}
