package com.vnd.model;

import java.util.ArrayList;
import java.util.List;

public class LineSegments{
    public int line;
		public List<Segment> segments;
		
		public boolean contains(int col){
			return find(col) != null;
		}

        public Segment find(int col){
            for(Segment seg : segments){
                if(seg.contains(col))
                    return seg;
            }
            return null;
        }
		
//		public int overlap(Segment seg, int minWidth){
//			int count = 0;
//			for(Segment s : segments){
//				if(s.overlapWith(seg, minWidth)){
//					++count;
//				}
//			}
//			return count;
//		}
		
		public List<Segment> overlaps(Segment seg, int minWidth){
			List<Segment> result = new ArrayList<>();
			for(Segment s : segments){
				if(s.overlapWith(seg, minWidth)){
					result.add(s);
				}
			}
			return result;
		}
		
		public String toString(){
			return "line: " + line + " Segments: " + segments;
		}
	}