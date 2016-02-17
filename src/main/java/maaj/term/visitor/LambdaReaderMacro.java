/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import maaj.term.Seq;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.VecT;
import maaj.util.H;
import maaj.util.Sym;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 */
public class LambdaReaderMacro implements VisitorTerm<Object> {

  //arg-position -> name; -1 means rest arg
  Map<Integer, Symbol> args = new HashMap<>();
  Matcher m = argPtrn.matcher("<empty at start>");

  @Override
  public Term run(Term t) {
    Term body = VisitorTerm.super.run(t); //cannot inline: sidefects
    return H.list(Sym.fnSymCore, argsVec(), body);
  }

  @Override
  public Term seq(Seq t, Object arg) {
    Seq s = (Seq) VisitorTerm.super.seq(t, arg);
    if (s.boundLength(256) > 256) //realize seq for side effects (find all args)
      throw new IllegalArgumentException("Cannot use lambda literal with seqs over 256 elements, consider using fn instead.");
    return s;
  }

  @Override
  public Term symbolSimple(Symbol t, Object arg) {
    String nm = t.getNm();
    if ("%&".equals(nm)) return replacer(-1);
    if ("%".equals(nm)) return replacer(1);

    m.reset(nm);
    if (m.matches())
      return replacer(Integer.parseInt(nm.substring(1)));
    else return VisitorTerm.super.symbolSimple(t, arg);
  }

  private Symbol replacer(int pos) {
    return args.computeIfAbsent(pos, i -> H.uniqueSymbol());
  }

  private Term argsVec() {
    Integer[] a = args.keySet().toArray(new Integer[1]);
    if (a.length == 0 || a[0] == null) return H.tuple(); //no arguments -> []

    Arrays.sort(a);

    boolean rest = a[0] == -1; //contains %&

    if (rest && a.length == 1) return args.get(a[0]); // only %&

    int max = a[a.length - 1];
    VecT v = VecH.emptyTransient();
    for (int i = 1; i <= max; i++) // add each argument name to vector; if not present, use _
      v.doConj(args.getOrDefault(i, Sym.ignoreSym));
    
    if (rest) { //capture rest too; add: & rest_arg
      v.doConj(Sym.ampSym);
      v.doConj(args.get(-1));
    }
    return v.asPersistent();
  }

  private static final Pattern argPtrn = Pattern.compile("\\%[1-9]+[0-9]*");

  public static VisitorTerm<Object> create() {
    return new LambdaReaderMacro();
  }

}
