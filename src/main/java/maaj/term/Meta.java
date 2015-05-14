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
  private final Map meta;

  private Meta(Term val, Map meta) {
    this.val = val.unwrap();
    this.meta = MapH.update(val.getMeta(), meta);
  }

  @Override
  public Term unwrap() {
    return val;
  }

  @Override
  public Term addMeta(Map meta) {
    return val.addMeta(MapH.update(this.meta, meta));
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
    return t.addMeta(meta);
  }

  @Override
  public Term evalMacros(Context c) {
    return Mimic.super.evalMacros(c).addMeta(meta);
  }

  @Override
  public Term eval(Context c) {
    Term t = Mimic.super.eval(c);
    return t.addMeta(meta);
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

  public static Meta of(Term val) {
    //they generally shouldn't have any meta
    //this /ctor probably shouldn't be ever called...
    return of(val.unwrap(), val.getMeta());
  }

  public static Meta of(Term val, Map meta) {
    //called from default: withMeta
    return new Meta(val == null ? H.NIL : val, meta);
  }



}
