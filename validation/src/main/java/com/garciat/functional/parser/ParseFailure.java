package com.garciat.functional.parser;

import com.garciat.functional.functions.Getter;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.function.Function;

public abstract class ParseFailure {

  private ParseFailure() {}

  public final String format() {
    return format(new Formatter());
  }

  protected abstract String format(Formatter formatter);

  // constructors

  public static ParseFailure message(String message) {
    return new Message(message);
  }

  public static ParseFailure exception(Throwable t) {
    return new Exception(t);
  }

  // combinators

  public static ParseFailure tag(String tag, ParseFailure sub) {
    return new Tag(tag, sub);
  }

  public static ParseFailure type(Class<?> type, ParseFailure sub) {
    return new Type(type, sub);
  }

  public static ParseFailure getter(Getter<?, ?> getter, ParseFailure sub) {
    return new Field(getter, sub);
  }

  public static ParseFailure merge(ParseFailure left, ParseFailure right) {
    return new Merge(left, right);
  }

  // data

  @Value
  @EqualsAndHashCode(callSuper=false)
  public static class Message extends ParseFailure {
    String message;

    @Override
    public String toString() {
      return "ParseFailure.message(\"" + message + "\")";
    }

    protected String format(Formatter formatter) {
      return formatter.line(message);
    }
  }

  @Value
  @EqualsAndHashCode(callSuper=false)
  public static class Exception extends ParseFailure {
    Throwable throwable;

    @Override
    public String toString() {
      String s = "ParseFailure.exception(type=" + throwable.getClass().getName();
      if (throwable.getMessage() != null) {
        s += ", message=\"" + throwable.getMessage() + '"';
      }
      return s + ")";
    }

    protected String format(Formatter formatter) {
      String s = "exception=" + throwable.getClass().getTypeName();
      if (throwable.getMessage() != null) {
        s += " message=" + throwable.getMessage();
      }
      return formatter.line(s);
    }
  }

  @Value
  @EqualsAndHashCode(callSuper=false)
  public static class Tag extends ParseFailure {
    String tag;
    ParseFailure sub;

    @Override
    public String toString() {
      return "ParseFailure.tag(\"" + tag + "\", " + sub + ")";
    }

    protected String format(Formatter formatter) {
      return formatter.line(tag)
              + formatter.indented(sub::format);
    }
  }

  @Value
  @EqualsAndHashCode(callSuper=false)
  public static class Type extends ParseFailure {
    Class<?> type;
    ParseFailure sub;

    @Override
    public String toString() {
      return "ParseFailure.type(" + type + ", " + sub + ")";
    }

    protected String format(Formatter formatter) {
      return formatter.line("Parsing type " + type.getName())
              + formatter.indented(sub::format);
    }
  }

  @Value
  @EqualsAndHashCode(callSuper=false)
  public static class Field extends ParseFailure {
    Getter<?, ?> getter;
    ParseFailure sub;

    @Override
    public String toString() {
      return "ParseFailure.field(" + getter.getInfo() + ", " + sub + ")";
    }

    protected String format(Formatter formatter) {
      return formatter.line("Parsing field " + getter.getInfo().getImplMethodName())
              + formatter.indented(sub::format);
    }
  }

  @Value
  @EqualsAndHashCode(callSuper=false)
  public static class Merge extends ParseFailure {
    ParseFailure left, right;

    @Override
    public String toString() {
      return "ParseFailure.merge(" + left + ", " + right + ")";
    }

    protected String format(Formatter formatter) {
      return left.format(formatter) + right.format(formatter);
    }
  }

  public static final class Formatter {
    public static final int INDENT_STEP = 2;
    private final int indent;
    private Formatter() {
      this(0);
    }
    private Formatter(int indent) {
      this.indent = indent;
    }
    public <T> T indented(Function<Formatter, T> cont) {
      return cont.apply(new Formatter(indent + INDENT_STEP));
    }
    public String line(String s) {
      return new String(new char[indent]).replace("\0", " ") + s + "\n";
    }
  }
}
