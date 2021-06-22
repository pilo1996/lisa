package it.unive.lisa.analysis.impl.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Brick extends BaseNonRelationalValueDomain<Brick> {

    Set<String> strings;
    Integer min;
    Integer max;

    Brick() {
        this.strings = new HashSet<>();
        this.min = 0;
        this.max = 0;
    }

    Brick(Set<String> strings, Integer min, Integer max) {
        if (min == null)
            throw new IllegalArgumentException("'min' cannot be infinite");

        this.strings = strings;
        this.min = 1;
        this.max = 1;
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
        throw new NotImplementedException();
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

        return String.format("b%s<%s,%s>",
                strings == null ? "K" : strings.toString(), min, isMaxInfinite() ? "INF" : max);
    }

    Boolean isMaxInfinite() {
        return max == null;
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
