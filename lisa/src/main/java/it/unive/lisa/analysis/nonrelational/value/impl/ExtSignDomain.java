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

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

	enum Sign {
		PLUS {
			@Override
			Sign minus() {
				return MINUS;
			}

			@Override
			Sign add(Sign other) {

				// add(+, -) = top
				// add(+, 0-) = top
				if(other == MINUS || other == ZERO_MINUS)
					return TOP;

				// add(+, top) = top
				// add(+, bottom) = bottom
				if (other == TOP || other == BOTTOM)
					return other;

				// add(+, +) = +
				// add(+, 0) = +
				// add(+, 0+) = +
				return this;
			}

			@Override
			Sign div(Sign other) {

				// div(+, 0+) = bottom
				if(other == ZERO_PLUS)
					return PLUS;

				// div(+, 0-) = MINUS
				if(other == ZERO_MINUS)
						return MINUS;

				// div(+, 0) = bottom
				if(other == ZERO)
					return BOTTOM;

				// div(+, bottom) = bottom
				// div(+, +) = +
				// div(+, -) = -
				// div(+, top) = top
				return other;
			}

			@Override
			Sign mul(Sign other) {
				// mul(+, +) = +
				// mul(+, -) = -
				// mul(+, 0) = 0
				// mul(+, 0-) = 0-
				// mul(+, 0+) = 0+
				// mul(+, top) = top
				// mul(+, bottom) = bottom
				return other;
			}

			@Override
			Sign mod(Sign other) {
				// mod(+, +-) = 0+ (the numbers might be equal)
				if(other == PLUS || other == MINUS)
					return ZERO_PLUS;

				// mod(+, 0) = bottom
				if(other == ZERO)
					return BOTTOM;

				// mod(+, 0+-) = top
				if(other == ZERO_MINUS || other == ZERO_PLUS)
					return TOP;

				// mod(+, top) = top (might be zero or the same number)
				// mod(+, bottom) = bottom
				return other;
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
				// add(-, 0+) = top
				if(other == ZERO_PLUS || other == PLUS)
					return TOP;

				// add(-, top) = top
				// add(-, bottom) = bottom
				if (other == TOP || other == BOTTOM)
					return other;

				// add(-, -) = -
				// add(-, 0) = -
				// add(-, 0-) = -
				return this;
			}

			@Override
			Sign div(Sign other) {

				// div(-, -) = +
				// div(-, 0-) = +
				if(other == MINUS || other == ZERO_MINUS)
					return PLUS;

				// div(-, top) = top
				if(other == TOP)
					return TOP;

				// div(-, 0) = bottom
				// div(-, bottom) = bottom
				if(other == BOTTOM || other == ZERO)
					return BOTTOM;

				// div(-, 0+) = -
				// div(-, +) = -
				return this;
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
				// mod(-, +-) = 0- (the numbers might be equal)
				if(other == PLUS || other == MINUS)
					return ZERO_MINUS;

				// mod(-, 0) = bottom
				if(other == ZERO)
					return BOTTOM;

				// mod(-, 0-+) = top
				if(other == ZERO_MINUS || other == ZERO_PLUS)
					return TOP;

				// mod(-, top) = top (might be zero or the same number)
				// mod(-, bottom) = bottom
				return other;
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
				// add(0, 0+) = 0+
				// add(0, 0-) = 0-
				// add(0, top) = top
				// add(0, bottom) = bottom
				return other;
			}

			@Override
			Sign div(Sign other) {
				// div(0, +) = 0
				// div(0, 0+) = 0
				// div(0, -) = 0
				// div(0, 0-) = 0
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
				//mod(0, 0-+) = bottom or 0 --> top
				if(other == ZERO_MINUS || other == ZERO_PLUS)
					return TOP;

				// mod(0, +) = 0
				// mod(0, -) = 0
				// mod(0, top) = 0
				// mod(0, 0) = bottom
				// mod(0, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			public String toString() {
				return "0";
			}

		},
		ZERO_PLUS {
			@Override
			Sign minus() {
				return ZERO_MINUS;
			}

			@Override
			Sign add(Sign other) {
				// add(0+, 0) = 0+
				if(other == ZERO)
					return ZERO_PLUS;

				// add(0+, 0-) = top
				if(other == ZERO_PLUS)
					return TOP;

				// add(0+, +) = +
				// add(0+, -) = -
				// add(0+, 0+) = 0+
				// add(0+, top) = top
				// add(0+, bottom) = bottom
				return other;
			}

			@Override
			Sign div(Sign other) {
				// div(0+, +) = 0+
				if(other == PLUS)
					return ZERO_PLUS;

				// div(0+, -) = 0-
				if(other == MINUS)
					return ZERO_MINUS;

				// div(0+, 0+) = top
				// div(0+, 0-) = top
				// div(0+, top) = top
				// div(0+, bottom) = bottom
				// div(0+, 0) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0+, +) = 0+
				// mul(0+, 0+) = 0+
				if(other == PLUS || other == ZERO_PLUS)
					return ZERO_PLUS;

				// mul(0+, -) = 0-
				// mul(0+, 0-) = 0-
				if(other == MINUS || other == ZERO_MINUS)
					return ZERO_MINUS;

				// mul(0+, 0) = 0
				// mul(0+, top) = 0
				// mul(0+, bottom) = bottom
				return other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			Sign mod(Sign other) {
				// mod(0+, +-) = 0 or + -> 0+
				if(other == MINUS || other == PLUS)
					return ZERO_PLUS;

				// mod(0+, 0) = bottom
				// mod(0+, bottom) = bottom
				if(other == ZERO|| other == BOTTOM )
					return BOTTOM;

				// mod(0+, 0-+) = bottom or 0+ -> top
				// mod(0+, top) = 0 or top -> top
				return TOP;
			}

			@Override
			public String toString() {
				return "0+";
			}

		},
		ZERO_MINUS {
			@Override
			Sign minus() {
				return ZERO_PLUS;
			}

			@Override
			Sign add(Sign other) {
				// add(0-, +) = top
				// add(0-, 0+) = top
				if(other == PLUS || other == ZERO_PLUS)
					return TOP;

				// add(0-, 0) = 0-
				if(other == ZERO)
					return ZERO_MINUS;

				// add(0-, 0-) = 0-
				// add(0-, bottom) = bottom
				// add(0-, -) = -
				// add(0-, top) = top
				return other;
			}

			@Override
			Sign div(Sign other) {
				// div(0-, +) = -
				// div(0-, -) = +
				if(other == PLUS || other == MINUS)
					return other.minus();

				//div(0-, 0-) = top
				//div(0-, 0+) = top
				if(other == ZERO_PLUS || other == ZERO_MINUS)
					return TOP;

				// div(0-, 0) = bottom
				if(other == ZERO)
					return BOTTOM;

				// div(0-, top) = top
				// div(0-, bottom) = bottom
				return other;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0-, +) = 0-
				if(other == PLUS)
					return ZERO_MINUS;

				// mul(0-, -) = 0+
				if(other == MINUS)
					return ZERO_PLUS;

				// mul(0-, 0+) = 0-
				if(other == ZERO_PLUS)
					return ZERO_MINUS;

				// mul(0-, 0) = 0
				// mul(0-, top) = top
				// mul(0-, bottom) = bottom
				return other;
			}

			@Override
			Sign mod(Sign other) {
				// mod(0-, +-) = 0-
				if(other == MINUS || other == PLUS)
					return ZERO_MINUS;

				// mod(0-, 0) = bottom
				// mod(0-, bottom) = bottom
				if(other == ZERO || other == BOTTOM)
					return BOTTOM;

				// mod(0-, 0+-) = 0- or bottom -> top
				// mod(0-, top) = 0 or top -> top
				return TOP;
			}

			@Override
			public String toString() {
				return "0-";
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
				// add(top, 0+) = top
				// add(top, 0-) = top
				// add(top, top) = top
				// add(top, bottom) = bottom
				return other == BOTTOM ? BOTTOM : this;
			}

			@Override
			Sign div(Sign other) {
				// div(top, +) = top
				// div(top, -) = top
				// div(top, 0-) = top
				// div(top, 0+) = top
				// div(top, top) = top

				// div(top, 0) = bottom
				// div(top, bottom) = bottom
				return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(top, +) = top
				// mul(top, -) = top
				// mul(top, 0+) = top
				// mul(top, 0-) = top
				// mul(top, top) = top

				// mul(top, 0) = 0

				// mul(top, bottom) = bottom
				return other == BOTTOM ? BOTTOM : other == ZERO ? ZERO : TOP;
			}

			@Override
			Sign mod(Sign other) {
				// mod(top, +) = top
				// mod(top, -) = top
				// mod(top, 0+-) = top
				// mod(top, top) = top

				// mod(top, 0) = bottom
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

	///*********************************** IT'S OKAY TO NOT BE OKAY ****************************************************
	private final Sign sign;

	public ExtSignDomain() {
		this(Sign.TOP);
	}

	private ExtSignDomain(Sign sign) {
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
		ExtSignDomain other = (ExtSignDomain) obj;
		return sign == other.sign;
	}

	@Override
	public ExtSignDomain top() {
		return new ExtSignDomain(Sign.TOP);
	}

	@Override
	public boolean isTop() {
		return sign == Sign.TOP;
	}

	@Override
	public ExtSignDomain bottom() {
		return new ExtSignDomain(Sign.BOTTOM);
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
	protected ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
		if(this.sign == null || other.sign == null)
			throw new SemanticException("Called object or other object is null.");

		switch (this.sign){
			case PLUS:
				if(other.sign == Sign.ZERO_PLUS)
					return other;
				if(other.sign == Sign.ZERO)
					return new ExtSignDomain(Sign.ZERO_PLUS);
				if(other.sign == Sign.BOTTOM)
					return this;
				break;
			case MINUS:
				if(other.sign == Sign.ZERO)
					return new ExtSignDomain(Sign.ZERO_MINUS);
				if(other.sign == Sign.ZERO_MINUS)
					return other;
				if(other.sign == Sign.BOTTOM)
					return this;
				break;
			case ZERO:
				if(other.sign == Sign.ZERO_MINUS || other.sign == Sign.ZERO_PLUS)
					return other;
				if(other.sign == Sign.MINUS)
					return new ExtSignDomain(Sign.ZERO_MINUS);
				if(other.sign == Sign.PLUS)
					return new ExtSignDomain(Sign.ZERO_PLUS);
				if(other.sign == Sign.BOTTOM)
					return this;
				break;
			case ZERO_PLUS:
				if(other.sign == Sign.PLUS || other.sign == Sign.ZERO)
					return this;
				if(other.sign == Sign.TOP)
					return other;
				break;
			case ZERO_MINUS:
				if(other.sign == Sign.MINUS || other.sign == Sign.ZERO)
					return this;
				if(other.sign == Sign.TOP)
					return other;
				break;
			case BOTTOM:
				if(other.sign == Sign.MINUS || other.sign == Sign.ZERO || other.sign == Sign.PLUS)
					return other;
		}
		return top();
	}

	@Override
	protected ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		if(this.sign == null || other.sign == null)
			throw new SemanticException("Called object or other object is null.");
		return lubAux(other);
	}

	@Override
	protected boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		if(this.sign == null || other.sign == null)
			throw new SemanticException("Called object or other object is null.");

		switch (this.sign){
			case PLUS:
			case ZERO:
			case MINUS:
				switch (other.sign){
					case PLUS:
					case MINUS:
					case ZERO:
					case ZERO_PLUS:
					case ZERO_MINUS:
					case TOP:
						return true;
					case BOTTOM:
						return false;
				}
			case ZERO_PLUS:
			case ZERO_MINUS:
				return other.sign == Sign.ZERO_MINUS || other.sign == Sign.ZERO_PLUS || other.sign == Sign.TOP;
			case TOP:
				return other.sign == Sign.TOP;
			case BOTTOM:
				return true;
		}
		return false;
	}

	@Override
	protected ExtSignDomain evalNullConstant(ProgramPoint pp) {
		return top();
	}

	@Override
	protected ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			int c = (int) constant.getValue();
			if (c == 0)
				return new ExtSignDomain(Sign.ZERO);
			else if (c > 0)
				return new ExtSignDomain(Sign.PLUS);
			else
				return new ExtSignDomain(Sign.MINUS);
		}
		return top();
	}

	@Override
	protected ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) {
		if (operator == UnaryOperator.NUMERIC_NEG) {
			return new ExtSignDomain(arg.sign.minus());
		}
		return top();
	}

	@Override
	protected ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right,
                                                 ProgramPoint pp) {
		switch (operator) {
			case NUMERIC_ADD:
				return new ExtSignDomain(left.sign.add(right.sign));
			case NUMERIC_DIV:
				return new ExtSignDomain(left.sign.div(right.sign));
			case NUMERIC_MOD:
				return new ExtSignDomain(left.sign.mod(right.sign));
			case NUMERIC_MUL:
				return new ExtSignDomain(left.sign.mul(right.sign));
			case NUMERIC_SUB:
				return new ExtSignDomain(left.sign.sub(right.sign));
			default:
				return top();
		}
	}

	@Override
	protected ExtSignDomain evalTernaryExpression(TernaryOperator operator, ExtSignDomain left, ExtSignDomain middle,
                                                  ExtSignDomain right, ProgramPoint pp) {
		return top();
	}

	///******************************* NOT TO DO ***********************************************************
	@Override
	protected Satisfiability satisfiesAbstractValue(ExtSignDomain value, ProgramPoint pp) {
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
	protected Satisfiability satisfiesUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Satisfiability satisfiesBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right,
                                                       ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	@Override
	protected Satisfiability satisfiesTernaryExpression(TernaryOperator operator, ExtSignDomain left, ExtSignDomain middle,
                                                        ExtSignDomain right, ProgramPoint pp) {
		return Satisfiability.UNKNOWN;
	}

	///***************************************************************************************************************
}
