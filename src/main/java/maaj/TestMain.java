/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj;

import java.io.StringReader;
import maaj.coll.traits.Persistent;
import maaj.coll.traits.TraPer;
import maaj.coll.traits.Transient;
import maaj.lang.Context;
import maaj.reader.MaajReader;
import maaj.util.H;
import maaj.util.SeqH;
import maaj.term.Int;
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
    System.out.println("------");

    StringReader sr = new StringReader("1[  2 4  ]3  \"hel\\u1234l\\to\" \n"
                                       + " `(hello? :from ('this ({}recursive[])test!))  {1 2, 3 4}\n"
                                       + "--5  (a/a/a/a/a/?)");
    for (Term t : MaajReader.read(sr, null)) {
      System.out.println("--");
      System.out.println(t.print());
      System.out.println(t.evalMacros(new Context()).print());
    }
  }


}
