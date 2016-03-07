package com.vnd.model;

import java.util.ArrayList;
import java.util.List;

public class LineGroup {
	public static Segment EmptySegment = new Segment(0, 0);

//	public static class Segment{
//		public int start;
//		public int length;
//		public int pixels = 1;
//		private int end;
//
//        public int getEnd(){
//            return end;
//        }
//
//		public void setEnd(int pos){
//			end = pos;
//			length = end - start;
//		}
//
////		public Segment(){}
//		public Segment(int start, int length){
//			this.start = start;
//			this.length = length;
//			end = start + length;
//		}
//
//		public boolean contains(int col){
//			return col >= start && col < end;
//		}
//
//		public boolean overlapWith(Segment seg, int minWidth){
//			int s = start > seg.start ? start : seg.start;
//			int e = end < seg.end ? end : seg.end;
//			return e - s >= minWidth;
//		}
//
//		public String toString(){
//			return "start, length: " + start + ", " + length;
//		}
//	}

	public List<Segment> lines = new ArrayList<>();
	public int start, end;
	public LineGroup(int start){
		this.start = start;
	}
	
	public int findMinCol(){
		int min = Integer.MAX_VALUE;
		for(Segment s : lines){
			if(s.start < min){
				min = s.start;
			}
		}
		return min;
	}
	public int findMaxCol(){
		int max = 0;
		for(Segment s : lines){
			if(s.getEnd() > max){
				max = s.getEnd();
			}
		}
		return max;
	}
//	List<LineSegments> lines = new ArrayList<>();
//	
//	public int start(){
//		if(lines.size() == 0){
//			return -1;
//		}
//		return lines.get(0).line;
//	}
//	
//	public int end(){
//		if(lines.size() == 0){
//			return -1;
//		}
//		return lines.get(lines.size() - 1).line;
//	}
	
	public String toString(){
		return "line start: " + start + " end: " + end;
	}

}
