package it.unive.lisa.analysis.impl.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.impl.string.utils.Index;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.*;

public class Bricks extends BaseNonRelationalValueDomain<Bricks> {

    /**
     * Parameter for limiting the length of the number of bricks in a Bricks object, used in widening
     */
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

    /**
     * Pads the list of 'this' according to 'other'.
     * If 'this' contains more bricks than 'other', than returns the same list.
     */
    private List<Brick> pad(Bricks other) {
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

    /**
     * Checks whether a bricks contains only a char
     */
    private boolean isChar() {
        return bricks.size() == 1 && bricks.get(0).strings.size() == 1 && ((String) bricks.get(0).strings.toArray()[0]).length() == 1;
    }

    private Character getChar() {
        return isChar() ? ((String) bricks.get(0).strings.toArray()[0]).charAt(0) : null;
    }

    @Override
    protected SemanticDomain.Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Bricks left, Bricks right, ProgramPoint pp) {

        if (operator == BinaryOperator.STRING_CONTAINS) {
            // satisfied for sure
            boolean satisfied;

            // may or may be not satisfied
            boolean unknown = false;

            if (right.isChar() && right.getChar() != null) {
                String c = right.getChar().toString();

                for (Brick brick : left.bricks) {
                    if (brick.min.equals(0)) {
                        for (String string : brick.strings) {
                            if (string.contains(c)) {
                                // unknown because brick.min == 0
                                unknown = true;
                                break;
                            }
                        }
                    }
                    else {
                        satisfied = true;
                        for (String string : brick.strings) {
                            if (!string.contains(c)) {
                                // not always satisfied because not all strings contain the character
                                satisfied = false;
                                break;
                            }
                            // unknown if at least a string contains the character
                            unknown = true;
                        }

                        if (satisfied)
                            return SemanticDomain.Satisfiability.SATISFIED;
                    }
                }

                return unknown ? SemanticDomain.Satisfiability.UNKNOWN : SemanticDomain.Satisfiability.NOT_SATISFIED;
            }

            return SemanticDomain.Satisfiability.UNKNOWN;
        }

        return super.satisfiesBinaryExpression(operator, left, right, pp);
    }

    @Override
    protected Bricks evalNullConstant(ProgramPoint pp) {
        return new Bricks().top();
    }

    @Override
    protected Bricks evalNonNullConstant(Constant constant, ProgramPoint pp) {

        // we need integer for Substrings
        if (constant.getValue() instanceof Integer) {
            return new Bricks((constant.getValue()).toString());
        }

        if (constant.getValue() instanceof String) {
            String s = (String) constant.getValue();
            s = s.substring(1, s.length() - 1);
            return new Bricks(s);
        }
        return top();
    }

    @Override
    protected Bricks evalBinaryExpression(BinaryOperator operator, Bricks left, Bricks right, ProgramPoint pp) {
        if (operator == BinaryOperator.STRING_CONCAT)
            return left.add(right);

        return new Bricks().top();
    }

    @Override
    protected Bricks evalTernaryExpression(TernaryOperator operator, Bricks left, Bricks middle, Bricks right, ProgramPoint pp) {
        if (operator == TernaryOperator.STRING_SUBSTRING) {
            List<Brick> brickList = Brick.normalizeList(left.bricks);
            Brick brick = brickList.get(0);
            int begin = Integer.parseInt((String) middle.bricks.get(0).strings.toArray()[0]);
            int end = Integer.parseInt((String) right.bricks.get(0).strings.toArray()[0]);

            // check if the first brick contains the substring
            if (brick.min.equals(1) && brick.max.equals(1)) {

                // every string in the brick must be valid
                for (String s: brick.strings) {
                    if (s.length() < end)
                        return new Bricks().top();
                }

                Set<String> substrings = new HashSet<>();
                for (String string : brick.strings) {
                    substrings.add(string.substring(begin, end));
                }

                // returns same list of bricks, but the first brick is modified according to the substring
                List<Brick> newList = new LinkedList<>(brickList);
                newList.set(0, new Brick(substrings, new Index(1), new Index(1)));

                return new Bricks(newList);
            }
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
