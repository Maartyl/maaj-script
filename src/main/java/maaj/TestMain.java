/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj;

import com.github.krukow.clj_lang.IMapEntry;
import com.github.krukow.clj_lang.IPersistentCollection;
import com.github.krukow.clj_lang.IPersistentMap;
import com.github.krukow.clj_lang.IPersistentStack;
import com.github.krukow.clj_lang.IPersistentVector;
import com.github.krukow.clj_lang.ISeq;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import maaj.coll.traits.Persistent;
import maaj.coll.traits.TraPer;
import maaj.coll.traits.Transient;
import maaj.lang.Context;
import maaj.util.H;
import maaj.util.SeqH;
import maaj.term.Int;
import maaj.term.Str;
import maaj.term.Term;
import maaj.term.*;
import maaj.util.Generators;

/**
 *
 * @author maartyl
 */
public class TestMain {

  private static interface Set extends TraPer<PSet, TSet> {

  }

  private static interface PSet extends Set, Persistent<PSet, TSet> {

  }

  private static interface TSet extends Set, Transient<PSet, TSet> {

  }

  public static void main(String[] args) {
    Int a = Int.of(3);
    System.out.println(a.neg());

    Vec v = H.tuple(a);

    SeqH.mapLazy(H.list(Int.of(1), Int.of(2), Int.of(3), Int.of(4)), (Invocable1) x -> {
      return ((Num) x).inc();
    }).foreach((Invocable1) x -> {
      System.out.println(x);
      return H.NIL;
    });

    try {
      Generators.range().foreach((Invocable1) x -> {
        Int i = H.requireInt(x);

        if (i.gt(Int.of(100)))
          throw new RuntimeException();

        System.out.print(x);
        System.out.print(' ');
        return x;
      });
    } catch (RuntimeException e) {
    }
    System.out.println();
  }

  static void test(IPersistentMap<String, Term> m) {
   

  }

  static void test(IPersistentVector<Term> v) {

  }

  static class TestVec implements IPersistentVector<Term> {

    @Override
    public int length() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentVector<Term> assocN(int i, Term val) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentVector<Term> cons(Term o) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Term peek() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentStack<Term> pop() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public int count() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentCollection<Term> empty() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public boolean equiv(Object o) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public ISeq<Term> seq() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public ISeq<Term> rseq() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Term nth(int i) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Term nth(int i, Term notFound) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }
  }

  static class TestMap implements IPersistentMap<Term, Term> {

    @Override
    public IPersistentMap<Term, Term> assoc(Term key, Term val) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentMap<Term, Term> assocEx(Term key, Term val) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentMap<Term, Term> without(Term key) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Iterator<Map.Entry<Term, Term>> iteratorFrom(Term key) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Iterator<Map.Entry<Term, Term>> reverseIterator() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Iterator<Map.Entry<Term, Term>> iterator() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public boolean containsKey(Term key) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IMapEntry<Term, Term> entryAt(Term key) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public int count() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentCollection<IMapEntry<Term, Term>> cons(IMapEntry<Term, Term> o) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public IPersistentCollection<IMapEntry<Term, Term>> empty() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public boolean equiv(Object o) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public ISeq<IMapEntry<Term, Term>> seq() {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Term valAt(Term key) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

    @Override
    public Term valAt(Term key, Term notFound) {
      throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
    }

  }

}
