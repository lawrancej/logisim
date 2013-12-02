/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;

import java.util.HashSet;

public abstract class Expression {
    public static final int OR_LEVEL = 0;
    public static final int XOR_LEVEL = 1;
    public static final int AND_LEVEL = 2;
    public static final int NOT_LEVEL = 3;

    static interface Visitor {
        public void visitAnd(Expression a, Expression b);
        public void visitOr(Expression a, Expression b);
        public void visitXor(Expression a, Expression b);
        public void visitNot(Expression a);
        public void visitVariable(String name);
        public void visitConstant(int value);
    }

    static interface IntVisitor {
        public int visitAnd(Expression a, Expression b);
        public int visitOr(Expression a, Expression b);
        public int visitXor(Expression a, Expression b);
        public int visitNot(Expression a);
        public int visitVariable(String name);
        public int visitConstant(int value);
    }

    public abstract int getPrecedence();
    public abstract <T> T visit(ExpressionVisitor<T> visitor);
    abstract void visit(Visitor visitor);
    abstract int visit(IntVisitor visitor);

    public boolean evaluate(final Assignments assignments) {
        int ret = visit(new IntVisitor() {
            @Override
            public int visitAnd(Expression a, Expression b) {
                return a.visit(this) & b.visit(this);
            }
            @Override
            public int visitOr(Expression a, Expression b) {
                return a.visit(this) | b.visit(this);
            }
            @Override
            public int visitXor(Expression a, Expression b) {
                return a.visit(this) ^ b.visit(this);
            }
            @Override
            public int visitNot(Expression a) {
                return ~a.visit(this);
            }
            @Override
            public int visitVariable(String name) {
                return assignments.get(name) ? 1 : 0;
            }
            @Override
            public int visitConstant(int value) {
                return value;
            }
        });
        return (ret & 1) != 0;
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        visit(new Visitor() {
            @Override
            public void visitAnd(Expression a, Expression b) { binary(a, b, AND_LEVEL, " "); }
            @Override
            public void visitOr(Expression a, Expression b) { binary(a, b, OR_LEVEL, " + "); }
            @Override
            public void visitXor(Expression a, Expression b) { binary(a, b, XOR_LEVEL, " ^ "); }

            private void binary(Expression a, Expression b, int level, String op) {
                if (a.getPrecedence() < level) {
                    text.append("("); a.visit(this); text.append(")");
                } else {
                    a.visit(this);
                }
                text.append(op);
                if (b.getPrecedence() < level) {
                    text.append("("); b.visit(this); text.append(")");
                } else {
                    b.visit(this);
                }
            }

            @Override
            public void visitNot(Expression a) {
                text.append("~");
                if (a.getPrecedence() < NOT_LEVEL) {
                    text.append("("); a.visit(this); text.append(")");
                } else {
                    a.visit(this);
                }
            }

            @Override
            public void visitVariable(String name) {
                text.append(name);
            }

            @Override
            public void visitConstant(int value) {
                text.append("" + Integer.toString(value, 16));
            }
        });
        return text.toString();
    }

    public boolean isCircular() {
        final HashSet<Expression> visited = new HashSet<Expression>();
        visited.add(this);
        return 1 == visit(new IntVisitor() {
            @Override
            public int visitAnd(Expression a, Expression b) { return binary(a, b); }
            @Override
            public int visitOr(Expression a, Expression b) { return binary(a, b); }
            @Override
            public int visitXor(Expression a, Expression b) { return binary(a, b); }
            @Override
            public int visitNot(Expression a) {
                if (!visited.add(a)) {
                    return 1;
                }

                if (a.visit(this) == 1) {
                    return 1;
                }

                visited.remove(a);
                return 0;
            }
            @Override
            public int visitVariable(String name) { return 0; }
            @Override
            public int visitConstant(int value) { return 0; }

            private int binary(Expression a, Expression b) {
                if (!visited.add(a)) {
                    return 1;
                }

                if (a.visit(this) == 1) {
                    return 1;
                }

                visited.remove(a);

                if (!visited.add(b)) {
                    return 1;
                }

                if (b.visit(this) == 1) {
                    return 1;
                }

                visited.remove(b);

                return 0;
            }
        });
    }

