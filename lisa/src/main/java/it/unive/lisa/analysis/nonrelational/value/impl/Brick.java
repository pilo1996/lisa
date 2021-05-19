package it.unive.lisa.analysis.nonrelational.value.impl;

import java.util.ArrayList;
import java.util.List;

public class Brick {

    private final int min, max;
    private final List<String> strings;

    public Brick(int min, int max, List<String> strings){
        this.min = min;
        this.max = max;
        this.strings = strings;
    }

    public Brick(int min, int max, String string){
        this.min = min;
        this.max = max;
        this.strings = new ArrayList<>();
        strings.add(string);
    }

    public Brick(){
        this.min = 0;
        this.max = 0;
        strings = new ArrayList<>();
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public List<String> getStrings() {
        return strings;
    }
}
