package it.unive.lisa.analysis.nonrelational.value.impl;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.TernaryOperator;
import it.unive.lisa.symbolic.value.UnaryOperator;

public class SignDomain extends BaseNonRelationalValueDomain<SignDomain> {

	/*
	 * Operations are implemented inside the enumeration, just to keep the code
	 * cleaner. The result is the same as having the switch inside the methods, as
	 * we saw during class
	 */
	enum Sign {
		PLUS {
			@Override
			Sign minus() {
				return MINUS;
			}

			@Override
			Sign add(Sign other) {
				// add(+, +) = +
				// add(+, -) = top
				// add(+, 0) = +
				// add(+, top) = top
				// add(+, bottom) = bottom
				if (other == TOP || other == BOTTOM)
					return other;
				return other == MINUS ? TOP : this;
			}

			@Override
			Sign div(Sign other) {
				// div(+, +) = +
				// div(+, -) = -
				// div(+, 0) = bottom
				// div(+, top) = top
				// div(+, bottom) = bottom
				return other == ZERO ? BOTTOM : other;
			}

			@Override
			Sign mul(Sign other) {
				// mul(+, +) = +
				// mul(+, -) = -
				// mul(+, 0) = 0
				// mul(+, top) = top
				// mul(+, bottom) = bottom
				return other;
			}

			@Override
			Sign mod(Sign other) {
				// mod(+, +) = top (the numbers might be equal)
				// mod(+, -) = top (the numbers might be equal)
				// mod(+, 0) = bottom
				// mod(+, top) = top (might be zero or the same number)
				// mod(+, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			public String toString() {
				return "+";
			}

		},
		MINUS {
			@Override
			Sign minus() {
				return PLUS;
			}

			@Override
			Sign add(Sign other) {
				// add(-, +) = top
				// add(-, -) = -
				// add(-, 0) = -
				// add(-, top) = top
				// add(-, bottom) = bottom
				if (other == TOP || other == BOTTOM)
					return other;
				return other == PLUS ? TOP : this;
			}

			@Override
			Sign div(Sign other) {
				// div(-, +) = -
				// div(-, -) = +
				// div(-, 0) = bottom
				// div(-, top) = top
				// div(-, bottom) = bottom
				return other == ZERO ? BOTTOM : other.minus();
			}

			@Override
			Sign mul(Sign other) {
				// mul(-, +) = -
				// mul(-, -) = +
				// mul(-, 0) = 0
				// mul(-, top) = top
				// mul(-, bottom) = bottom
				return other.minus();
			}

			@Override
			Sign mod(Sign other) {
				// mod(-, +) = top (the numbers might be equal)
				// mod(-, -) = top (the numbers might be equal)
				// mod(-, 0) = bottom
				// mod(-, top) = top (might be zero or the same number)
				// mod(-, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			public String toString() {
				return "-";
			}

		},
		ZERO {
			@Override
			Sign minus() {
				return ZERO;
			}

			@Override
			Sign add(Sign other) {
				// add(0, +) = +
				// add(0, -) = -
				// add(0, 0) = 0
				// add(0, top) = top
				// add(0, bottom) = bottom
				return other;
			}

			@Override
			Sign div(Sign other) {
				// div(0, +) = 0
				// div(0, -) = 0
				// div(0, 0) = bottom
				// div(0, top) = 0
				// div(0, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0, +) = 0
				// mul(0, -) = 0
				// mul(0, 0) = 0
				// mul(0, top) = 0
				// mul(0, bottom) = bottom
				return other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			Sign mod(Sign other) {
				// mod(0, +) = 0
				// mod(0, -) = 0
				// mod(0, 0) = bottom
				// mod(0, top) = 0
				// mod(0, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			public String toString() {
				return "0";
			}

		},
		TOP {
			@Override
			Sign minus() {
				return this;
			}

			@Override
			Sign add(Sign other) {
				// add(top, +) = top
				// add(top, -) = top
				// add(top, 0) = top
				// add(top, top) = top
				// add(top, bottom) = bottom
				return other == BOTTOM ? BOTTOM : this;
			}

			@Override
			Sign div(Sign other) {
				// div(top, +) = top
				// div(top, -) = top
				// div(top, 0) = bottom
				// div(top, top) = top
				// div(top, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(top, +) = top
				// mul(top, -) = top
				// mul(top, 0) = 0
				// mul(top, top) = top
				// mul(top, bottom) = bottom
				return other == BOTTOM ? BOTTOM : other == ZERO ? ZERO : TOP;
			}

			@Override
			Sign mod(Sign other) {
				// mod(top, +) = top
				// mod(top, -) = top
				// mod(top, 0) = bottom
				// mod(top, top) = top
				// mod(top, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			public String toString() {
				return Lattice.TOP_STRING;
			}

		},
		BOTTOM {
			@Override
			Sign minus() {
				return this;
			}

			@Override
			Sign add(Sign other) {
				return this;
			}

			@Override
			Sign div(Sign other) {
				return this;
			}

			@Override
			Sign mul(Sign other) {
				return this;
			}

			@Override
			Sign mod(Sign other) {
				return this;
			}

			@Override
			public String toString() {
				return Lattice.BOTTOM_STRING;
			}

		};

		abstract Sign minus();

		abstract Sign add(Sign other);

		final Sign sub(Sign other) {
			return add(other.minus());
		}

		abstract Sign div(Sign other);

		abstract Sign mul(Sign other);

		abstract Sign mod(Sign other);

		@Override
		public abstract String toString();
	}

