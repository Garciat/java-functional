package com.garciat.functional.parser;

import lombok.EqualsAndHashCode;
import lombok.Value;

public abstract class ParseFailure {

  private ParseFailure() {}

  // formatting

  public String format() {
    // TODO
    return toString();
  }

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
  }

  @Value
  @EqualsAndHashCode(callSuper=false)
  public static class Merge extends ParseFailure {
    ParseFailure left;
    ParseFailure right;

    @Override
    public String toString() {
      return "ParseFailure.merge(" + left + ", " + right + ")";
    }
  }
}
