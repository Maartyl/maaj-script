/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import maaj.reader.ReaderContext;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.util.H;

/**
 * This loader loads namespaces from files, which names are computed from the namespace name.
 * Namespace name transformation into file:
 * - replace '.' with '/' and add ".maaj"
 * - so: foo.bar -> foo/bar.maaj
 * -- which essentailly means: /foo/bar.maaj
 * - files are searched for on CLASSPATH
 * <p>
 * @author maartyl
 */
public class PathLoader extends Namespace.Loader {

  @Override
  public Namespace loadNamespaceFor(Symbol nsName, maaj.lang.Context cxt) {
    URL file = findFile(nsName);
    Namespace ns = createEmptyWithName(nsName);
    load(file, cxt.withNamespace(ns), nsName);
    return ns;
  }
  /**
   * reads and evaluates all top-level forms in found file (namespace contents definitions)
   * <p>
   */
  private void load(URL file, Context cxt, Symbol nsName) {
    ReaderContext rc = new ReaderContext(nsName, file.getPath());
    try (Reader r = new InputStreamReader(file.openStream(), "UTF8")) {
      for (Term t : H.read(r, rc)) 
        t.eval(cxt);
    } catch (Exception e) {
      throw H.sneakyThrow(e);
    }
  }

  private URL findFile(Symbol ns) {
    if (ns.isQualified())
      throw new IllegalArgumentException("Don't know how to load qualified namespace.");
    String name = ns.getNm();
    String path = name.replace('.', '/') + ".maaj";

    URL r = res(path);
    if (r != null)
      return r;

    throw H.sneakyThrow(new FileNotFoundException("Cannot find file for: " + name + " (" + path + ")"));
  }

  private URL res(String path) {
    return PathLoader.class.getClassLoader().getResource(path);
  }

}
