package it.unive.lisa.analysis.nonrelational.value.impl;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.TernaryOperator;
import it.unive.lisa.symbolic.value.UnaryOperator;

public class BricksDomain extends BaseNonRelationalValueDomain<BricksDomain> {

    private final Brick brick;

    public BricksDomain(Brick brick) {
        this.brick = brick;
    }

    public BricksDomain(){
        brick = new Brick();
    }

    @Override
    protected BricksDomain lubAux(BricksDomain other) throws SemanticException {
        return null;
    }

    @Override
    protected BricksDomain wideningAux(BricksDomain other) throws SemanticException {
        return null;
    }

    @Override
    protected boolean lessOrEqualAux(BricksDomain other) throws SemanticException {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public BricksDomain top() {
        return null;
    }

    @Override
    public BricksDomain bottom() {
        return null;
    }

    @Override
    public boolean isTop() {
        return super.isTop();
    }

    @Override
    public boolean isBottom() {
        return super.isBottom();
    }

    @Override
    public String representation() {
        return null;
    }

    @Override
    protected BricksDomain evalNullConstant(ProgramPoint pp) {
        return null;
    }

    @Override
    protected BricksDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        return null;
    }

    @Override
    protected BricksDomain evalUnaryExpression(UnaryOperator operator, BricksDomain arg, ProgramPoint pp) {
        return null;
    }

    @Override
    protected BricksDomain evalBinaryExpression(BinaryOperator operator, BricksDomain left, BricksDomain right, ProgramPoint pp) {
        return null;
    }

    @Override
    protected BricksDomain evalTernaryExpression(TernaryOperator operator, BricksDomain left, BricksDomain middle, BricksDomain right, ProgramPoint pp) {
        return null;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesAbstractValue(BricksDomain value, ProgramPoint pp) {
        return null;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesNullConstant(ProgramPoint pp) {
        return null;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesNonNullConstant(Constant constant, ProgramPoint pp) {
        return null;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesUnaryExpression(UnaryOperator operator, BricksDomain arg, ProgramPoint pp) {
        return null;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, BricksDomain left, BricksDomain right, ProgramPoint pp) {
        return null;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesTernaryExpression(TernaryOperator operator, BricksDomain left, BricksDomain middle, BricksDomain right, ProgramPoint pp) {
        return null;
    }
}
