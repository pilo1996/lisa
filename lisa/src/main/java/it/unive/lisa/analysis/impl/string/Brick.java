package it.unive.lisa.analysis.impl.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.impl.string.utils.Index;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;

import java.util.*;

public class Brick extends BaseNonRelationalValueDomain<Brick> {
    private static final Brick EMPTY = new Brick();

    /**
     * Parameter for limiting the min-max range in a brick, used in widening
     */
    private static final int kI = 10;

    /**
     * parameter for limiting the number of strings contained in a brick, used in widening
     */
    private static final int kS = 20;

    Set<String> strings;
    Index min;
    Index max;

    // default Brick is the EMPTY brick
    Brick() {
        this.strings = new HashSet<>();
        this.min = new Index(0);
        this.max = new Index(0);
    }

    /**
     * Constructor of a brick
     * @param strings a list of strings, null means the whole alphabet
     * @param min >= 0, not null
     * @param max >= 0, can be null (infinity)
     */
    Brick(Set<String> strings, Index min, Index max) {
        if (min.equals(Index.INFINITY))
            throw new IllegalArgumentException("'min' cannot be infinite");

        this.strings = strings;
        this.min = min;
        this.max = max;
    }

    // these just make it easier to construct a Brick without the need to use Index for non-infinite integer numbers
    Brick(Set<String> strings, int min, int max) { this(strings, new Index(min), new Index(max)); }
    Brick(Set<String> strings, int min, Index max) { this(strings, new Index(min), max); }

    // Faster constructor for a brick with indices 1,1
    Brick(String string) {
        this.strings = new HashSet<>();
        strings.add(string);
        this.min = new Index(1);
        this.max = new Index(1);
    }

    @Override
    protected Brick lubAux(Brick other) throws SemanticException {
        Set<String> newStrings = null;

        if (this.strings != null && other.strings != null) {
            newStrings = new HashSet<>(this.strings);
            newStrings.addAll(other.strings);
        }

        Index newMin = Index.min(this.min, other.min);
        Index newMax = Index.max(this.max, other.max);

        return new Brick(newStrings, newMin, newMax);
    }

    @Override
    public Brick glbAux(Brick other) {
        Set<String> newStrings;

        if (this.strings == null)
            newStrings = other.strings;
        else if (other.strings == null)
            newStrings = this.strings;
        else {
            newStrings = new HashSet<>(this.strings);
            newStrings.addAll(other.strings);
        }

        Index newMin = Index.max(this.min, other.min);
        Index newMax = Index.min(this.max, other.max);

        return new Brick(newStrings, newMin, newMax);
    }

    @Override
    protected Brick wideningAux(Brick other) throws SemanticException {
        Set<String> s = null;

        if (this.strings != null && other.strings != null) {
            s = new HashSet<>();
            s.addAll(this.strings);
            s.addAll(other.strings);
        }

        if (s == null || s.size() > kS || this.isTop() || other.isTop())
            return top();

        Index min = Index.min(this.min, other.min);
        Index max = Index.max(this.max, other.max);

        if (max == Index.INFINITY || max.minus(min).gt(kI))
            return new Brick(s, 0, Index.INFINITY);
        else
            return new Brick(s, min, max);
    }

    @Override
    protected boolean lessOrEqualAux(Brick other) throws SemanticException {
        if (this.strings == null && other.strings != null)
            return false;

        if (this.strings != null && other.strings != null) {
            // Checks whether this.strings is a subset of other.strings
            for (String s: this.strings) {
                if (!other.strings.contains(s)) {
                    return false;
                }
            }
        }

        // Checks min and max parameters
        return this.min.ge(other.min) && this.max.le(other.max);
    }

    @Override
    public Brick top() {
        return new Brick(null, 0, Index.INFINITY);
    }

    @Override
    public Brick bottom() {
        return new Brick(new HashSet<>(), 1, 0);
    }

    @Override
    public boolean isTop() {
        return strings == null && min.equals(0) && max == Index.INFINITY;
    }

    @Override
    public boolean isBottom() {
        return (strings != null && strings.isEmpty() && !(min.equals(0) && max.equals(0)))
                || max.lt(min)
                || (strings != null && !strings.isEmpty() && min.equals(0) && max.equals(0));
    }

    @Override
    public String representation() {
        if (isTop())
            return Lattice.TOP_STRING;

        if (isBottom())
            return Lattice.BOTTOM_STRING;

        return String.format("%s(%s,%s)",
                strings == null ? "K" : strings.toString(), min, max);
    }

