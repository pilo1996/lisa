package it.unive.lisa.analysis.impl.string.utils;

public class Index {
    public static final Index INFINITY = new Index();

    private Integer _int;

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

    public Index plus(Index other) {
        if (this.equals(INFINITY) || other.equals(INFINITY)) {
            return Index.INFINITY;
        }
        return new Index(this._int + other._int);
    }

    public Index minus(Index other) {
        if (other.equals(INFINITY))
            throw new ArithmeticException("You can't subtract INFINITY!");

        return this.minus(other._int);
    }

    public Index plus(Integer other) {
        return this.equals(INFINITY) ? INFINITY : new Index(this._int + other);
    }

    public Index minus(Integer other) {
        if (this.lt(other))
            throw new ArithmeticException("First value must be greater than the second value.");

        return this.equals(INFINITY) ? INFINITY : new Index(this._int - other);
    }

    public boolean equals(Object o) {
        if (o instanceof Index) return this.equals((Index) o);
        if (o instanceof Integer) return this.equals((Integer) o);

        return false;
    }

    public boolean equals(Index o) {
        return ((this == (INFINITY) && o == (INFINITY)) || (this != (INFINITY) && _int.equals(o._int)));
    }
    public boolean lt(Index other) { return other.equals(INFINITY) || this.lt(other._int); }
    public boolean le(Index other) { return other.equals(INFINITY) || this.le(other._int); }
    public boolean gt(Index other) { return !other.equals(INFINITY) && this.gt(other._int); }
    public boolean ge(Index other) { return !other.equals(INFINITY) && this.ge(other._int); }

    public boolean equals(Integer o) { return this.equals(INFINITY) || _int.equals(o); }
    public boolean lt(Integer other) { return !this.equals(INFINITY) && _int < other; }
    public boolean le(Integer other) { return !this.equals(INFINITY) && _int <= other; }
    public boolean gt(Integer other) { return this.equals(INFINITY) || _int > other; }
    public boolean ge(Integer other) { return this.equals(INFINITY) || _int >= other; }

    public String toString() {
        return _int == null ? "INF" : _int.toString();
    }

    public static Index max(Index v1, Index v2) {
        return v1.gt(v2) ? v1 : v2;
    }

    public static Index min(Index v1, Index v2) {
        return v1.lt(v2) ? v1 : v2;
    }
}
