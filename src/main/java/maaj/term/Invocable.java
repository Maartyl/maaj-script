/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import com.github.krukow.clj_lang.ArityException;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;

/**
 * Base interface for terms that can be invoked: functions, macros, special forms...
 * Specialized version are used throughout code as functional interfaces...
 * <p>
 * @author maartyl
 */
public interface Invocable extends Ground, Runnable, Callable<Term> {

  default Term invokeSeq(Seq args) {
    //TODO: periodically un-default this; check if everything ok; re-default
    //this has default implementation because of lambdas:
    //like this I have to specify (Invocable1)
    //without this being default: it would use this which yield wrong result
    throw new UnsupportedOperationException("invokeSeq has to be overriden");
  }

  @Override
  public default Term apply(Context cxt, Seq args) {
    return invokeSeq(args);
  }

  default Term invoke() {
    return invokeSeq(H.list());
  }
  default Term invoke(Term arg1) {
    return invokeSeq(H.list(arg1));
  }

  default Term invoke(Term arg1, Term arg2) {
    return invokeSeq(H.list(arg1, arg2));
  }

  default Term invoke(Term arg1, Term arg2, Term arg3) {
    return invokeSeq(H.list(arg1, arg2, arg3));
  }

  default Term invoke(Term arg1, Term arg2, Term arg3, Term arg4) {
    return invokeSeq(H.list(arg1, arg2, arg3, arg4));
  }

  default Term invoke(Term arg1, Term arg2, Term arg3, Term arg4, Term arg5) {
    return invokeSeq(H.list(arg1, arg2, arg3, arg4, arg5));
  }

  @Override
  public default void run() {
    invoke();
  }

  @Override
  public default Term call() throws Exception {
    return invoke();
  }


  default Term throwArity(int n) {
    String name = getClass().getSimpleName();
    int suffix = name.lastIndexOf("__");
    throw new ArityException(n, (suffix == -1 ? name : name.substring(0, suffix)).replace('_', '-'));
  }

  default Term throwArityFor(Seq args) {
    int argLen = args.boundLength(30);
    return throwArity(argLen == Integer.MAX_VALUE ? -1 : argLen);
  }

  @Override
  public default void show(Writer w) throws IOException {
    w.append("#<$>");
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.invocable(this, arg);
  }


}