    Expression removeVariable(final String input) {
        return visit(new ExpressionVisitor<Expression>() {
            @Override
            public Expression visitAnd(Expression a, Expression b) {
                Expression l = a.visit(this);
                Expression r = b.visit(this);
                if (l == null) {
                    return r;
                }

                if (r == null) {
                    return l;
                }

                return Expressions.and(l, r);
            }
            @Override
            public Expression visitOr(Expression a, Expression b) {
                Expression l = a.visit(this);
                Expression r = b.visit(this);
                if (l == null) {
                    return r;
                }

                if (r == null) {
                    return l;
                }

                return Expressions.or(l, r);
            }
            @Override
            public Expression visitXor(Expression a, Expression b) {
                Expression l = a.visit(this);
                Expression r = b.visit(this);
                if (l == null) {
                    return r;
                }

                if (r == null) {
                    return l;
                }

                return Expressions.xor(l, r);
            }
            @Override
            public Expression visitNot(Expression a) {
                Expression l = a.visit(this);
                if (l == null) {
                    return null;
                }

                return Expressions.not(l);
            }
            @Override
            public Expression visitVariable(String name) {
                return name.equals(input) ? null : Expressions.variable(name);
            }
            @Override
            public Expression visitConstant(int value) {
                return Expressions.constant(value);
            }
        });
    }

    Expression replaceVariable(final String oldName, final String newName) {
        return visit(new ExpressionVisitor<Expression>() {
            @Override
            public Expression visitAnd(Expression a, Expression b) {
                Expression l = a.visit(this);
                Expression r = b.visit(this);
                return Expressions.and(l, r);
            }
            @Override
            public Expression visitOr(Expression a, Expression b) {
                Expression l = a.visit(this);
                Expression r = b.visit(this);
                return Expressions.or(l, r);
            }
            @Override
            public Expression visitXor(Expression a, Expression b) {
                Expression l = a.visit(this);
                Expression r = b.visit(this);
                return Expressions.xor(l, r);
            }
            @Override
            public Expression visitNot(Expression a) {
                Expression l = a.visit(this);
                return Expressions.not(l);
            }
            @Override
            public Expression visitVariable(String name) {
                return Expressions.variable(name.equals(oldName) ? newName : name);
            }
            @Override
            public Expression visitConstant(int value) {
                return Expressions.constant(value);
            }
        });
    }

    public boolean containsXor() {
        return 1 == visit(new IntVisitor() {
            @Override
            public int visitAnd(Expression a, Expression b) {
                return a.visit(this) == 1 || b.visit(this) == 1 ? 1 : 0;
            }
            @Override
            public int visitOr(Expression a, Expression b) {
                return a.visit(this) == 1 || b.visit(this) == 1 ? 1 : 0;
            }
            @Override
            public int visitXor(Expression a, Expression b) {
                return 1;
            }
            @Override
            public int visitNot(Expression a) {
                return a.visit(this);
            }
            @Override
            public int visitVariable(String name) {
                return 0;
            }
            @Override
            public int visitConstant(int value) {
                return 0;
            }
        });
    }

    public boolean isCnf() {
        return 1 == visit(new IntVisitor() {
            int level = 0;

            @Override
            public int visitAnd(Expression a, Expression b) {
                if (level > 1) {
                    return 0;
                }

                int oldLevel = level;
                level = 1;
                int ret = a.visit(this) == 1 && b.visit(this) == 1 ? 1 : 0;
                level = oldLevel;
                return ret;
            }
            @Override
            public int visitOr(Expression a, Expression b) {
                if (level > 0) {
                    return 0;
                }

                return a.visit(this) == 1 && b.visit(this) == 1 ? 1 : 0;
            }
            @Override
            public int visitXor(Expression a, Expression b) {
                return 0;
            }
            @Override
            public int visitNot(Expression a) {
                if (level == 2) {
                    return 0;
                }

                int oldLevel = level;
                level = 2;
                int ret = a.visit(this);
                level = oldLevel;
                return ret;
            }
            @Override
            public int visitVariable(String name) {
                return 1;
            }
            @Override
            public int visitConstant(int value) {
                return 1;
            }
        });
    }
}
