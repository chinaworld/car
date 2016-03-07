package com.vnd.model;

public class Segment {
    public int start;
    public int length;
    public int pixels = 1;
    private int end;

    public int getEnd() {
        return end;
    }

    public void setEnd(int pos) {
        end = pos;
        length = end - start;
    }

    //		public Segment(){}
    public Segment(int start, int length) {
        this.start = start;
        this.length = length;
        end = start + length;
    }

    public boolean contains(int col) {
        return col >= start && col < end;
    }

    public boolean overlapWith(Segment seg, int minWidth) {
        int s = start > seg.start ? start : seg.start;
        int e = end < seg.end ? end : seg.end;
        return e - s >= minWidth;
    }

    public Segment clone(){
        return new Segment(start, length);
    }

    public String toString() {
        return "start, length: " + start + ", " + length;
    }
}