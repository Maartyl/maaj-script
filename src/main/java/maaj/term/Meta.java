/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
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
    this.val = val; //TODO: decide : unwrap?
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
  public Str show() {
    return Mimic.super.show();
  }

  @Override
  public void serialize(Writer w) throws IOException {
    if (meta.isEmpty()) {
      w.append('^');
      meta.serialize(w);
    }
    val.serialize(w);
  }


  @Override
  public void show(Writer w) throws IOException {
    if (meta.isEmpty()) {
      w.append('^');
      meta.show(w);
    }
    val.show(w);
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
