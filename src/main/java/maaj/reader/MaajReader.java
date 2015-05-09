/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.reader;

import java.io.Reader;
import maaj.exceptions.ReaderException;
import maaj.term.Invocable0;
import maaj.term.Map;
import maaj.term.MapT;
import maaj.term.Num;
import maaj.term.Seq;
import maaj.term.Str;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Vec;
import maaj.term.VecT;
import maaj.util.H;
import maaj.util.MapH;
import maaj.util.SeqH;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 */
public class MaajReader {

  private final PosReader reader;
  private final ReaderContext context;

  private MaajReader(PosReader r, ReaderContext context) {
    this.reader = r;
    this.context = context;
  }

  public Seq readAll() {
    if (nextSkipWhitespace() < 0)
      return H.END;
    return H.lazy(read0Cur(), this::readAll);
  }

  private int next() {
    return reader.readNextChar();
  }

  private int cur() {
    return reader.getCurrentChar();
  }

  private void unread() {
    reader.unread();
  }

  private int nextSkipWhitespace() {
    while (isWhitespace(next())) {
    }
    return cur();
  }

  private <T> T fail(String message) {
    throw new ReaderException(context, reader.getRow(), reader.getColumn(), message);
  }

  private Term read0(int c) {
    switch (c) {
    case '(': return readList();
    case '[': return readVec();
    case '{': return readMap();
    case '"': return readStr();
    case '#': return readHash();
    case ';': return readComment();
    case '/': return readSlash();
    case '^': return readMeta();
    case '~': return readUnquote();
    case '\\': return readEscape();
    case '`': return H.list(quoteQualified, read0SkipWhitespace());
    case '\'': return H.list(quote, read0SkipWhitespace());
    case '@': return H.list(deref, read0SkipWhitespace());
    case ')': return fail("unmatched: )");
    case ']': return fail("unmatched: ]");
    case '}': return fail("unmatched: }");
    }
    if (c < 0) return fail("unexpected EOF");
    if (isNumericStart(c))
      return readNum();
    if (isSymbolicStart(c))
      return readSymbol();


    return fail("read0: " + (char) c + " /:" + c);
  }

  private Term read0SkipWhitespace() {
    return read0(nextSkipWhitespace());
  }

  private Term read0Cur() {
    return read0(cur());
  }

  private Seq readList() {
    switch (nextSkipWhitespace()) {
    case ')': return H.END;
    default:
      return SeqH.sexp(read0Cur(), readList());
    }
  }

  private Vec readVec() {
    VecT v = VecH.emptyTransient();
    while (nextSkipWhitespace() != ']')
      v.doConj(read0Cur());
    return VecH.preferTuple(v.asPersistent());
  }

  private Map readMap() {
    MapT m = MapH.emptyTransient();
    while (nextSkipWhitespace() != '}') {
      Term key = read0Cur();
      if (nextSkipWhitespace() == '}')
        return fail("map: Requires even number of terms.");
      Term val = read0Cur();
      m.doAssoc(key, val);
    }
    return m.asPersistent();
  }

  private Term readHash() {

    return fail("readHash: " + (char) cur() + " /:" + cur());
  }

  private Term readSlash() {
    //symbols cannot start with /, unless / - DECIDE what to do with this
    return fail("readSlash: " + (char) cur() + " /:" + cur());
  }

  private Str readStr() {
    StringBuilder sb = new StringBuilder();
    while (next() != '"') {
      if (cur() < 0)
        return fail("EOF while reading string");
      if (cur() == '\\')
        sb.append(readStrEscape());
      else
        sb.append((char) cur());
    }
    return Str.of(sb.toString());
  }

  private char readStrEscape() {
    switch (next()) {
    case 'n': return '\n';
    case 't': return '\t';
    case '\\': return '\\';
    case '"': return '"';
    case 'r': return '\r';
    case 'b': return '\b';
    case 'f': return '\f';
    case '\'': return '\'';
    case 'u':
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 4; ++i)
        sb.append((char) next());
      return (char) Integer.parseInt(sb.toString(), 16);
    }
    return fail("unrecognized escape sequence: \\" + (char) cur());
  }

  private Num readNum() {
    return fail("readStr: " + (char) cur() + " /:" + cur());
  }

  private Term readMeta() {
    return fail("readMeta : meta not implemented yet");
  }

  private Term readSymbol() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private Term readUnquote() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private Term readComment() {
    while (next() != '\n') {
    }
    return read0SkipWhitespace();
  }

  private Term readEscape() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private static final Symbol deref = H.symbol("maaj.core", "deref");
  private static final Symbol quote = H.symbol("maaj.core", "quote");
  private static final Symbol quoteQualified = H.symbol("maaj.core", "quote-qualified");
  private static final Symbol unquote = H.symbol("maaj.core", "unquote");
  private static final Symbol unquoteSplicing = H.symbol("maaj.core", "unquote-splicing");

  private static boolean isWhitespace(int c) {
    return Character.isWhitespace(c) || c == ',' || c < 32 || Character.isIdentifierIgnorable(c);
  }

  private static boolean isNumericStart(int c) {
    return Character.isDigit(c);
  }

  private static boolean isNumeric(int c) {
    return isNumericStart(c) || c == '.';
  }

  private static boolean isSymbolicStart(int c) {
    return Character.isAlphabetic(c) 
           || c == '!' || c == '$' || c == '%' || c == '&' || c == '*'
           || c == '*' || c == '-' || c == '+' || c == '?' || c == '.'
           || c == '<' || c == '>' || c == ':' || c == '_' || c == '='
           || c == '\\' || c == '|'
           || Character.isIdeographic(c);
  }

  private static boolean isSymbolic(int c) {
    return c == '/' || isSymbolicStart(c) || c == '#';
  }

  public static Seq read(Reader r, ReaderContext cxt) {
    return H.lazy(() -> new MaajReader(new PosReader(r), cxt).readAll());
  }
}
