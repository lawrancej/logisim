/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

import java.util.ArrayList;
import java.util.List;

import static com.cburch.logisim.util.LocaleString.getFromLocale;

public final class Parser {
    private Parser() { }

    public static Expression parse(String in, AnalyzerModel model) throws ParserException {
        ArrayList<Token> tokens = toTokens(in, false);

        if (tokens.size() == 0) {
            return null;
        }


        for (Token token : tokens) {
            if (token.type == TOKEN_ERROR) {
                throw token.error(getFromLocale("invalidCharacterError", token.text));
            } else if (token.type == TOKEN_IDENT) {
                int index = model.getInputs().indexOf(token.text);
                if (index < 0) {
                    // ok; but maybe this is an operator
                    String opText = token.text.toUpperCase();
                    switch (opText) {
                        case "NOT":
                            token.type = TOKEN_NOT;
                            break;
                        case "AND":
                            token.type = TOKEN_AND;
                            break;
                        case "XOR":
                            token.type = TOKEN_XOR;
                            break;
                        case "OR":
                            token.type = TOKEN_OR;
                            break;
                        default:
                            throw token.error(getFromLocale("badVariableName", token.text));
                    }
                }
            }
        }

        return parse(tokens);
    }

    /** I wrote this without thinking, and then realized that this is
     * quite complicated because of removing operators. I haven't
     * bothered to do it correctly; instead, it just regenerates a
     * string from the raw expression.
    static String removeVariable(String in, String variable) {
        StringBuilder ret = new StringBuilder();
        ArrayList tokens = toTokens(in, true);
        Token lastWhite = null;
        for (int i = 0, n = tokens.size(); i < n; i++) {
            Token token = (Token) tokens.get(i);
            if (token.type == TOKEN_IDENT && token.text.equals(variable)) {
                // just ignore it
                ;
            } else if (token.type == TOKEN_WHITE) {
                if (lastWhite != null) {
                    if (lastWhite.text.length() >= token.text.length()) {
                        // don't repeat shorter whitespace
                        ;
                    } else {
                        ret.replace(ret.length() - lastWhite.text.length(),
                                ret.length(), token.text);
                        lastWhite = token;
                    }
                } else {
                    lastWhite = token;
                    ret.append(token.text);
                }
            } else {
                lastWhite = null;
                ret.append(token.text);
            }
        }
        return ret.toString();
    }
    */

    static String replaceVariable(String in, String oldName, String newName) {
        StringBuilder ret = new StringBuilder();
        Iterable<Token> tokens = toTokens(in, true);
        for (Token token : tokens) {
            if (token.type == TOKEN_IDENT && token.text.equals(oldName)) {
                ret.append(newName);
            } else {
                ret.append(token.text);
            }
        }
        return ret.toString();
    }

    //
    // tokenizing code
    //
    private static final int TOKEN_AND = 0;
    private static final int TOKEN_OR = 1;
    private static final int TOKEN_XOR = 2;
    private static final int TOKEN_NOT = 3;
    private static final int TOKEN_NOT_POSTFIX = 4;
    private static final int TOKEN_LPAREN = 5;
    private static final int TOKEN_RPAREN = 6;
    private static final int TOKEN_IDENT = 7;
    private static final int TOKEN_CONST = 8;
    private static final int TOKEN_WHITE = 9;
    private static final int TOKEN_ERROR = 10;

    private static class Token {
        int type;
        final int offset;
        final int length;
        final String text;

        Token(int type, int offset, String text) {
            this(type, offset, text.length(), text);
        }

        Token(int type, int offset, int length, String text) {
            this.type = type;
            this.offset = offset;
            this.length = length;
            this.text = text;
        }

        ParserException error(String message) {
            return new ParserException(message, offset, length);
        }
    }

    private static ArrayList<Token> toTokens(String in, boolean includeWhite) {
        String in1 = in;
        ArrayList<Token> tokens = new ArrayList<>();

        // Guarantee that we will stop just after reading whitespace,
        // not in the middle of a token.
        in1 += ' ';
        int pos = 0;
        while (true) {
            int whiteStart = pos;
            while (pos < in1.length() && Character.isWhitespace(in1.charAt(pos))) pos++;
            if (includeWhite && pos != whiteStart) {
                tokens.add(new Token(TOKEN_WHITE, whiteStart, in1.substring(whiteStart, pos)));
            }
            if (pos == in1.length()) {
                return tokens;
            }


            int start = pos;
            char startChar = in1.charAt(pos);
            pos++;
            if (Character.isJavaIdentifierStart(startChar)) {
                while (Character.isJavaIdentifierPart(in1.charAt(pos))) pos++;
                tokens.add(new Token(TOKEN_IDENT, start, in1.substring(start, pos)));
            } else {
                switch (startChar) {
                    case '(':
                        tokens.add(new Token(TOKEN_LPAREN, start, "("));
                        break;
                    case ')':
                        tokens.add(new Token(TOKEN_RPAREN, start, ")"));
                        break;
                    case '0':
                    case '1':
                        tokens.add(new Token(TOKEN_CONST, start, String.valueOf(startChar)));
                        break;
                    case '~':
                        tokens.add(new Token(TOKEN_NOT, start, "~"));
                        break;
                    case '\'':
                        tokens.add(new Token(TOKEN_NOT_POSTFIX, start, "'"));
                        break;
                    case '^':
                        tokens.add(new Token(TOKEN_XOR, start, "^"));
                        break;
                    case '+':
                        tokens.add(new Token(TOKEN_OR, start, "+"));
                        break;
                    case '!':
                        tokens.add(new Token(TOKEN_NOT, start, "!"));
                        break;
                    case '&':
                        if ((int) in1.charAt(pos) == (int) '&') pos++;
                        tokens.add(new Token(TOKEN_AND, start, in1.substring(start, pos)));
                        break;
                    case '|':
                        if ((int) in1.charAt(pos) == (int) '|') pos++;
                        tokens.add(new Token(TOKEN_OR, start, in1.substring(start, pos)));
                        break;
                    default:
                        while (!okCharacter(in1.charAt(pos))) pos++;
                        String errorText = in1.substring(start, pos);
                        tokens.add(new Token(TOKEN_ERROR, start, errorText));
                }
            }
        }
    }

