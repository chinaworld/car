package com.vnd.logic;

import java.util.List;

import com.vnd.util.Util;
import org.opencv.core.Point;
import org.opencv.core.Rect;

public class Slope {
	static float calcSlope(int x1, int y1, int x2, int y2){
		return (float)(y2 - y1) / (float)(x2 - x1);
	}
	
	static float calcSlope(Point p0, Point p1){
		if(p1.x == p0.x){
			return 100;
		}
		return (float)((p1.y - p0.y) / (p1.x - p0.x));
	}
	
	static class SlopeInfo implements Comparable<SlopeInfo>{
		Point p0, p1;
		float slope;
		boolean isTop;
		
		public SlopeInfo(Point p0, Point p1, float slope, boolean isTop) {
			this.p0 = p0;
			this.p1 = p1;
			this.slope = slope;
			this.isTop = isTop;
		}

		@Override
		public int compareTo(SlopeInfo o) {
			float d = slope - o.slope ;
			if(d == 0){
				return 0;
			}else if(d < 0){
				return -1;
			}else{
				return 1;
			}
		}
	}
	
	static SlopeInfo[] calcSlope(List<Rect> groupedAreas, List<Rect> fontAreas){
		int len = groupedAreas.size();
		len = len * (len - 1);
		SlopeInfo[] slopes = new SlopeInfo[len];
		int idx = 0;
		for(int i=1; i<groupedAreas.size(); ++i){
			for(int j=0; j<i; ++j){
				Rect r1 = groupedAreas.get(i);
				Rect f1 = fontAreas.get(i);
				Rect r2 = groupedAreas.get(j);
				Rect f2 = fontAreas.get(j);
				Point pTop0 = new Point(f1.x + r1.x + r1.width / 2, r1.y);
				Point pTop1 = new Point(f2.x + r2.x + r2.width / 2, r2.y);
				float sTop = calcSlope(pTop0, pTop1);
				slopes[idx++] = new SlopeInfo(pTop0, pTop1, sTop, true);
				Util.mark("^^^^^^^^^^^slopes top^^^^^^^^^^: ", slopes[idx - 1].slope);

				Point pBtm0 = new Point(pTop0.x, r1.y + r1.height);
				Point pBtm1 = new Point(pTop1.x, r2.y + r2.height);
				float sBtm = calcSlope(pBtm0, pBtm1);
				slopes[idx++] = new SlopeInfo(pBtm0, pBtm1, sBtm, false);
				Util.mark("^^^^^^^^^^^slopes bottom^^^^^^^^^^: ", slopes[idx-1].slope);
			}
		}
		
//		Arrays.sort(slopes);
//		if(len <= 4){
//			return slopes;
//		}else if(len <= 6){
//			return Arrays.copyOfRange(slopes, 1, len - 1);
//		}
////		else if(len <= 9){
////			return Arrays.copyOfRange(slopes, 2, len - 2);
////		}
//		else{
//			return Arrays.copyOfRange(slopes, 2, len - 2);
//		}
		return slopes;
	}
	
	static float average(SlopeInfo[] slopes){
		float total = 0;
		for(int i=0; i<slopes.length; ++i){
			total += slopes[i].slope;
		}
		return total / slopes.length;
	}
}
