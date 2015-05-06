/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import com.github.krukow.clj_lang.ArityException;
import java.io.IOException;
import maaj.coll.traits.Functor;
import maaj.coll.traits.VecLikeBase;
import maaj.lang.Context;
import maaj.util.H;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 * @param <VB> self
 */
public interface VecBase<VB extends VecBase<VB>> extends CollectionBase<VB>, VecLikeBase<VB> {

  /**
   * Knows how to become itself from transient version.
   * Essentailly private.
   * <p>
   * @param v
   * @return
   */
  VB fromTransient(VecT v);

  VB fromPersistent(Vec v);

  @Override
  @SuppressWarnings("unchecked")
  default public VB fmap(Invocable mapper) {
    int count = getCountAsInteger();
    VecT v = VecH.emptyTransient();
    for (int i = 0; i < count; i++)
      v.doConj(mapper.invoke(nth(i)));
    return fromTransient(v);
  }

  @Override
  default public VB bindM(Invocable fn2Monad) {
    VecT v = VecH.emptyTransient();
    foreach((Invocable1) x -> (Term) ((Functor<?>) fn2Monad.invoke(x)).foreach((Invocable1) v::doConj));
    return fromTransient(v);
  }

  @Override
  default public VB retM(Term contents) {
    return fromPersistent(H.tuple(contents));
  }

  @Override
  default public Term apply(Context cxt, Seq args) {
    if (H.isSingle(args))
      return valAt(args.first().eval(cxt));
    throw new ArityException(args.boundLength(20), "Vector can only be applied to 1 Int argument.");
  }

  @Override
  default public Term evalMacros(Context c) {
    return fmap((Invocable1) x -> x.evalMacros(c));
  }

  @Override
  default public Term eval(Context c) {
    return fmap((Invocable1) x -> x.eval(c));
  }

  @Override
  default public Term reduce(Term acc, Invocable reducer) {
    int count = getCountAsInteger();
    for (int i = 0; i < count; i++)
      acc = reducer.invoke(acc, nth(i));
    return acc;
  }

  @Override
  default public VecBase<VB> foreach(Invocable mapper) {
    int count = getCountAsInteger();
    for (int i = 0; i < count; i++) {
      mapper.invoke(nth(i));
    }
    return this;
  }

  @Override
  default public void serialize(java.io.Writer w) throws IOException {
    int count = getCountAsInteger();
    switch (count) {
    case 0: w.append("[]");
      break;
//    case 1:
//      w.append('[');
//      nth(0).serialize(w);
//      w.append(']');
//      break;
    default:
      w.append('[');
      nth(0).serialize(w);
      for (int i = 1; i < count; i++) {
        w.append(' ');
        nth(i).serialize(w);
      }
      w.append(']');
    }
  }
}
