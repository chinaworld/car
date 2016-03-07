package com.vnd.unused;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vnd.logic.Projection;
import com.vnd.logic.TiltCorrecter;
import com.vnd.model.Config;
import com.vnd.model.SubModel;
import com.vnd.util.Util;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class CharExtrator {
	Rect rect;
	int[] vprojection;
	Mat originalPlate;
	Mat plateGrey;
	Mat plateColor;
	
	int subtract(int value, int d){
		int result = value - d;
		if(result < 0){
			result = 0;
		}
		return result;
	}
	int add(int value, int d, int upper){
		int result = value + d;
		if(result >= upper){
			result = upper - 1;
		}
		return result;
	}
	
//	public CharExtrator(Rect rect){
//		this(rect, false);
//	}
	private SubModel model;
	public CharExtrator(SubModel model, Rect rect){
		this.model = model;
//		rect.x = subtract(rect.x, 10);
//		rect.width = add(rect.width, 20, Model.greyImage.width());
//		rect.y = subtract(rect.y, rect.height / 2);
//		rect.height = add(rect.height, rect.height, Model.greyImage.height());
//		this.rect = refinePlate(rect);
		this.rect = rect;
//		EdgeRegion2 er = new EdgeRegion2();
		plateGrey = model.greyImage.submat(rect);
		originalPlate = plateGrey;
		if(Config.isDebug){
//			Mat hist = Histogram.draw(plateGrey);
			model.saveImage(plateGrey, "Plate for Histogram", ++count, "scale", model.getScale());
//			Model.saveImage(hist, "Histogram", count, "scale", Model.scale);
		}
//		Model.saveImage("Plate grey", plateGrey);
//		Imgproc.equalizeHist(plateGrey, plateGrey);
//		Model.saveImage("equalized plate", plateGrey);
//		int thresholdType = Model.reverseColor ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY;
		int t = 0;
		if(!Config.adaptiveBinary || rect.height < 8){
			int quart = plateGrey.cols() / 4;
			Mat sub = plateGrey.colRange(quart, plateGrey.cols() - quart);
			if(model.reverseColor()){
				t = Util.getThreashold(sub, 0.35f);
			}else{
				t = Util.getThreashold(sub, 0.65f);
			}
			plateGrey = Util.binary(plateGrey, t, model.getThresholdType());
		}else{
//			Mat dst = new Mat();
//			int blockSize = plateGrey.rows() / 3;
//			if(blockSize % 2 == 0){
//				++blockSize;
//			}
//			Imgproc.adaptiveThreshold(plateGrey, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, -5);
//			plateGrey = dst;
			plateGrey = Util.adaptiveBinary(plateGrey, -15, model.getThresholdType());
		}
//		plateGrey = er.vdiff(plateGrey);
//		plateGrey = EdgeRegionFinder.sobel(plateGrey);
		model.saveImage(plateGrey, "Plate Grey", model.getScale(), ++count, "threshold", String.valueOf(t));
		plateColor = model.colorImage.submat(rect);
		vprojection = Projection.verticalProjection(plateGrey);
//		if(Config.rotate){
//			rotate();
//		}
	}
	
	static int count = 0;
	
//	Rect refinePlate(Rect rect){
//		Mat plateEdges = Model.edges.submat(rect).clone();
//		Mat binaryEdges = Util.binary(plateEdges, 0.7f, Imgproc.THRESH_BINARY);
//		Rect r1 = new Rect(0, 0, plateEdges.cols() / 2, plateEdges.rows());
//		Rect r2 = r1.clone();
//		r2.x = r1.width;
//		r2.width = rect.width - r1.width;
//		
//		Rect refined1 = refine(binaryEdges.submat(r1), 4);
//		Rect refined2 = refine(binaryEdges.submat(r2), 4);
//		Rect result = new Rect();
//		result.x = rect.x + refined1.x;
//		result.width = rect.width - refined1.x - (r2.width - refined2.x - refined2.width);
//		result.y = rect.y;
//		result.height = rect.height;
//		return result;
//	}
	
//	void removeLeftEdge(Rect leftCharRect){
//		int[] line = findLeftEdge(leftCharRect);
//		int tx = line[0];
//		int ty = line[1];
//		int bx = line[2];
//		int by = line[3];
//		int dx = bx - tx;
//		byte[] pixel = {0};
//		if(dx == 0){
//			for(int y = ty; y<by; ++y){
//				for(int x=0; x<tx; ++x){
//					plateGrey.put(y, x, pixel);
//				}
//			}
//		}else{
//			float a = dx/(float)(by - ty);
//			float b = ty - tx/a;
//			for(int y = ty; y<by; ++y){
//				int left = Math.round(a * (y - b));
//				for(int x=0; x<left; ++x){
//					plateGrey.put(y, x, pixel);
//				}
//			}
//		}
//		Model.saveImage("Remove Left Edge", plateGrey);
//	}
	
//	int[] findLeftEdge(Rect leftCharRect){
//		Rect rect = leftCharRect.clone();
//		rect.width = rect.width / 2;
//		int topX, bottomX;
//		byte[] pixel = new byte[1];
//		for(topX=0; topX < rect.width; ++topX){
//			plateGrey.get(rect.y, topX, pixel);
//			if(pixel[0] != 0){
//				break;
//			}
//		}
//		int bottomY = rect.y + rect.height;
//		for(bottomX=0; bottomX < rect.width; ++bottomX){
//			plateGrey.get(bottomY, bottomX, pixel);
//			if(pixel[0] != 0){
//				break;
//			}
//		}
//		
//		for(; topX < rect.width; ++topX, ++bottomX){
//			if(!isLine(topX, rect.y, bottomX, bottomY)){
//				break;
//			}
//		}
//		for(; topX < rect.width; ++topX){
//			if(!isLine(topX, rect.y, bottomX, bottomY)){
//				break;
//			}
//		}
//		for(; bottomX < rect.width; ++bottomX){
//			if(!isLine(topX, rect.y, bottomX, bottomY)){
//				break;
//			}
//		}
//		return new int[]{topX, rect.y, bottomX, bottomY};
//	}
	
//	boolean isLine(int tx, int ty, int bx, int by){
//		int alowedBlank = 2;
//		int blank = 0;
//		int dx = bx - tx;
//		byte[] pixel = new byte[1];
//		if(dx == 0){
//			for(int y = ty; y<by; ++y){
//				plateGrey.get(y, tx, pixel);
//				if(pixel[0] == 0){
//					++blank;
//					if(blank > alowedBlank)
//						return false;
//				}
//			}
//		}else{
//			float a = dx/(float)(by - ty);
//			float b = ty - tx/a;
//			for(int y = ty; y<by; ++y){
//				int x = Math.round(a * (y - b));
//				plateGrey.get(y, x, pixel);
//				if(pixel[0] == 0){
//					++blank;
//					if(blank > alowedBlank)
//						return false;
//				}
//			}
//		}
//		return true;
//	}
	
//	void rotate(){
//		int pixelWidth = sumPeaks(vprojection);
//		
//		Mat plate = plateGrey;
//		int toRotate = 0;
//		int d = 1;
//		for(int angle=d; angle<20; angle += d){
//			plate = Util.rotate(plateGrey, angle);
//			Model.saveImage(plate, "rotated with angle", angle);
////			Model.saveImage("Rotated Plate: " + angle, plate);
//			int[] projection = Projection.verticalProjection(plate);
////			Mat graph = drawProjection(plate, projection);
////			Model.saveImage("Projection Graph: + " + angle, graph);
//			int width = sumPeaks(projection);
//			if(width >= pixelWidth){
//				toRotate = angle - d;
//				break;
//			}else{
//				pixelWidth = width;
//			}
//		}
//		if(toRotate == 0){
//			d = -1;
//			for(int angle=d; angle>-20; angle += d){
//				plate = Util.rotate(plateGrey, angle);
//				Model.saveImage(plate, "rotated with angle", angle);
////				Model.saveImage("Rotated Plate: " + angle, plate);
//				int[] projection = Projection.verticalProjection(plate);
////				Mat graph = drawProjection(plate, projection);
////				Model.saveImage("Projection Graph: + " + angle, graph);
//				int width = sumPeaks(projection);
//				if(width >= pixelWidth){
//					toRotate = angle - d;
//					break;
//				}else{
//					pixelWidth = width;
//				}
//			}
//		}
//		if(toRotate != 0){
//			plateGrey = Util.rotate(plateGrey, toRotate);
//			Model.saveImage(plateGrey, "Rotated Plate Grey", toRotate);
//			plateColor = Util.rotate(plateColor, toRotate);
//			vprojection = Projection.verticalProjection(plateGrey);
//		}
//	}
	
	int sumPeaks(int[] projection){
		List<Peak> peaks = findPeaks(2, projection);
		int sum = 0;
		for(Peak p : peaks){
			sum += p.end - p.start;
		}
		return sum;
	}
	
	static class Peak{
		int start = -1;
		int end = -1;
		
		public String toString(){
			return start + ", " + end;
		}
	}
	
	boolean isPoint(Rect rect, int fontSize){
		if(rect.width * 2 > fontSize){
			return false;
		}
		Mat mat = plateGrey.submat(rect);
		int[] hproj = Projection.horizontalProjection(mat);
//		Projection.filterProjection(hproj, rect.height / 3, rect.width / 2, 2);
		int height = Projection.getFilteredLength(hproj, 1, rect.height / 3, 2);
		if(height == 0){
			return true;
		}else if(height > hproj.length * 0.8f){
			return false;
		}
		int[] proj = Arrays.copyOfRange(vprojection, rect.x, rect.x + rect.width);
		int minHeight = rect.height / 3;
		int count = 0;
		for(int i=0; i<proj.length; ++i){
			if(proj[i] > minHeight){
				++count;
			}
		}
		return count < 1 || (proj.length > 3 && count < 2);
	}
	
//	float maxDiffRatio = 0.5f;
	
	List<Rect> filterBySize(List<Rect> rects, int fontSize){
		float maxDiffRatio = (fontSize > 16 ? 0.4f : 0.6f);
		float maxDiff = maxDiffRatio * fontSize;
		
		List<Rect> result = new ArrayList<>();
//		float maxDiff = maxDiffRatio * fontSize;

		for(Rect r : rects){
			if(Math.abs(fontSize - r.width) < maxDiff){
				result.add(r);
			}
		}
		return result;
	}
	
	List<int[]> getSubLists(int[] original, int length){
		List<int[]> result = new ArrayList<>();
		if(original.length <= length){
			result.add(original);
			return result;
		}
		for(int i=0, len=original.length - length; i<=len; ++i){
			result.add(Arrays.copyOfRange(original, i, i + length));
		}
		return result;
	}
	
	boolean isIntervalsCorrect(int[] intervals, int fontSize){
		int itval = fontSize + 1;
		int abnormalCount = 0;
		float maxDiffRatio = (fontSize > 10 ? 0.2f : 0.3f);
		float maxDiff = itval * maxDiffRatio;
		if(model.fontsCount == 7){
			int mitval = Math.round(itval * intervalRatio);
//			float mmaxDiff = mitval * maxDiffRatio;
			for(int i=0; i<intervals.length; ++i){
				int d;
				if(i == model.dotPos){
					d = intervals[i] - mitval;
				}else{
					d = intervals[i] - itval;
				}
				if(Math.abs(d) >= maxDiff){
					++abnormalCount;
				}
			}
		}else{
			for(int i=0; i<intervals.length; ++i){
				if(Math.abs(intervals[i] - itval) >= maxDiff){
					++abnormalCount;
				}
			}
		}
		return abnormalCount <= 2;
	}
	
	boolean hasCorrectIntervals(List<Rect> rects, int fontSize){
		if(rects.size() < model.fontsCount){
			return false;
		}
		int[] intervals = getIntervals(rects);
		List<int[]> subs = getSubLists(intervals, model.fontsCount - 1);
		for(int[] itvals : subs){
			if(isIntervalsCorrect(itvals, fontSize)){
				return true;
			}
		}
		return false;
//		int[] copy = Arrays.copyOf(intervals, intervals.length);
//		Arrays.sort(copy);
		
//		int count = 0;
//		for(int i=0; i<intervals.length - 1; ++i){
//			int current = intervals[i];
//			int next = intervals[i+1];
//			int min, max;
//			
//			if(current < next){
//				min = current;
//				max = next;
//			}else{
//				min = next;
//				max = current;
//			}
//			
//			if(min * 2 <= max){
//				++count;
//			}
//		}
//		return intervals.length - count >= 4;
	}
	
	List<Rect> filterByIntervals(List<Rect> rects){
		int[] intervals = getIntervals(rects);
//		int[] copied = Arrays.copyOf(intervals, intervals.length);
//		Arrays.sort(copied);
//		int middle = copied[copied.length / 2];
//		float maxDiff = middle * 0.8f;
		int start = 0;
		int end = intervals.length - 1;
		for(int i=0; i<intervals.length - 1; ++i){
			int current = intervals[i];
			int next = intervals[i+1];
			int min, max;
			
			if(current < next){
				min = current;
				max = next;
			}else{
				min = next;
				max = current;
			}
			
			if(min * 3 < max){
				start = i;
				break;
			}
		}
		for(int i=start + 1; i<intervals.length - 1; ++i){
			int current = intervals[i];
			int next = intervals[i+1];
			int min, max;
			
			if(current < next){
				min = current;
				max = next;
			}else{
				min = next;
				max = current;
			}
			
			if(max >= min * 3){
				end = i;
				break;
			}
		}
		if(end <= start){
			return new ArrayList<>();
		}
		return rects.subList(start, end + 2);
//		List<Rect> result = new ArrayList<>();
////		float maxDiff = maxDiffRatio * fontSize;
//
//		for(Rect r : rects){
//			if(Math.abs(fontSize - r.width) < maxDiff){
//				result.add(r);
//			}
//		}
//		return result;
	}
	
	List<Rect> filterBlocks(List<Rect> rects){
		List<Rect> result = new ArrayList<>();
		for(Rect r : rects){
			Mat img = plateGrey.submat(r);
			int count = Core.countNonZero(img);
			int total = r.width * r.height;
			if(total != 0 && (float)count / (float)total < 0.9){
				result.add(r);
			}
		}
		return result;
	}
	
	List<Rect> split(Rect rect){
		Mat mat = originalPlate.submat(rect);

		int t = Util.getThreashold(mat, 0.65f);
		Mat binary = Util.binary(mat, t, model.getThresholdType());
		int[] proj = Projection.verticalProjection(binary);
		
		if(Config.isDebug){
			Mat mp = drawProjection(binary);
			model.saveImage(mp, "Split font", ++count);
		}
		
		for(int v = model.getScale() + 1, max = originalPlate.height() / 3; v < max; ++v){
			List<Rect> rects = getRectsByPeaks(v, proj);
			if(rects.size() >= 2){
				return rects;
			}
		}
		List<Rect> result = new ArrayList<>();
		int w = rect.width / 2 - 1;
		Rect r0 = rect.clone();
		r0.width = w;
		Rect r1 = rect.clone();
		r1.x = r0.x + r0.width + 1;
		r1.width = w;
		result.add(r0);
		result.add(r1);
		return result;
	}
	
	List<Rect> getRectsByPeaks(int peakLineValue, int[] proj){
		List<Peak> peaks = findPeaks(peakLineValue, proj);
		List<Rect> rects = new ArrayList<>();
		for(Peak p : peaks){
			Rect r = getFontRect(p);
			if(r != null)
				rects.add(r);
		}
//		if(Model.isDebug){
//			Mat mat = drawRects(rects);
//			Model.saveImage("initial split", mat);
//		}
		return rects;
	}
	
	List<Rect> split(List<Rect> rects){
		List<Rect> result = new ArrayList<>();
		int[] intervals = getIntervals(rects);
		int avg = getAvgFontSize(intervals) - 1;
		int twice = avg * 2;
//		int[] sorted = intervals.clone();
//		Arrays.sort(sorted);
		for(Rect r : rects){
			if(r.width < twice){
				result.add(r);
			}else{
				result.addAll(split(r));
			}
		}
		return result;
	}
	
	private HashMap<Rect, List<Rect>> mergedRects = new HashMap<>();
	List<Rect> getFontRects(int peakBottomValue){
//		Arrays.fill(Model.isMerged, false);
		mergedRects.clear();
		List<Rect> rects = new ArrayList<>();
		List<Peak> peaks = findPeaks(peakBottomValue, vprojection);
		for(Peak p : peaks){
			Rect r = getFontRect(p);
			if(r != null)
				rects.add(r);
		}
		if(Config.isDebug){
			Mat mat = drawRects(rects);
			model.saveImage("initial split", mat);
		}
//		System.out.println("rect count: " + rects.size());
//		rects = filterBlocks(rects);
		if(rects.size() < 5){
			return rects;
		}
//		int[] intervals = getIntervals(rects);
		int fontSize = getAvgFontSize(getIntervals(rects));
		for(Iterator<Rect> itr = rects.iterator(); itr.hasNext();){
			Rect r = itr.next();
			if(isPoint(r, fontSize)){
				itr.remove();
			}
		}
		if(rects.size() < 5){
			return rects;
		}
		if(rects.size() < 7){
			rects = split(rects);
		}
		if(rects.size() < 7){
			if(tryAddFirst(rects)){
				return rects;
			}else{
				return Collections.emptyList();
			}
		}
		fontSize = getAvgFontSize(getIntervals(rects));
		rects = mergeH(rects, fontSize);
		fontSize = getAvgFontSize(getIntervals(rects));
		if(!hasCorrectIntervals(rects, fontSize)){
			return Collections.emptyList();
		}
//		rects = filterByIntervals(rects);
		rects = resizeFontRects1(rects, fontSize);
		fontSize = getAvgFontSize(getIntervals(rects));
		if(fontSize < Config.MIN_FONT_WIDTH){
			return Collections.emptyList();
		}
//		resizeFontRects(rects, fontSize);

//		List<Rect> result = new ArrayList<>();

//		for(Rect r : rects){
//			if(Math.abs(fontSize - r.width) < maxDiff){
//				result.add(r);
//			}
//		}
//		rects = result;
		rects = filterBySize(rects, fontSize);
		if(rects.size() < 7){
			return rects;
		}
//		result = new ArrayList<>();
		filterLastByLagestGap(rects);
//		for(Rect r : rects){
//			if(Math.abs(fontSize - r.width) < maxDiff){
//				result.add(r);
//			}
//		}
//		result = filterBySize(rects, maxDiff, fontSize);
		if(rects.size() < 7){
			return rects;
		}
		List<Rect> result = rects;
		if(rects.size() > 7){
			result = getLeastDiffRects(rects, fontSize);
		}
//		if(result.size() > 7){
//			List<Rect> result2 = findByLagestGap(result);
//			if(result2.size() != 7){
//				result = getLeastDiffRects(result, fontSize);
//			}else{
//				result = result2;
//			}
//		}
//		processChuan(result, fontSize);
		if(isCorrect(result)){
			amendFirstLast(result);
			return result;
		}else{
			if(tryFixSize(result)){
				return result;
			}
			return Collections.emptyList();
		}
	}
	
	void amendFirst(List<Rect> rects, int avgInterval){
		int half = avgInterval / 2;
		Rect r0 = rects.get(0);
		Rect r1 = rects.get(1);
		int mid1 = r1.x + half;
		int mid0 = mid1 - avgInterval;
		int to = mid0 + half;
		r0.x = to - avgInterval;
		if(r0.x < 0){
			r0.x = 0;
		}
		r0.width = to - r0.x - 1;
	}
	
	private void amendFirstLast(List<Rect> rects){
		int fontSize = getAvgFontSize(getIntervals(rects)) + 1;
		Rect first = rects.get(0);
		int firstDiff = fontSize - first.width;
		if(firstDiff > 0){
//			first.x -= firstDiff;
//			first.width = fontSize;
			amendFirst(rects, fontSize);
		}
		Rect last = rects.get(rects.size() - 1);
		int lastDiff = fontSize - last.width;
		if(lastDiff > 0 && last.x + fontSize <= plateGrey.cols()){
			last.width = fontSize;
		}
	}
	
	boolean tryAddFirst(List<Rect> rects){
		if(rects.size() != 6){
			return false;
		}
		
		int itvals[] = getIntervals(rects);
		int fontSize = getAvgFontSize(itvals);
		
		int itval = fontSize + 1;
		int mitval = Math.round(itval * intervalRatio);

		int diff = Math.abs(mitval - itvals[0]);
		if(diff > 2){
			return false;
		}
		for(int i=1; i<itvals.length; ++i){
			int d = Math.abs(itval - itvals[i]);
			if(d > 2){
				return false;
			}
			diff += d;
		}
		
		if(diff < 6){
			Rect r0 = new Rect();
			rects.add(0, r0);
			amendFirst(rects, itval);
			return isCorrect(rects);
		}
		return false;
	}
	
	boolean tryFixSize(List<Rect> rects){
		int fontSize = getAvgFontSize(getIntervals(rects)) + 1;
		int diff = fontSize - rects.get(0).width;
		if(diff > 1){
//			rects.get(0).x -= diff;
//			rects.get(0).width = fontSize;
			amendFirst(rects, fontSize);
			if(isCorrect(rects)){
				return true;
			}
		}
		return false;
	}
	
	List<Rect> getLeastDiffRects(List<Rect> rects, int fontSize){
		if(rects.size() < model.fontsCount + 1){
			return rects;
		}
		int[] intervals = getIntervals(rects);
		int leastDiff = Integer.MAX_VALUE;
		int leastIndex = -1;
		for(int i=0,len=rects.size() - model.fontsCount + 1; i<len; ++i){
			int diff = getDiff(Arrays.copyOfRange(intervals, i, i+6), fontSize);
			if(diff < leastDiff){
				leastDiff = diff;
				leastIndex = i;
			}
		}
		if(leastIndex >= 0){
			return rects.subList(leastIndex, leastIndex + 7);
		}else{
			return rects;
		}
	}
	
	static float intervalRatio = (45 + 12 * 2 + 10f) / (45f + 12f);
	
	int getDiff(int[] intervals, int fontSize){
		int avg = fontSize + 1;
		int maxInterval = Math.round(intervalRatio * avg);
		int diff= 0;
		for(int i=0; i<intervals.length; ++i){
			int itv = intervals[i];
			int d;
			if(i != 1){
				d = itv - avg;
			}else{
				d = itv - maxInterval;
			}
			diff += d * d;
		}
		return diff;
	}
	
	boolean isSizeCorrect(List<Rect> rects){
		int[] wids = new int[rects.size()];
		for(int i=0; i<wids.length; ++i){
			wids[i] = rects.get(i).width;
		}
		Arrays.sort(wids);
		float ratio = (float)wids[wids.length - 1] / wids[0];
		if(ratio >= 2){
			return false;
		}else if(ratio >= 1.5){
			ratio = (float)wids[wids.length - 2] / wids[0];
			if(ratio >= 1.5){
				return false;
			}
		}
		return true;
	}
	
//	static int NORMAL_D = 57; // 45 + 12;
//	static int MAX_D = 79; //45 + 12 + 12 + 10;
//	static float RATIO_D = MAX_D / (float)NORMAL_D;
	boolean isCorrect(List<Rect> rects){
		if(rects.size() != model.fontsCount){
			return false;
		}
		if(!isSizeCorrect(rects)){
			return false;
		}
		int abnormal = 0;
		for(Rect r : rects){
			float ratio = r.height / (float)r.width;
			if(ratio > Config.MAX_FONT_HEIGHT_WIDTH_RATIO || ratio < Config.MIN_FONT_HEIGHT_WIDTH_RATIO){
				++abnormal;
			}
		}
		if(abnormal > 3){
			return false;
		}
		int[] intervals = getIntervals(rects);
		int maxPos = getMaxPos(intervals);
		if(maxPos != 1){
			Rect rlast = rects.get(rects.size() - 1);
			if(rlast.x + rlast.width - rects.get(0).x > 200){
				return false;
			}
			int diff = intervals[maxPos] - intervals[1];
			if((float)diff / intervals[maxPos] > 0.2f)
				return false;
		}
		Arrays.sort(intervals);
		int max = intervals[intervals.length - 1];
		int max1 = intervals[intervals.length - 2];
		int min = intervals[0];
		if(max1 > min * 1.5 || max > max1 * 2){
			return false;
		}
		return true;
	}
	
	static class Continous{
		int start = 0;
		int count = 0;
	}
	
//	List<Rect> mergeSingle(List<Rect> rects, int index, int fontSize){
//		List<Rect> copied = new ArrayList<>(rects);
//		Rect cur = copied.get(index);
//		float maxDiff = fontSize * 0.3f;
//		float diff = fontSize * 0.2f;
//		if(index > 0){
//			Rect pre = copied.get(index - 1);
//			pre.width = cur.x + cur.width - pre.x;
//			boolean sizeOk = isFine(pre.width, fontSize, diff);
//			boolean preIntervalOk = true;
//			if(index > 1){
//				Rect prepre = copied.get(index - 2);
//				int itvl = getinterval(prepre, pre);
//				preIntervalOk = isFine(itvl, fontSize, maxDiff);
//			}
//			copied.remove(index);
//		}
//	}
	
	List<Rect> mergeH(List<Rect> rects, int fontSize){
//		List<Rect> rects = new ArrayList<>(splited);
//		List<Rect> nearVBars = new ArrayList<>();
		List<Continous> continousVBars = new ArrayList<>();
		
		Continous current = null;
		for(int i=0; i<rects.size(); ++i){
			Rect r = rects.get(i);
			if(isVBar(r, fontSize)){
				if(current == null){
					current = new Continous();
					continousVBars.add(current);
					current.start = i;
				}
				++current.count;
			}else{
				current = null;
			}
		}
		
		List<Rect> result = rects;
		for(int i=0; i<continousVBars.size(); ++i){
//		for(Continous c : continousVBars){
			Continous c = continousVBars.get(i);
			if(c.count < 2){
				continue;
			}
			List<Rect> temp = merge(result, c, fontSize);
			int removed = rects.size() - temp.size();
			if(removed > 0){
//				for(int j=i+1; j<continousVBars.size(); ++j){
//					continousVBars.get(j).start -= removed;
//				}
				return mergeH(temp, fontSize);
			}
			result = temp;
		}
		return result;
	}
	
	boolean isFine(int width, int fontSize, float maxDiff){
		return Math.abs(width - fontSize) < maxDiff;
	}
	
	void addMergedRects(Rect merged, Rect... rects){
		List<Rect> subs = Arrays.asList(rects);
		mergedRects.put(merged, subs);
	}
	
	List<Rect> merge(List<Rect> rects, Continous vBars, int fontSize){
		float maxDiff = fontSize * 0.3f;
//		boolean isMerged = false;
		if(vBars.count >= 3){
			List<Rect> copied = new ArrayList<>(rects);
			int i3 = vBars.start + vBars.count - 1;
			if(rects.size() - i3 >= 7){
				Rect r3 = rects.get(i3);
				Rect r2 = rects.get(i3 - 1);
				Rect r1 = rects.get(i3 - 2);
				Rect merged = r1.clone();
				merged.width = r3.x + r3.width - r1.x;
				copied.set(i3, merged);
				for(int i=i3-1; i>=0; --i){
					copied.remove(i);
				}
				fontSize = getAvgFontSize(getIntervals(copied));
				if(isFine(merged.width, fontSize, maxDiff) && 
						isFine(getinterval(merged, rects.get(i3 + 1)), fontSize, maxDiff)){
//					mergedRects.add(merged);
					addMergedRects(merged, r1, r2, r3);
//					copied.remove(i3 - 1);
//					copied.remove(i3 - 2);
					return copied;
				}
			}
		}

		if(vBars.count >= 2){
			List<Rect> copied = new ArrayList<>(rects);
			int i3 = vBars.start + vBars.count - 1;
			Rect r2 = rects.get(i3);
			Rect r1 = rects.get(i3 - 1);
			Rect merged = r1.clone();
			merged.width = r2.x + r2.width - r1.x;
			if(isFine(merged.width, fontSize, maxDiff)){
				copied.set(i3, merged);
//				mergedRects.add(merged);
				addMergedRects(merged, r1, r2);
				copied.remove(i3 - 1);
//				for(int i=i3-1; i>=vBars.start; --i){
//					copied.remove(i);
//				}
//				vBars.start += 1; //not +2 because a bar is already removed
				vBars.count -= 2;
				return merge(copied, vBars, fontSize);
			}
		}
//		if(vBars.count >= 2 && vBars.start > 1){
//			List<Rect> copied = new ArrayList<>(rects);
//			Rect r1 = rects.get(vBars.start);
//			Rect r2 = rects.get(vBars.start + 1);
//			Rect merged = r1.clone();
//			merged.width = r2.x + r2.width - r1.x;
//			if(isFine(merged.width, fontSize, maxDiff)){
//				copied.set(vBars.start + 1, merged);
//				mergedRects.add(merged);
//				copied.remove(vBars.start);
//				vBars.start += 1; //not +2 because a bar is already removed
//				vBars.count -= 2;
//				return merge(copied, vBars, fontSize);
//			}
//		}
		return rects;
	}
	
	
	
	boolean isVBar(Rect rect, int fontSize){
		float diff = fontSize < 10 ? 0.6f : 0.5f;
		if(rect.width > fontSize * diff){
			return false;
		}
//		Mat img = plateGrey.submat(rect);
//		int[] hproj = Projection.horizontalProjection(img);
		int[] hproj2 = Arrays.copyOfRange(vprojection, rect.x, rect.x + rect.width); 
//		Projection.filterProjection(hproj, rect.height / 3, rect.width / 2, 2);
		int minHeight = rect.height / 2;
		for(int v : hproj2){
			if(v > minHeight){
				return true;
			}
		}
//		int height = Projection.getFilteredLength(hproj, 1, minHeight, 2);
//		return height > minHeight;
		return false;
		
//		int count = 0;
//		byte[] pixels = new byte[1];
//		for(int r=0; r<img.rows(); ++r){
//			for(int c=0; c<img.cols(); ++c){
//				img.get(r, c, pixels);
//				if(pixels[0] != 0){
//					++count;
//				}
//			}
//		}
//		
//		return (float)count / (img.rows() * img.cols()) > 0.4f;
	}
	
	//process ´¨
	void processChuan(List<Rect> rects, int fontSize){
		int[] intervals = getIntervals(rects);
		int maxPos = getMaxPos(intervals);
//		float maxDiff = maxDiffRatio * fontSize;
		int toAdd = 0; //(int)(maxDiff/2);
//		if(toAdd == 0)
//			toAdd = 1;
		if(intervals.length - maxPos == 5){
			Rect r1 = rects.get(maxPos);
			if(maxPos == 0){
				Rect r = new Rect();
				r.width = fontSize + toAdd;
				r.x = r1.x - r.width;
				if(r.x < 0){
					r.width += r.x;
					r.x = 0;
				}
				r.y = r1.y;
				r.height = r1.height;
				rects.add(0, r);
			}else{
				Rect r = rects.get(maxPos - 1);
				r.width = fontSize + toAdd;
				r.x = r1.x - r.width;
				if(r.x < 0){
					r.width += r.x;
					r.x = 0;
				}
//				if(r.x + r.width >= r1.x)
				while(rects.get(0) != r){
					rects.remove(0);
				}
			}
		}
//		if((intervals.length - maxPos == 5 && r0.width < fontSize)){
//			int width = fontSize + toAdd;
//			r0.x = r1.x - width;
//			r0.width = width;
//		}else if(maxPos == 0 && rects.size() == 6){
//			Rect r = new Rect();
//			r.width = fontSize + toAdd;
//			r.x = r0.x - r.width;
//			r.y = r0.y;
//			r.y = r0.height;
//			rects.add(0, r);
//		}
	}
	
	//The largest gap is at 2nd position, use this rule to filter.
		void filterLastByLagestGap(List<Rect> rects){
			if(rects.size() < 8){
				return;
			}
			int[] intervals = getIntervals(rects);
			int avgInteval = middleAvg(intervals);
			
			int maxPos = getMaxPos(intervals);
			if(intervals[maxPos] > avgInteval * 1.8f){
				if(maxPos + 1 > rects.size() - maxPos){
					for(int i=rects.size() - 1; i>maxPos; --i){
						rects.remove(i);
					}
				}else{
					for(int i=0; i<=maxPos; ++i){
						rects.remove(i);
					}
				}
				filterLastByLagestGap(rects);
			}else 
				if(maxPos == intervals.length - 1){
				rects.remove(rects.size() - 1);
				filterLastByLagestGap(rects);
			}else if(maxPos == 0){
				rects.remove(0);
				filterLastByLagestGap(rects);
			}
		}

		//The largest gap is at 2nd position, use this rule to filter.
		List<Rect> findByLagestGap(List<Rect> rects){
			List<Rect> result = new ArrayList<>(rects);
			filterByLagestGap(result);
			return result;
		}
	
	//The largest gap is at 2nd position, use this rule to filter.
	void filterByLagestGap(List<Rect> rects){
		if(rects.size() < 7){
			return;
		}
		int[] intervals = getIntervals(rects);
		int maxPos = getMaxPos(intervals);
		int maxLeft = maxPos - 1;
		if(maxLeft < 0 || intervals[maxPos] - intervals[maxLeft] >= 2){
			maxLeft = maxPos;
		}
		int maxRight = maxPos + 1;
		if(maxRight > rects.size() - 5 || intervals[maxPos] - intervals[maxRight] >= 2){
			maxRight = maxPos;
		}
		if(maxPos == 0 || (maxLeft > 1 && maxLeft < intervals.length - 1)){
			rects.remove(0);
			filterByLagestGap(rects);
		}else if(maxPos == intervals.length - 1 || intervals.length - maxRight > 5){
			rects.remove(rects.size() - 1);
			filterByLagestGap(rects);
		}
//		LinkedList<IntervalPosition> ips = new LinkedList<>();
//		for(int i=0; i<intervals.length; ++i){
//			ips.add(new IntervalPosition(intervals[i], i));
//		}
//		Collections.sort(ips);
//		IntervalPosition max = ips.peekLast();
//		while(max.pos == 0 || max.pos == ips.size() - 1){
//			ips.removeLast();
//			if(max.pos == 0){
//				shiftLeft(ips);
//			}
//			max = ips.peekLast();
//		}
//		while(max.pos > 1){
//			removePos0(ips);
//		}
	}
	
	void removeOverlap(List<Rect> rects){
		for(int i=0, last = rects.size() - 1; i<last; ++i){
			Rect r0 = rects.get(i);
			Rect r1 = rects.get(i + 1);
			if(r0.x < 0)
				r0.x = 0;
			if(r1.x + r1.width > plateGrey.cols()){
				r1.width = (plateGrey.cols() - r1.x - 1);
			}
			int right0 = r0.x + r0.width;
			if(right0 > r1.x){
				if(r0.width > r1.width){
					r0.width = r1.x - r0.x;
				}else{
					int d = right0 - r1.x;
					r1.x = right0;
					r1.width -= d;
				}
			}
		}
	}
	
	void removePos0(LinkedList<IntervalPosition> ips){
		IntervalPosition item = null;
		for(IntervalPosition ip : ips){
			if(ip.pos == 0){
				item = ip;
				break;
			}
		}
		if(item != null){
			ips.remove(item);
			shiftLeft(ips);
		}
	}
	
	void shiftLeft(LinkedList<IntervalPosition> ips){
		for(IntervalPosition ip : ips){
			ip.pos--;
		}
	}
	
	int limitLeft(int left, int limit){
		if(left > limit){
			left = limit;
		}
		if(left < 0){
			left = 0;
		}
		return left;
	}
	int limitRight(int right, int limit, int max){
		if(right < limit){
			right = limit;
		}
		if(right > max){
			right = max;
		}
		return right;
	}
	
	List<Rect> resizeFontRects1(List<Rect> rects, int fontSize){
		List<Rect> result = new ArrayList<>();
		int halfSize = fontSize / 2;
		for(int i=0; i<rects.size(); ++i){
			Rect r = rects.get(i).clone();
			int mid = r.x + r.width / 2;
			if(i != 0){
				r.x = mid - halfSize - 1;
				r.width = fontSize + 1;
			}else{
				r.x = mid - halfSize;
				r.width = fontSize;
			}
//			r.x = mid - halfSize;
			result.add(r);
		}
		limitRects(rects, result);
//		removeOverlap(result);
		return result;
	}
	
	void limitRects(List<Rect> rects, List<Rect> result){
		for(int i=0; i<rects.size(); ++i){
			Rect rold = rects.get(i);
			int limL = 0;
			if(i > 0){
				Rect rl = rects.get(i-1);
				limL = rl.x + rl.width;
			}
			int limR = plateGrey.cols();
			if(i < rects.size() - 1){
				limR = rects.get(i + 1).x;
			}
			Rect rnew = result.get(i);
			if(rnew.x > rold.x){
				rnew.x = rold.x;
			}
			if(rnew.x < limL){
				rnew.x = limL;
			}
			int maxr = rold.x + rold.width;
			if(rnew.x + rnew.width < maxr){
				rnew.width = maxr - rnew.x;
			}
			if(rnew.x + rnew.width > limR){
				rnew.width = limR - rnew.x;
			}
			if(i == rects.size() - 1){
				int leftAdded = Math.abs(rnew.x - rold.x);
				int rightAdded = rnew.width - rold.width - leftAdded;
				int diff = rightAdded - leftAdded;
				int half = rnew.width / 2;
				if(diff >= half){
					rnew.width -= diff;
				}else if(diff <= -half && rnew.x + rnew.width < limR){
					int d = -diff;
					rnew.x += d;
					rnew.width -= d;
				}
			}
		}
	}
	
	void resizeFontRects(List<Rect> rects, int fontSize){
		int halfSize = fontSize / 2;
		int last = rects.size() - 1;
		for(int i=0; i<rects.size(); ++i){
			Rect r = rects.get(i);
//		for(Rect r : rects){
			int mid = r.x + r.width / 2;
			int left = mid - halfSize;
			int right = mid + halfSize;
			int maxRight = plateGrey.cols();
			if(i < last){
				Rect next = rects.get(i + 1);
				maxRight = next.x;
				if(right > next.x){
//					int d = right - next.x + 1;
					right = next.x;
//					left += d;
//					limitLeft(left, r.x);
				}
			}
			if(i > 0){
				Rect pre = rects.get(i - 1);
				int preRight = pre.x + pre.width;
				if(left < preRight){
//					int d = preRight - left + 1;
					left = preRight;
//					right -= d;
//					limitRight(right, r.x + r.width);
				}
			}
			left = limitLeft(left, r.x);
			right = limitRight(right, r.x + r.width, maxRight);
			r.x = left;
			r.width = right - left;
		}
	}
	
//	void addSpace(List<Rect> rects, int[] intervals){
//		for(int i=0; i<intervals.length; ++i){
//			Rect r = rects.get(i);
//			int rmid = r.x + r.width / 2;
//			Rect next = rects.get(i + 1);
//			int nmid = next.x + next.width / 2;
//			int left;
//			int right = (rmid + nmid)/2;
//			if(right >= next.x){
//				right = next.x - 1;
//			}
//			if(i == 0){
//				left = rmid - (right - rmid);
//			}else{
//				Rect pre = rects.get(i - 1);
//				
//			}
//		}
//	}
	
//	int getAvgFontSize(List<Rect> rects){
//		int[] copy = getIntervals(rects);
//		if(copy.length > 7){
//			Arrays.sort(copy);
//			int start = copy.length - 4;
//			int m1 = Math.round((copy[start - 1] + copy[start] + copy[start + 1]) / 3f) - 1;
//			for(int i=0; i<copy.length; ++i){
//				if(copy[i] * 2 > m1){
//					start = (copy.length - i) / 2 + i;
//					return Math.round((copy[start - 1] + copy[start] + copy[start + 1]) / 3f) - 1;
//				}
//			}
//		}
//		return middleAvg(intervals) - 1;
//	}
	
	int getAvgFontSize(int[] intervals){
		if(intervals.length > 7){
			int[] copy = intervals.clone();
			Arrays.sort(copy);
			int start = intervals.length - 4;
			int m1 = Math.round((copy[start - 1] + copy[start] + copy[start + 1]) / 3f) - 1;
			for(int i=0; i<copy.length; ++i){
				if(copy[i] * 2 > m1){
					start = (intervals.length - i) / 2 + i;
					return Math.round((copy[start - 1] + copy[start] + copy[start + 1]) / 3f) - 1;
				}
			}
		}
		return middleAvg(intervals) - 1;
	}
	
	static int middleAvg(int[] values){
		if(values.length < 3){
			throw new RuntimeException("Not enough intervals");
		}
		int[] copy = values.clone();
		Arrays.sort(copy);
		int start = copy.length / 2;
		return Math.round((copy[start - 1] + copy[start] + copy[start + 1]) / 3f);
	}
	
	static class IntervalPosition implements Comparable<IntervalPosition>{
		int interval;
		int pos;
		
		public IntervalPosition(int interval, int pos){
			this.interval = interval;
			this.pos = pos;
		}

		@Override
		public int compareTo(IntervalPosition o) {
			return interval - o.interval;
		}
	}
	
	int[] getIntervals(List<Rect> rects){
		int[] result = new int[rects.size() - 1];
		for(int i=0; i<result.length; ++i){
			Rect r = rects.get(i);
			Rect next = rects.get(i + 1);
//			int s = r.x + r.width / 2;
//			int e = next.x + next.width / 2;
//			result[i] = e - s;
			result[i] = getinterval(r, next);
		}
		return result;
	}
	
	int getinterval(Rect r, Rect next){
		int s = r.x + r.width / 2;
		int e = next.x + next.width / 2;
		return e - s;
	}
	
//	int tempMaxValue = 0;
	int getMaxPos(int[] values){
		int max = 0;
		int pos = -1;
		for(int i=0; i<values.length; ++i){
			if(values[i] > max){
				max = values[i];
				pos = i;
			}
		}
		
//		tempMaxValue = max;
		return pos;
	}
	
	Rect getFontRect(Peak peak){
//		int[] hprojections = Projection.horizontalProjection(plateGrey, peak.start, peak.end + 1);
////		Projection.fillGaps(hprojections, 2);
//		Projection.filterProjection(hprojections, -1, plateGrey.rows() / 2, 3);
//		
//		int min = Integer.MAX_VALUE;
//		int max = 0;
//		for(int i=0; i<hprojections.length; ++i){
//			if(hprojections[i] > 0){
//				if(i < min){
//					min = i;
//				}
//				if(i > max){
//					max = i;
//				}
//			}
//		}
//		if(max == 0){
//			return null;
//		}
		return new Rect(peak.start, 0, peak.end - peak.start + 1, plateGrey.rows());
	}
	
	public static List<Peak> findPeaks(int value, int[] vprojection){
		List<Peak> peaks = new ArrayList<>();
		Peak current = new Peak();
		if(vprojection[0] > value){
			current.start = 0;
		}
		for(int i=0; i<vprojection.length; ++i){
			int v = vprojection[i];
			boolean lower = current.start < 0;
			if(value < v && lower){
				current.start = i;
			}else if(value > v && !lower){
				current.end = i;
				peaks.add(current);
				current = new Peak();
			}
		}
		if(current.start >= 0 && current.end < 0){
			current.end = vprojection.length - 1;
			peaks.add(current);
		}
		return peaks;
	}
	
	//float rowValue = 1f;
	Mat drawProjection(Mat plateGrey){
		int[] vprojection = Projection.verticalProjection(plateGrey);
		Mat result = Mat.zeros(model.rows, model.cols, CvType.CV_8UC3);
//		plateColor.copyTo(result);
		int max = plateGrey.height();
//		System.out.println("max value: " + max);
		byte[] pp = {0, 0, 0};
		byte[] pgrey = {0};
		for(int r=0; r<plateGrey.rows(); ++r){
			for(int c=0; c<plateGrey.cols(); ++c){
				plateGrey.get(r, c, pgrey);
				pp[0] = pp[1] = pp[2] = pgrey[0];
				result.put(r, c, pp);
			}
		}
		int remainRows = model.rows - plateGrey.rows();
		float rowValue = max / (float)remainRows;
		byte[] pixel = {(byte)255, 0, 0};
		for(int c=0; c<vprojection.length; ++c){
			int mr = (int)(vprojection[c] / rowValue) + plateGrey.rows();
			if(mr >= model.rows){
				mr = model.rows - 1;
			}
			for(int r=plateGrey.rows(); r<=mr; ++r){
				result.put(r, c, pixel);
			}
		}
		
		for(int i=0; i<plateGrey.rows(); ++i){
			int y = (int)( i / rowValue + plateGrey.rows());
			Core.line(result, new Point(0, y), new Point(result.cols(), y), new Scalar(22, 22, 22));
		}
		return result;
	}
	
	Mat drawRects(List<Rect> rects){
		Mat mat = plateColor.clone();
		Mat extraction = Mat.zeros(plateColor.rows(), plateColor.cols() + 10 * 7, CvType.CV_8UC3);
		int xs=0;
		byte[] pixel = new byte[3];
		for(Rect rect : rects){
			if(rect.x + rect.width > plateColor.cols()){
				rect.width = plateColor.cols() - rect.x;
			}
			Core.rectangle(mat, new Point(rect.x, rect.y + 1), 
					new Point(rect.x + rect.width, rect.y + rect.height - 1), 
					new Scalar(0, 0, 255));
			Mat sub = plateColor.submat(rect);
			for(int r=0; r<sub.rows(); ++r){
				for(int c=0; c<sub.cols(); ++c){
					sub.get(r, c, pixel);
					extraction.put(r, c + xs, pixel);
				}
			}
			xs += sub.cols() + 10;
		}
		model.saveImage("Extracted", extraction);
		return mat;
	}
	
	List<Rect> process(){

//		Model.saveImage("Vertical Projection " + Model.scale, graph);
//		Histogram.draw(plateGrey);
		
//		byte pixel[] = {0, 0, (byte)255};
		int max = plateGrey.height() / 3; //Util.max(vprojection);
		List<Rect> rects = null;
		for(int v=model.getScale(); v<=max; ++v){
			rects = getFontRects(v);
			if(rects.size() == model.fontsCount){
//				Model.yLine = v;
				Rect left = rects.get(0);
				Rect rightRect = rects.get(rects.size() - 1);
				Rect fRegion = new Rect(left.x, 0, rightRect.x + rightRect.width - left.x, plateGrey.height());
				Mat fontRegion = plateGrey.submat(fRegion);
//				Model.tiltDiff = TiltCorrecter.getTiltDiff(fontRegion, v, rightRect.width);
				TiltCorrecter tilter = new TiltCorrecter(fontRegion, v, rightRect.width);
				model.tiltDiff = tilter.bestTilt();
//				if(!tilter.isSplitable()){
//					return Collections.emptyList();
//				}
				
				if(Config.isDebug){
					Mat graph = drawProjection(plateGrey);
					Mat mat = drawRects(rects);
					model.saveImage("result split", mat);
					float rowValue = plateGrey.height() / (float)(model.rows - plateGrey.height());
					int y = (int)( v / rowValue + plateGrey.rows());
					Core.line(graph, new Point(0, y), new Point(graph.cols(), y), new Scalar(0, 0, 255));
					model.saveImage(graph, "Vertical Projection", model.getScale(), rect.x, model.reverseColor());
				}
//				removeLeftEdge(rects.get(0));
//				List<List<Rect>> mergedSubRects = new ArrayList<>();
				for(int i=0; i<model.fontsCount; ++i){
					Rect r = rects.get(i);
					for(Rect mr : mergedRects.keySet()){
						int right = mr.x + mr.width;
						if(r.x < right && r.x + r.width >= right){
//							Model.isMerged[i] = true;
							decideIfMerge(rects, i, mergedRects.get(mr));
//							mergedSubRects.set(i, mergedRects.get(mr));
//							if(i == Model.fontsCount - 1){
//								
//							}
							break;
						}
					}
				}
				return rects;
			}
		}
		if(Config.isDebug){
			Mat graph = drawProjection(plateGrey);
//			Model.saveImage("Vertical Projection " + Model.scale + " " + rect.x, graph);
			model.saveImage(graph, "Vertical Projection", model.getScale(), rect.x, model.reverseColor());
		}
		return Collections.emptyList();
	}
	
	void decideIfMerge(List<Rect> result, int mergeIndex, List<Rect> subRects){
		if(mergeIndex != result.size() - 1){
			return;
		}
		Rect pre = result.get(mergeIndex - 1);
		Rect merged = result.get(mergeIndex);
		Rect sub0 = subRects.get(0);
		int avg = getAvgFontSize(getIntervals(result)) + 1;
		int middlePre = pre.x + pre.width / 2;
		int dMergerd = merged.x + merged.width / 2 - middlePre + 1;
		int middleSub0 = sub0.x + sub0.width / 2;
		int dSub0 = middleSub0 - middlePre;
		if(Math.abs(dMergerd - avg) >= Math.abs(dSub0 - avg)){
			int half = avg / 2 - 1;
			sub0.x = middleSub0 - half;
			sub0.width = half + half;
			result.set(mergeIndex, sub0);
//			Model.isMerged[mergeIndex] = false;
		}
	}
	
//	void process1(){
//		Mat graph = drawProjection(plateGrey, vprojection);
//		
////		Histogram.draw(plateGrey);
//		
//		byte pixel[] = {0, 0, (byte)255};
//		int max = Util.max(vprojection);
//		List<Peak> peaks;
//		for(int v=0; v<max; ++v){
//			peaks = findPeaks(v, vprojection);
//			if(peaks.size() == 7){
//				for(int r=0; r<plateColor.rows(); ++r){
//					for(Peak p : peaks){
//						plateColor.put(r, p.start, pixel);
//						plateColor.put(r, p.end, pixel);
//					}
//				}
//				int y = (int)( v / rowValue);
//				Core.line(graph, new Point(0, y), new Point(graph.cols(), y), new Scalar(0, 0, 255));
//				Model.saveImage("Vertical Projection", graph);
//				break;
//			}
//		}
//		Model.saveImage("Split", plateColor);
//		
//	}
}
