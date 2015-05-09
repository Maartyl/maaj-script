/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;
import maaj.util.MapH;

/**
 *
 * @author maartyl
 */
public class Var implements Mimic {
  private Map meta;
  private Term value;

  @Override
  public Term unwrap() {
    if (value == null)
      throw new InvalidOperationException("unwrap unbound var");
    return value;
  }

  @Override
  public Term addMeta(Map meta) {
    this.meta = MapH.update(this.meta, meta);
    return this;
  }

  @Override
  public Map getMeta() {
    return meta;
  }

  @Override
  public Term transform(Invocable transformer) {
    return Mimic.super.transform(transformer);
  }

  @Override
  public void show(Writer w) throws IOException {
    Mimic.super.show(w);
  }

  @Override
  public Term evalMacros(Context c) {
    return Mimic.super.evalMacros(c);
  }


}
