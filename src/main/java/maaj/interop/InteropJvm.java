/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import maaj.term.Invocable1;
import maaj.term.Seq;
import maaj.term.Term;
import maaj.util.H;
import maaj.util.SeqH;
import java.lang.reflect.Modifier;

/**
 *
 * @author maartyl
 */
public class InteropJvm implements Interop {

  private final Converter cvrt; //converter to use

  public InteropJvm(Converter cvrt) {
    this.cvrt = cvrt;
  }

  //thisPtr==null <=> static
  @Override
  public Term call(Class callOn, Object thisPtr, String methodName, Seq args) {
    //callOn cannot be determined from thisPtr, because it can be static call and thisPtr null
    if (args.boundLength(255) > 255)
      throw new IllegalArgumentException("too many arguments to JVM method: " + methodName + " (on " + callOn.getName() + ")");

    List<Invoker> matches = filterMethods(callOn.getMethods(), methodName,
                                          args.boundLength(256), thisPtr == null, typesOfElems(args));

    if (matches.size() == 1)
      return callAndWrap(matches.get(0), thisPtr, args);

    if (matches.size() > 1) {
      matches.sort((l, r) -> l.cost() - r.cost());

      if (matches.get(0).cost() < matches.get(1).cost())
        return callAndWrap(matches.get(0), thisPtr, args);

      StringBuilder b = new StringBuilder();
      int cost0 = matches.get(0).cost();
      for (int i = 0; matches.get(i).cost() <= cost0; ++i) //all with same, lowest cost
        b.append(matches.get(i).getMethodString()).append('\n');

      //ambiguous call
      //what ex?
      throw new RuntimeException("ambiguous call to: "
                                 + methodName + " (on " + callOn.getName() + ")"
                                 + "(args: " + SeqH.take(20, args).print() + ")"
                                 + "(matches: " + b.toString() + ")");
    }

    throw H.sneakyThrow(new NoSuchMethodException("No matching overload found for: "
                                                  + methodName + " (on " + callOn.getName() + ")"
                                                  + "(args: " + SeqH.take(20, args).print() + ")"));
  }

  private Term callAndWrap(Invoker v, Object thisPtr, Seq args) {
    try {
      return H.wrap(v.invoke(thisPtr, contentsOfElems(args)));
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw H.sneakyThrow(e);
    }
  }

  private List<Invoker> filterMethods(Method[] allMethods, String methodName, int argCount, boolean isStatic, Class<?>[] argTypes) {
    List<Invoker> matches = new ArrayList<>(2);
    for (Method m : allMethods) {
      if (m.getName().equals(methodName) && m.getParameterCount() == argCount
          && ((Modifier.STATIC & m.getModifiers()) > 0 == isStatic)) {
        Invoker v = tryGetInvoker(m, argTypes);
        if (v != null)
          matches.add(v);
      }
    }
    return matches;
  }

  //null if impossible to call (find conversion)
  private Invoker tryGetInvoker(Method m, Class[] argTypes) {
    final Class[] slots = m.getParameterTypes();
    assert argTypes.length == slots.length : "diff len of arg list (" + argTypes.length + ", " + slots.length + ")";
    final Conversion[] cs = new Conversion[slots.length];

    for (int i = 0; i < slots.length; ++i) //get conversions for all types
      if ((cs[i] = cvrt.lookup(argTypes[i], slots[i])) == null) return null; //cannot convert

    int costSumMut = 0;
    for (Conversion c : cs) costSumMut += c.cost();
    final int costSum = costSumMut;

    return new Invoker() {
      @Override
      public Object invoke(Object thisPtr, Object[] args) throws IllegalAccessException, InvocationTargetException {
        Object[] params = new Object[args.length];
        for (int i = 0; i < args.length; ++i)
          params[i] = cs[i].convert(args[i]);

        return m.invoke(thisPtr, params);
      }
      @Override
      public int cost() {
        return costSum;
      }

      @Override
      public String getMethodString() {
        return m.toString();
      }
    };
  }

  private static Class[] typesOfElems(Seq seq) {
    Class[] clss = new Class[seq.count().asInteger()];
    H.RangeSeeder rs = new H.RangeSeeder(); //lambda cannot bind mutable reference
    seq.foreach((Invocable1) x -> {
      clss[rs.next()] = x.getType();
      return H.NIL;
    });
    return clss;
  }

  private static Object[] contentsOfElems(Seq seq) {
    Object[] objs = new Object[seq.count().asInteger()];
    H.RangeSeeder rs = new H.RangeSeeder(); //lambda cannot bind mutable reference
    seq.foreach((Invocable1) x -> {
      objs[rs.next()] = x.getContent();
      return H.NIL;
    });
    return objs;
  }
}
