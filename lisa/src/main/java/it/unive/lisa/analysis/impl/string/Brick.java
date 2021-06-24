package it.unive.lisa.analysis.impl.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

public class Brick extends BaseNonRelationalValueDomain<Brick> {
    // parameter for limiting the min-max range in a brick, used in widening
    private static final int kI = 10;

    // parameter for limiting the length of each string of a brick, used in widening
    private static final int kS = 200;

    Set<String> strings;
    Integer min;
    Integer max;

    // default Brick is the empty brick
    Brick() {
        this.strings = new HashSet<>();
        this.min = 0;
        this.max = 0;
    }

    Brick(Set<String> strings, Integer min, Integer max) {
        if (min == null)
            throw new IllegalArgumentException("'min' cannot be infinite");

        this.strings = strings;
        this.min = min;
        this.max = max;
    }

    Brick(String string) {
        this.strings = new HashSet<>();
        strings.add(string);
        this.min = 1;
        this.max = 1;
    }

    @Override
    protected Brick lubAux(Brick other) throws SemanticException {
        Set<String> newStrings = null;

        if (this.strings != null && other.strings != null) {
            newStrings = new HashSet<>(this.strings);
            newStrings.addAll(other.strings);
        }

        Integer newMin = this.min <= other.min ? this.min : other.min;
        Integer newMax = (this.isMaxInfinite() || other.isMaxInfinite()) ? null : this.max >= other.max ? this.max : other.max;

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

        Integer newMin = this.min >= other.min ? this.min : other.min;
        Integer newMax = this.isMaxInfinite() ? other.max : other.isMaxInfinite() || this.max < other.max ? this.max : other.max;

        return new Brick(newStrings, newMin, newMax);
    }

    @Override
    protected Brick wideningAux(Brick other) throws SemanticException {
        Set<String> s = new HashSet<>(this.strings);
        s.addAll(other.strings);

        if (s.size() > kS || this.isTop() || other.isTop())
            return top();

        Integer min = this.min < other.min ? this.min : other.min;
        Integer max;
        if (this.isMaxInfinite() || other.isMaxInfinite()) max = null;
        else if (this.max > other.max) max = this.max;
        else max = other.max;

        if (max == null || max - min > kI)
            return new Brick(s, 0, null);
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
        return this.min >= other.min && (other.isMaxInfinite() || (!this.isMaxInfinite() && this.max <= other.max));
    }

    @Override
    public Brick top() {
        return new Brick(null, 0, null);
    }

    @Override
    public Brick bottom() {
        return new Brick(new HashSet<>(), 1, 0);
    }

    @Override
    public boolean isTop() {
        return strings == null && min == 0 && isMaxInfinite();
    }

    @Override
    public boolean isBottom() {
        return (strings != null && strings.isEmpty() && !(min == 0 && !isMaxInfinite() && max == 0))
                || (!isMaxInfinite() && max < min)
                || (strings != null && !strings.isEmpty() && min == 0 && !isMaxInfinite() && max == 0);
    }

    @Override
    public String representation() {
        if (isTop())
            return Lattice.TOP_STRING;

        if (isBottom())
            return Lattice.BOTTOM_STRING;

        return String.format("%s(%s,%s)",
                strings == null ? "K" : strings.toString(), min, isMaxInfinite() ? "INF" : max);
    }

    Boolean isMaxInfinite() {
        return max == null;
    }

    Brick applyRule3() {
        Set<String> newStrings = new HashSet<>(strings);

        if (!isMaxInfinite() && min.equals(max)) {
            for (int i = 0; i < min - 1; i++) {
                Set<String> temp = new HashSet<>();

                for (String s1: newStrings) {
                    for (String s2: strings) {
                        temp.add(s1.concat(s2));
                    }
                }

                newStrings.clear();
                newStrings.addAll(temp);
            }

            return new Brick(newStrings, 1, 1);
        }
        else {
            return this;
        }
    }

    List<Brick> applyRule5() {
        List<Brick> newList = new LinkedList<>();

        if (!isTop() && !isBottom() && min >= 1 && (isMaxInfinite() || max > min)) {
            newList.add((new Brick(strings, min, min)).applyRule3());
            newList.add(new Brick(strings, 0, max - min));
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
     * @return normalized list following the 5 rules stated in the paper
     *
     * TODO does this method belong to Brick or should it be placed in Bricks?
     */
    public static List<Brick> normalizeList(List<Brick> bricks) {
        int startSize;

        List<Brick> finalBricks = bricks;

        // rule 1
        finalBricks.removeIf((new Brick())::equals);

        do {
            startSize = finalBricks.size();

            List<Brick> newBricks1 = new LinkedList<>();
            // rule 3 (finalBricks --> newBricks1)
            for (Brick b: finalBricks) {
                newBricks1.add(b.applyRule3());
            }

            List<Brick> newBricks2 = new LinkedList<>();
            // rule 5 (newBricks1 --> newBricks2)
            for (Brick b: newBricks1) {
                newBricks2.addAll(b.applyRule5());
            }

            List<Brick> newBricks3 = new LinkedList<>();
            // rule 4 (newBricks2 --> newBricks3)
            if (newBricks2.size() == 1) {
                newBricks3.addAll(newBricks2);
            }
            for(int i = 0; i < newBricks2.size() - 1; i++) {
                Brick b1 = newBricks2.get(i);
                Brick b2 = newBricks2.get(i + 1);

                if ((b1.strings == null && b2.strings == null) || b1.strings.equals(b2.strings)) {
                    Integer newMin = b1.min + b2.min;
                    Integer newMax = (b1.isMaxInfinite() || b2.isMaxInfinite()) ? null : b1.max + b2.max;

                    newBricks3.add(new Brick(b1.strings, newMin, newMax));
                    i++;
                }
                else {
                    newBricks3.add(b1);
                    if (i == newBricks2.size() - 2) {
                        newBricks3.add(b2);
                    }
                }
            }

            List<Brick> newBricks4 = new LinkedList<>();
            if (newBricks3.size() == 1) {
                newBricks4.addAll(newBricks3);
            }
            // rule 2
            for(int i = 1; i < newBricks3.size(); i++) {
                Brick b1 = newBricks3.get(i - 1);
                Brick b2 = newBricks3.get(i);

                if (!b1.isMaxInfinite() && !b2.isMaxInfinite() && b1.min == 1 && b1.max == 1 && b2.min == 1 && b2.max == 1) {
                    Set<String> newStrings = new HashSet<>();

                    if (b1.strings == null || b2.strings == null) {
                        newBricks4.add(b1);
                        newBricks4.add(b2);
                    }
                    else {
                        for (String s1: b1.strings) {
                            for (String s2: b2.strings) {
                                newStrings.add(s1.concat(s2));
                            }
                        }

                        newBricks4.add(new Brick(newStrings, 1, 1));
                    }
                }
            }

            finalBricks = newBricks4;
        } while (finalBricks.size() < startSize);

        return finalBricks;
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
