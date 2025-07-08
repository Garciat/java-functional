package com.garciat.functional.parser;

import com.garciat.functional.functions.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public sealed interface ParseFailure {

    static ParseFailure message(String message) {
        return new Message(message);
    }

    static ParseFailure exception(Throwable t) {
        return new Exception(t);
    }

    // constructors

    static ParseFailure tag(String tag, ParseFailure sub) {
        return new Tag(tag, sub);
    }

    static ParseFailure type(Class<?> type, ParseFailure sub) {
        return new Type(type, sub);
    }

    // combinators

    static ParseFailure getter(Getter<?, ?> getter, ParseFailure sub) {
        return new Field(getter, sub);
    }

    static ParseFailure merge(ParseFailure left, ParseFailure right) {
        return new Merge(left, right);
    }

    default String format() {
        return format(new Formatter());
    }

    String format(Formatter formatter);

    // data

    record Message(String message) implements ParseFailure {

        @Override
        @NotNull
        public String toString() {
            return "ParseFailure.message(\"" + message + "\")";
        }

        @Override
        public String format(Formatter formatter) {
            return formatter.line(message);
        }
    }

    record Exception(Throwable throwable) implements ParseFailure {

        @Override
        @NotNull
        public String toString() {
            String s = "ParseFailure.exception(type=" + throwable.getClass().getName();
            if (throwable.getMessage() != null) {
                s += ", message=\"" + throwable.getMessage() + '"';
            }
            return s + ")";
        }

        @Override
        public String format(Formatter formatter) {
            String s = "exception=" + throwable.getClass().getTypeName();
            if (throwable.getMessage() != null) {
                s += " message=" + throwable.getMessage();
            }
            return formatter.line(s);
        }
    }

    record Tag(String tag, ParseFailure sub) implements ParseFailure {

        @Override
        @NotNull
        public String toString() {
            return "ParseFailure.tag(\"" + tag + "\", " + sub + ")";
        }

        @Override
        public String format(Formatter formatter) {
            return formatter.line(tag)
                    + formatter.indented(sub::format);
        }
    }

    record Type(Class<?> type, ParseFailure sub) implements ParseFailure {

        @Override
        @NotNull
        public String toString() {
            return "ParseFailure.type(" + type + ", " + sub + ")";
        }

        @Override
        public String format(Formatter formatter) {
            return formatter.line("Parsing type " + type.getName())
                    + formatter.indented(sub::format);
        }
    }

    record Field(Getter<?, ?> getter,
                 ParseFailure sub) implements ParseFailure {


        @Override
        @NotNull
        public String toString() {
            return "ParseFailure.field(" + getter.getInfo() + ", " + sub + ")";
        }

        @Override
        public String format(Formatter formatter) {
            return formatter.line("Parsing field " + getter.getInfo().implMethodName())
                    + formatter.indented(sub::format);
        }
    }

    record Merge(ParseFailure left, ParseFailure right) implements ParseFailure {

        @Override
        @NotNull
        public String toString() {
            return "ParseFailure.merge(" + left + ", " + right + ")";
        }

        @Override
        public String format(Formatter formatter) {
            return left.format(formatter) + right.format(formatter);
        }
    }

    final class Formatter {
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