    /**
     * Combines consecutive bricks with min=1 and max=1
     */
    private static List<Brick> applyRule2(List<Brick> bricks) {
        if (bricks.size() == 1) {
            return bricks;
        }

        List<Brick> finalBricks = new LinkedList<>();

        for(int i = 1; i < bricks.size(); i++) {
            Brick b1 = bricks.get(i - 1);
            Brick b2 = bricks.get(i);

            if (b1.min.equals(1) && b1.max.equals(1) && b2.min.equals(1) && b2.max.equals(1)) {
                Set<String> newStrings = new HashSet<>();

                if (b1.strings == null || b2.strings == null) {
                    finalBricks.add(b1);
                    finalBricks.add(b2);
                }
                else {
                    // combines the strings of the two bricks
                    for (String s1: b1.strings) {
                        for (String s2: b2.strings) {
                            newStrings.add(s1.concat(s2));
                        }
                    }

                    finalBricks.add(new Brick(newStrings, 1, 1));
                }
            }
        }

        return finalBricks;
    }

    /**
     * Transforms a min=max brick in 1,1 brick
     */
    private Brick applyRule3() {

        if (strings == null)
            return new Brick(null, new Index(1), new Index(1));

        Set<String> newStrings = new HashSet<>(strings);

        if (min.equals(max)) {
            // combine strings min times
            for (int i = 0; i < min.getInteger() - 1; i++) {
                Set<String> temp = new HashSet<>();

                for (String s1: newStrings) {
                    for (String s2: strings) {
                        temp.add(s1.concat(s2));
                    }
                }

                newStrings.clear();
                newStrings.addAll(temp);
            }

            return new Brick(newStrings, new Index(1), new Index(1));
        }
        else {
            return this;
        }
    }

    /**
     * Combines consecutive bricks with the same set of strings
     */
    private static List<Brick> applyRule4(List<Brick> bricks) {
        if (bricks.size() == 1) {
            return bricks;
        }

        List<Brick> finalBricks = new LinkedList<>();

        for(int i = 0; i < bricks.size() - 1; i++) {
            Brick b1 = bricks.get(i);
            Brick b2 = bricks.get(i + 1);

            if ((b1.strings == null && b2.strings == null) || (b1.strings != null && b2.strings != null && b1.strings.equals(b2.strings))) {
                Index newMin = b1.min.plus(b2.min);
                Index newMax = b1.max.plus(b2.max);

                finalBricks.add(new Brick(b1.strings, newMin, newMax));
                i++;
            }
            else {
                finalBricks.add(b1);
                if (i == finalBricks.size() - 2) {
                    finalBricks.add(b2);
                }
            }
        }

        return finalBricks;
    }

    /**
     * Transforms a min, max brick in 1,1 brick and a 0,max-min brick
     */
    private List<Brick> applyRule5() {
        List<Brick> newList = new LinkedList<>();

        if (!isTop() && !isBottom() && min.ge(1) && max.ge(min)) {
            newList.add((new Brick(strings, min, min)).applyRule3());
            newList.add(new Brick(strings, 0, max.minus(min)));
        }
        else {
            newList.add(this);
        }

        return newList;
    }

    /**
     * Normalizes a list of bricks
     *
     * @param bricks an ordered list of bricks
     * @return the normalized list, following the 5 rules stated in the paper
     *
     */
    public static List<Brick> normalizeList(List<Brick> bricks) {
        int startSize;

        List<Brick> finalBricks = bricks;
        List<Brick> tempBricks = new LinkedList<>();

        // rule 1, removes empty bricks
        finalBricks.removeIf(EMPTY::equals);

        // repeat until the list of bricks stops shrinking (fixpoint)
        do {
            startSize = finalBricks.size();

            // rule 2
            finalBricks = applyRule2(finalBricks);

            // rule 3
            for (Brick b: finalBricks) {
                tempBricks.add(b.applyRule3());
            }
            finalBricks = List.copyOf(tempBricks);
            tempBricks = new LinkedList<>();

            // rule 4
            finalBricks = applyRule4(finalBricks);

            // rule 5
            for (Brick b: finalBricks) {
                tempBricks.addAll(b.applyRule5());
            }
            finalBricks = List.copyOf(tempBricks);
            tempBricks = new LinkedList<>();

        } while (finalBricks.size() < startSize);

        return new LinkedList<>(finalBricks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brick brick = (Brick) o;
        return Objects.equals(strings, brick.strings) && Objects.equals(min, brick.min) && Objects.equals(max, brick.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strings, min, max);
    }
}
