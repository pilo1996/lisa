package it.unive.lisa.analysis.impl.string.utils;

public class Index {
    public static final Index INFINITY = new Index();

    /**
     * internal integer value, null stands for INFINITY
    */
    private final Integer _int;

    public Index() {
        _int = null;
    }

    public Index(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Index must be positive");
        }
        this._int = value;
    }

    public Integer getInteger() {
        return _int;
    }

    /**
     * Sum of two indices.
     * Finite + Infinite = Infinite
     */
    public Index plus(Index other) { return this.plus(other._int); }
    public Index plus(Integer other) {
        return this._int == null || other == null ? INFINITY : new Index(this._int + other);
    }

    /**
     * Difference between two indices.
     * You can't subtract infinite in the positive integer space
     */
    public Index minus(Index other) { return this.minus(other._int); }
    public Index minus(Integer other) {
        if (this.lt(other))
            throw new ArithmeticException("First value must be greater than the second value.");

        return this._int == null ? INFINITY : new Index(this._int - other);
    }

    public boolean equals(Object o) {
        if (o instanceof Index) return this.equals((Index) o);
        if (o instanceof Integer) return this.equals((Integer) o);

        return false;
    }

    public boolean equals(Index o) { return this.equals(o._int); }
    public boolean equals(Integer o) { return  (this._int == null && o == null) || (this._int != null && _int.equals(o)); }

    /**
     * this < other
     * Comparing two infinities is always false
     */
    public boolean lt(Index other) { return this.lt(other._int); }
    public boolean lt(Integer other) { return other == null || (this._int != null && _int < other); }

    /**
     * this <= other
     * Comparing two infinities is always false
     */
    public boolean le(Index other) { return this.le(other._int); }
    public boolean le(Integer other) { return other == null || (this._int != null && _int <= other); }

    /**
     * this > other
     * Comparing two infinities is always false
     */
    public boolean gt(Index other) { return this.gt(other._int); }
    public boolean gt(Integer other) { return other != null && (this._int == null || _int > other); }

    /**
     * this >= other
     * Comparing two infinities is always false
     */
    public boolean ge(Index other) { return this.ge(other._int); }
    public boolean ge(Integer other) { return other != null && (this._int == null || _int >= other); }

    public String toString() {
        return _int == null ? "INF" : _int.toString();
    }

    /**
     * Returns the highest index
     */
    public static Index max(Index v1, Index v2) {
        return v1.gt(v2) ? v1 : v2;
    }

    /**
     * Returns the lowest index
     */
    public static Index min(Index v1, Index v2) {
        return v1.lt(v2) ? v1 : v2;
    }
}
