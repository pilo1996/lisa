package it.unive.lisa.analysis.impl.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryOperator;

import javax.naming.BinaryRefAddr;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Bricks extends BaseNonRelationalValueDomain<Bricks> {

    List<Brick> bricks;

    public Bricks() {
        new LinkedList<>();
    }

    Bricks(List<Brick> bricks) {
        if (bricks == null)
            throw new IllegalArgumentException("The list of bricks must not be null");

        this.bricks = bricks;
    }

    Bricks(Brick brick) {
        this.bricks = new LinkedList<>();
        bricks.add(brick);
    }

    Bricks(String string) {
        this(new Brick(string));
    }

    @Override
    protected Bricks lubAux(Bricks other) throws SemanticException {
        List<Brick> l1 = this.pad(other);
        List<Brick> l2 = other.pad(this);

        List<Brick> newList = new LinkedList<>();

        for (int i = 0; i < l1.size(); i++) {
            newList.add(l1.get(i).lub(l2.get(i)));
        }

        return new Bricks(newList);
    }

    @Override
    protected Bricks wideningAux(Bricks other) throws SemanticException {
        return null;
    }

    @Override
    protected boolean lessOrEqualAux(Bricks other) throws SemanticException {
        List<Brick> l1 = this.pad(other);
        List<Brick> l2 = other.pad(this);

        for (int i = 0; i < l1.size(); i++) {
            if (!(l1.get(i).lessOrEqual(l2.get(i))))
                return false;
        }

        return true;
    }

    @Override
    public Bricks top() {
        return new Bricks(new Brick().top());
    }

    @Override
    public Bricks bottom() {
        return new Bricks();
    }

    @Override
    public boolean isTop() {
        return this.bricks.size() == 1 && this.bricks.get(0).isTop();
    }

    @Override
    public boolean isBottom() {
        if (this.bricks.isEmpty())
            return true;

        for (Brick brick: bricks) {
            if (brick.isBottom())
                return true;
        }

        return false;
    }

    List<Brick> pad(Bricks other) {
        List<Brick> l1 = this.bricks;
        List<Brick> l2 = other.bricks;
        int n1 = l1.size();
        int n2 = l2.size();
        int n = n2 - n1;

        if (n <= 0)
            return this.bricks;

        List<Brick> newList = new LinkedList<>();
        int emptyBricksAdded = 0;

        for (int i = 0; i < n2 - 1; i++) {
            if (emptyBricksAdded >= n) {
                newList.add(l1.remove(0));
            }
            else if (l1.isEmpty() || l1.get(0) != l2.get(i)) {
                newList.add(new Brick());
                emptyBricksAdded++;
            }
            else {
                newList.add(l1.remove(0));
            }
        }

        return newList;
    }

    public Bricks add(Bricks other) {
        List<Brick> newList = new LinkedList<>();
        newList.addAll(this.bricks);
        newList.addAll(other.bricks);
        return new Bricks(newList);
    }

    @Override
    protected Bricks evalNullConstant(ProgramPoint pp) {
        return new Bricks().top();
    }

    @Override
    protected Bricks evalNonNullConstant(Constant constant, ProgramPoint pp) {
        return new Bricks(constant.getValue().toString());
    }

    @Override
    protected Bricks evalUnaryExpression(UnaryOperator operator, Bricks arg, ProgramPoint pp) {
        return super.evalUnaryExpression(operator, arg, pp);
    }

    @Override
    protected Bricks evalBinaryExpression(BinaryOperator operator, Bricks left, Bricks right, ProgramPoint pp) {
        if (operator == BinaryOperator.STRING_CONCAT)
            return left.add(right);

        return new Bricks().top();
    }

    @Override
    public String representation() {
        if (isTop())
            return Lattice.TOP_STRING;

        if (isBottom())
            return Lattice.BOTTOM_STRING;

        StringBuilder rep = new StringBuilder("br{");

        for (Brick brick: bricks) {
            rep.append(brick.toString()).append(",");
        }

        rep.append("}");

        return rep.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bricks bricks1 = (Bricks) o;
        return Objects.equals(bricks, bricks1.bricks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bricks);
    }
}
