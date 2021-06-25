package it.unive.lisa.analysis.impl.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryOperator;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.TernaryOperator;
import it.unive.lisa.symbolic.value.UnaryOperator;

import java.util.*;

public class Bricks extends BaseNonRelationalValueDomain<Bricks> {

    // parameter for limiting the length of the number of bricks in a Bricks object, used in widening
    static final int kL = 10;

    List<Brick> bricks;

    public Bricks() {
        bricks = new LinkedList<>();
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

        return new Bricks(Brick.normalizeList(newList));
    }

    @Override
    protected Bricks wideningAux(Bricks other) throws SemanticException {
        if ((!this.lessOrEqual(other) && !other.lessOrEqual(this)) ||
                (this.bricks.size() > kL || other.bricks.size() > kL))
            return top();

        // w(L1, L2)
        List<Brick> l1 = this.pad(other);
        List<Brick> l2 = other.pad(this);

        int n = l1.size();
        List<Brick> l3 = new LinkedList<>();

        for(int i = 0; i < n; i++) {
            Brick b1 = l1.get(i);
            Brick b2 = l2.get(i);

            l3.add(b1.widening(b2));
        }

        return new Bricks(Brick.normalizeList(l3));
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
        LinkedList<Brick> l1 = new LinkedList<>(this.bricks);
        LinkedList<Brick> l2 = new LinkedList<>(other.bricks);
        int n1 = l1.size();
        int n2 = l2.size();
        int n = n2 - n1;

        if (n <= 0)
            return this.bricks;

        List<Brick> newList = new LinkedList<>();
        int emptyBricksAdded = 0;

        for (int i = 0; i < n2; i++) {
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
        return new Bricks(Brick.normalizeList(newList));
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Bricks left, Bricks right, ProgramPoint pp) {
        switch(operator){
            case STRING_CONCAT:
                return SemanticDomain.Satisfiability.SATISFIED;
            case STRING_CONTAINS:
                if(right.bricks.size() == 1){
                    Brick c = right.bricks.get(0);
                    if(c.strings.size() == 1)
                        for (String s : c.strings) {
                            if (s.length() == 1)
                                return SemanticDomain.Satisfiability.SATISFIED;
                        }
                }
                break;
        }
        return SemanticDomain.Satisfiability.NOT_SATISFIED;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesTernaryExpression(TernaryOperator operator, Bricks left, Bricks middle, Bricks right, ProgramPoint pp) {
        if (operator == TernaryOperator.STRING_SUBSTRING) {
            if(!(left.bricks.size() == 1 || left.bricks.get(0).strings.size() == 1) || !(right.bricks.size() == 1 || right.bricks.get(0).strings.size() == 1))
                return SemanticDomain.Satisfiability.NOT_SATISFIED;
            return SemanticDomain.Satisfiability.SATISFIED;
        }
        return super.satisfiesTernaryExpression(operator, left, middle, right, pp);
    }

    @Override
    protected Bricks evalNullConstant(ProgramPoint pp) {
        return new Bricks().top();
    }

    @Override
    protected Bricks evalNonNullConstant(Constant constant, ProgramPoint pp) {
        String s = constant.getValue().toString();
        if (constant.getValue() instanceof String) {
            s = s.substring(1, s.length() - 1);
        }
        return new Bricks(s);
    }

    @Override
    protected Bricks evalUnaryExpression(UnaryOperator operator, Bricks arg, ProgramPoint pp) {
        return super.evalUnaryExpression(operator, arg, pp);
    }

    @Override
    protected Bricks evalBinaryExpression(BinaryOperator operator, Bricks left, Bricks right, ProgramPoint pp) {
        if (operator == BinaryOperator.STRING_CONCAT)
            return left.add(right);
        if(operator == BinaryOperator.STRING_CONTAINS){
            CharSequence c = (CharSequence) right.bricks.get(0).strings.toArray()[0];
            boolean check = false;
            for (Brick bl : left.bricks) {
                if(bl.min >= 1 && (bl.isMaxInfinite() || bl.max <= bl.min)){
                    for (String s : bl.strings)
                        check = s.contains(c);
                    if(check)
                        return new Bricks("TRUE");
                }
            }
            return new Bricks("FALSE");
        }
        return new Bricks().top();
    }

    @Override
    protected Bricks evalTernaryExpression(TernaryOperator operator, Bricks left, Bricks middle, Bricks right, ProgramPoint pp) {
        if (operator == TernaryOperator.STRING_SUBSTRING) {
            List<Brick> start = Brick.normalizeList(middle.bricks);
            if((start.get(0).isMaxInfinite() || start.get(0).max > 0) && start.get(0).min == 0)
                return new Bricks().top();
            int e = (int) right.bricks.get(0).strings.toArray()[0];
            for (String s : start.get(0).strings){
                if(s.length() < e)
                    return new Bricks().top();
            }
            //We can pack all possible substrings in a new abstract value
            List<Brick> allSubstrings = new LinkedList<>();
            for (Brick b : start) {
                for(String s : b.strings){
                    for (int i = 0; i < s.length(); i++) {
                        for (int j = i+1; j < s.length(); j++) {
                            allSubstrings.add(new Brick(s.substring(i, j)));
                        }
                    }
                }
            }
            return new Bricks(allSubstrings);
        }
        return new Bricks().top();
    }

    @Override
    public String representation() {
        if (isTop())
            return Lattice.TOP_STRING;

        if (isBottom())
            return Lattice.BOTTOM_STRING;

        StringBuilder rep = new StringBuilder("{");

        for (int i = 0; i < bricks.size(); i++) {
            rep.append(bricks.get(i).toString());
            if (i < bricks.size() - 1)
                rep.append(",");
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