    private static boolean okCharacter(char c) {
        return Character.isWhitespace(c) || Character.isJavaIdentifierStart(c)
            || "()01~^+!&|".indexOf(c) >= 0;
    }

    //
    // parsing code
    //
    private static class Context {
        final int level;
        final Expression current;
        final Token cause;

        Context(Expression current, int level, Token cause) {
            this.level = level;
            this.current = current;
            this.cause = cause;
        }
    }

    private static Expression parse(List<Token> tokens) throws ParserException {
        ArrayList<Context> stack = new ArrayList<>();
        Expression current = null;
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.type == TOKEN_IDENT || t.type == TOKEN_CONST) {
                Expression here;
                if (t.type == TOKEN_IDENT) {
                    here = Expressions.variable(t.text);
                } else {
                    here = Expressions.constant(Integer.parseInt(t.text, 16));
                }
                while (i + 1 < tokens.size() && tokens.get(i + 1).type == TOKEN_NOT_POSTFIX) {
                    here = Expressions.not(here);
                    i++;
                }
                while (peekLevel(stack) == Expression.NOT_LEVEL) {
                    here = Expressions.not(here);
                    pop(stack);
                }
                current = Expressions.and(current, here);
                if (peekLevel(stack) == Expression.AND_LEVEL) {
                    Context top = pop(stack);
                    current = Expressions.and(top.current, current);
                }
            } else if (t.type == TOKEN_NOT) {
                if (current != null) {
                    push(stack, current, Expression.AND_LEVEL,
                        new Token(TOKEN_AND, t.offset, getFromLocale("implicitAndOperator")));
                }
                push(stack, null, Expression.NOT_LEVEL, t);
                current = null;
            } else if (t.type == TOKEN_NOT_POSTFIX) {
                throw t.error(getFromLocale("unexpectedApostrophe"));
            } else if (t.type == TOKEN_LPAREN) {
                if (current != null) {
                    push(stack, current, Expression.AND_LEVEL,
                            new Token(TOKEN_AND, t.offset, 0, getFromLocale("implicitAndOperator")));
                }
                push(stack, null, -2, t);
                current = null;
            } else if (t.type == TOKEN_RPAREN) {
                current = popTo(stack, -1, current);
                // there had better be a LPAREN atop the stack now.
                if (stack.isEmpty()) {
                    throw t.error(getFromLocale("lparenMissingError"));
                }
                pop(stack);
                while (i + 1 < tokens.size() && tokens.get(i + 1).type == TOKEN_NOT_POSTFIX) {
                    current = Expressions.not(current);
                    i++;
                }
                current = popTo(stack, Expression.AND_LEVEL, current);
            } else {
                if (current == null) {
                    throw t.error(getFromLocale("missingLeftOperandError", t.text));
                }
                int level = 0;
                switch (t.type) {
                case TOKEN_AND: level = Expression.AND_LEVEL; break;
                case TOKEN_OR: level = Expression.OR_LEVEL; break;
                case TOKEN_XOR: level = Expression.XOR_LEVEL; break;
                }
                push(stack, popTo(stack, level, current), level, t);
                current = null;
            }
        }
        current = popTo(stack, -1, current);
        if (!stack.isEmpty()) {
            Context top = pop(stack);
            throw top.cause.error(getFromLocale("rparenMissingError"));
        }
        return current;
    }

    private static void push(List<Context> stack, Expression expr,
                             int level, Token cause) {
        stack.add(new Context(expr, level, cause));
    }
    private static int peekLevel(List<Context> stack) {
        if (stack.isEmpty()) {
            return -3;
        }

        Context context = stack.get(stack.size() - 1);
        return context.level;
    }
    private static Context pop(List<Context> stack) {
        return stack.remove(stack.size() - 1);
    }

    private static Expression popTo(ArrayList<Context> stack, int level,
                                    Expression current) throws ParserException {
        Expression current1 = current;
        while (!stack.isEmpty() && peekLevel(stack) >= level) {
            Context top = pop(stack);
            if (current1 == null) {
                throw top.cause.error(getFromLocale("missingRightOperandError", top.cause.text));
            }

            switch (top.level) {
                case Expression.AND_LEVEL:
                    current1 = Expressions.and(top.current, current1);
                    break;
                case Expression.OR_LEVEL:
                    current1 = Expressions.or(top.current, current1);
                    break;
                case Expression.XOR_LEVEL:
                    current1 = Expressions.xor(top.current, current1);
                    break;
                case Expression.NOT_LEVEL:
                    current1 = Expressions.not(current1);
                    break;
            }
        }
        return current1;
    }
}
