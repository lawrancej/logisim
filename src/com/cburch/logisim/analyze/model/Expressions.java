/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.model;


public class Expressions {
	private Expressions() { }
	
	private static abstract class Binary extends Expression {
		protected final Expression a;
		protected final Expression b;
		
		Binary(Expression a, Expression b) {
			this.a = a;
			this.b = b;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == null) return false;
			if (this.getClass() != other.getClass()) return false;
			Binary o = (Binary) other;
			return this.a.equals(o.a) && this.b.equals(o.b);
		}
		
		@Override
		public int hashCode() {
			return 31 * (31 * getClass().hashCode() + a.hashCode())
				+ b.hashCode();
		}
	}
	
	private static class And extends Binary {
		And(Expression a, Expression b) {
			super(a, b);
		}
		
		@Override
		public <T> T visit(ExpressionVisitor<T> visitor) {
			return visitor.visitAnd(a, b);
		}
		
		@Override
		void visit(Visitor visitor) {
			visitor.visitAnd(a, b);
		}
		
		@Override
		int visit(IntVisitor visitor) {
			return visitor.visitAnd(a, b);
		}
		
		@Override
		public int getPrecedence() {
			return Expression.AND_LEVEL;
		}
	}

	private static class Or extends Binary {
		Or(Expression a, Expression b) {
			super(a, b);
		}
		
		@Override
		public <T> T visit(ExpressionVisitor<T> visitor) {
			return visitor.visitOr(a, b);
		}
		
		@Override
		void visit(Visitor visitor) {
			visitor.visitOr(a, b);
		}
				
		@Override
		int visit(IntVisitor visitor) {
			return visitor.visitOr(a, b);
		}
		
		@Override
		public int getPrecedence() {
			return Expression.OR_LEVEL;
		}
	}

	private static class Xor extends Binary {
		Xor(Expression a, Expression b) {
			super(a, b);
		}
		
		@Override
		public <T> T visit(ExpressionVisitor<T> visitor) {
			return visitor.visitXor(a, b);
		}
		
		@Override
		void visit(Visitor visitor) {
			visitor.visitXor(a, b);
		}
				
		@Override
		int visit(IntVisitor visitor) {
			return visitor.visitXor(a, b);
		}

		@Override
		public int getPrecedence() {
			return Expression.XOR_LEVEL;
		}
	}

	private static class Not extends Expression {
		private Expression a;
		
		Not(Expression a) {
			this.a = a;
		}
		
		@Override
		public <T> T visit(ExpressionVisitor<T> visitor) {
			return visitor.visitNot(a);
		}
		
		@Override
		void visit(Visitor visitor) {
			visitor.visitNot(a);
		}
		
		@Override
		int visit(IntVisitor visitor) {
			return visitor.visitNot(a);
		}

		@Override
		public int getPrecedence() {
			return Expression.NOT_LEVEL;
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Not)) return false;
			Not o = (Not) other;
			return this.a.equals(o.a);
		}
		
		@Override
		public int hashCode() {
			return 31 * a.hashCode();
		}
	}
	
	private static class Variable extends Expression {
		private String name;
		
		Variable(String name) {
			this.name = name;
		}
		
		@Override
		public <T> T visit(ExpressionVisitor<T> visitor) {
			return visitor.visitVariable(name);
		}
		
		@Override
		void visit(Visitor visitor) {
			visitor.visitVariable(name);
		}
		
		@Override
		int visit(IntVisitor visitor) {
			return visitor.visitVariable(name);
		}
		
		@Override
		public int getPrecedence() {
			return Integer.MAX_VALUE;
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Variable)) return false;
			Variable o = (Variable) other;
			return this.name.equals(o.name);
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
	
	private static class Constant extends Expression {
		private int value;
		
		Constant(int value) {
			this.value = value;
		}
		
		@Override
		public <T> T visit(ExpressionVisitor<T> visitor) {
			return visitor.visitConstant(value);
		}
				
		@Override
		void visit(Visitor visitor) {
			visitor.visitConstant(value);
		}
				
		@Override
		int visit(IntVisitor visitor) {
			return visitor.visitConstant(value);
		}

		@Override
		public int getPrecedence() {
			return Integer.MAX_VALUE;
		}
				
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Constant)) return false;
			Constant o = (Constant) other;
			return this.value == o.value;
		}
		
		@Override
		public int hashCode() {
			return value;
		}
	}

	public static Expression and(Expression a, Expression b) {
		if (a == null) return b;
		if (b == null) return a;
		return new And(a, b);
	}

	public static Expression or(Expression a, Expression b) {
		if (a == null) return b;
		if (b == null) return a;
		return new Or(a, b);
	}
	
	public static Expression xor(Expression a, Expression b) {
		if (a == null) return b;
		if (b == null) return a;
		return new Xor(a, b);
	}
	
	public static Expression not(Expression a) {
		if (a == null) return null;
		return new Not(a);
	}
	
	public static Expression variable(String name) {
		return new Variable(name);
	}
	
	public static Expression constant(int value) {
		return new Constant(value);
	}
}