	private final Sign sign;

	public SignDomain() {
		this(Sign.TOP);
	}

	private SignDomain(Sign sign) {
		this.sign = sign;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sign == null) ? 0 : sign.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignDomain other = (SignDomain) obj;
		return sign == other.sign;
	}

	@Override
	public SignDomain top() {
		return new SignDomain(Sign.TOP);
	}

	@Override
	public boolean isTop() {
		return sign == Sign.TOP;
	}

	@Override
	public SignDomain bottom() {
		return new SignDomain(Sign.BOTTOM);
	}

	@Override
	public boolean isBottom() {
		return sign == Sign.BOTTOM;
	}

	@Override
	public String representation() {
		return sign.toString();
	}

	@Override
	protected SignDomain evalNullConstant(ProgramPoint pp) {
		return top();
	}

	@Override
	protected SignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			int c = (int) constant.getValue();
			if (c == 0)
				return new SignDomain(Sign.ZERO);
			else if (c > 0)
				return new SignDomain(Sign.PLUS);
			else
				return new SignDomain(Sign.MINUS);
		}
		return top();
	}

	@Override
	protected SignDomain evalUnaryExpression(UnaryOperator operator, SignDomain arg, ProgramPoint pp) {
		if (operator == UnaryOperator.NUMERIC_NEG) {
			return new SignDomain(arg.sign.minus());
		}
		return top();
	}

	@Override
	protected SignDomain evalBinaryExpression(BinaryOperator operator, SignDomain left, SignDomain right,
			ProgramPoint pp) {
		switch (operator) {
		case NUMERIC_ADD:
			return new SignDomain(left.sign.add(right.sign));
		case NUMERIC_DIV:
			return new SignDomain(left.sign.div(right.sign));
		case NUMERIC_MOD:
			return new SignDomain(left.sign.mod(right.sign));
		case NUMERIC_MUL:
			return new SignDomain(left.sign.mul(right.sign));
		case NUMERIC_SUB:
			return new SignDomain(left.sign.sub(right.sign));
		default:
			return top();

		}
	}

	@Override
	protected SignDomain evalTernaryExpression(TernaryOperator operator, SignDomain left, SignDomain middle,
			SignDomain right, ProgramPoint pp) {
		return top();
	}

	@Override
	protected Satisfiability satisfiesAbstractValue(SignDomain value, ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Satisfiability satisfiesNullConstant(ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Satisfiability satisfiesNonNullConstant(Constant constant, ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Satisfiability satisfiesUnaryExpression(UnaryOperator operator, SignDomain arg, ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Satisfiability satisfiesBinaryExpression(BinaryOperator operator, SignDomain left, SignDomain right,
			ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Satisfiability satisfiesTernaryExpression(TernaryOperator operator, SignDomain left, SignDomain middle,
			SignDomain right, ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected SignDomain lubAux(SignDomain other) throws SemanticException {
		return top();
	}

	@Override
	protected SignDomain wideningAux(SignDomain other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	protected boolean lessOrEqualAux(SignDomain other) throws SemanticException {
		return false;
	}
}
