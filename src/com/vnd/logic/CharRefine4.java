package com.vnd.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vnd.model.SubModel;
import com.vnd.util.Util;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class CharRefine4 {
//	Rect plate;
	Rect plateArea;
	List<Rect> fontAreas;
//	Mat binaryPlate;
	Mat transformedPlate = new Mat();
	int fontHeight;
//	Mat greyPlate;
	Mat greyPlateMat;
	Mat edgesPlate;
	
	int dh = 0;
	SubModel model;
//	int threshold;
	
//	int tiltDiff = 0;
	
	public CharRefine4(SubModel model, List<Rect> fonts){
		this.model = model;
		fontHeight = model.getPlate().height + 1;
		this.plateArea = model.getPlate().clone();
		fontAreas = new ArrayList<>();
		for(Rect f : fonts){
			fontAreas.add(f.clone());
		}
//		tiltDiff = Model.tiltDiff;
		process();
	}
	
//	int regetCount = 0;
//	Mat[] reGetfonts(float slope){
//		addHeight();
//		if(plateArea.width < 20){
//			greyPlateMat = Model.grey0.submat(plateArea);
//		}else{
//			greyPlateMat = Model.greyImage.submat(plateArea); //use blur image
//		}
////		Model.saveImage("Grey Image2", Model.greyImage);
//		Model.saveImage("Plate Grey Image", greyPlateMat);
//		transformSlope(slope);
//		greyPlateMat = transformedPlate;
//		edgesPlate = EdgeRegion2.hdiff(greyPlateMat); // Model.edges.submat(plateArea);
//		return getFonts();
//	}
	
//	void transformSlope(float slope){
////		float slope = calcSlope();
//		if(slope == 0){
//			transformedPlate = greyPlateMat;
//			return;
//		}
//		Point ps0 = null, ps1 = null, ps2 = null;
//		Point pd0 = null, pd1 = null, pd2 = null;
//		if(slope < 0){
//			ps2 = new Point(greyPlateMat.cols() - 1, 0);
//			ps1 = new Point(0, greyPlateMat.rows() - 1);
//			double y = ps2.y - slope * ps2.x;
//			ps0 = new Point(0, y);
//			
//			pd0 = ps0.clone();
//			pd1 = ps1.clone();
//			pd2 = new Point(ps2.x, ps0.y);
//		}else if(slope > 0){
//			ps0 = new Point(0, 0);
//			ps2 = new Point(greyPlateMat.cols() - 1, greyPlateMat.rows() - 1);
//			double y = ps2.y - slope * ps2.x;
//			ps1 = new Point(0, y);
//			
//			pd0 = ps0.clone();
//			pd1 = ps1.clone();
//			pd2 = new Point(ps2.x, ps1.y);
//		}
//		
//		Mat warpMat = Imgproc.getAffineTransform(new MatOfPoint2f(ps0, ps1, ps2), new MatOfPoint2f(pd0, pd1, pd2));
//		Imgproc.warpAffine(greyPlateMat, transformedPlate, warpMat, greyPlateMat.size());
//		Model.saveImage("Transform slope only", transformedPlate);
//	}
	
	void addHeight(){
		if(plateArea.y - dh < 0){
			dh = plateArea.y;
		}
		plateArea.y -= dh;
		plateArea.height += 2 * dh;
		if(plateArea.y + plateArea.height >= model.greyImage.height()){
			plateArea.height = model.greyImage.height() - plateArea.y - 1;
		}
	}
	
	private void process(){
//		Rect prect = plateArea.clone();
		dh = plateArea.height / 2;
		if(dh > 8){
			dh = 8;
		}
		//dh = 10;
		addHeight();
		
		int[] widths = new int[fontAreas.size()];
		int i=0;
		for(Rect r : fontAreas){
			r.y = 0;
			r.height = plateArea.height;
//			r.x += plateArea.x;
			widths[i++] = r.width;
		}
		
		Arrays.sort(widths);
		int mid = widths.length / 2;
		int width = (widths[mid] + widths[mid + 1]) / 2 + 1;
		Rect left = fontAreas.get(0);
		if(left.width < width){
			int d = width - left.width;
			if(plateArea.x >= d){
				left.x -= d;
				plateArea.x -= d;
				plateArea.width += d + 1;
				for(Rect r : fontAreas){
					r.x += d;
				}
			}
		}else{
			left.x += left.width - width;
		}
		left.width = width;

		Rect right = fontAreas.get(widths.length - 1);
//		right.width -= 1;
//		if(width > right.width){
//			plateArea.width += width - right.width + 1;
//		}
		if(right.width >= width){
			right.width = width;
		}else{
			int dd = width - right.width;
			right.width += dd;
			plateArea.width += dd;
		}

		int shift = left.x - 1;
		if(shift < 0){
			shift = 0;
		}
		plateArea.x += shift;
		for(Rect fr : fontAreas){
			fr.x -= shift;
		}
		plateArea.width = right.x + right.width + 2;
		if(plateArea.width + plateArea.x >= model.greyImage.width()){
			plateArea.width = model.greyImage.width() - plateArea.x - 1;
		}
		
		int tooRight = right.x + right.width - plateArea.width;
		if(tooRight > 0){
			right.width -= tooRight;
		}
		preparePlateMats();
		
//		prect.x = plateArea.x;
//		prect.width  = plateArea.width;
//		greyPlate = Model.greyImage.submat(plateArea);
//		Size size = new Size(3, 3);
//		Imgproc.GaussianBlur(greyPlate, greyPlate, size, 0, 0, Imgproc.BORDER_DEFAULT);
		
//		threshold = Util.getThreashold(edgesPlate, 0.76f);
//		EdgeRegion2 regionFinder = new EdgeRegion2(Model.scale, edgesPlate, threshold);
//		Rect[] regions = regionFinder.getRegions();
//		if(regions.length > 0){
//			Rect region = regions[0];
//			System.out.println("initial font height: " + fontHeight);
//			int nheight = region.height + 1;
//			if(fontHeight >= nheight * 2){
//				correct = false;
//			}
//			fontHeight = nheight;
//			System.out.println("refined font height: " + fontHeight);
//		}else{
//			correct = false;
//		}
//		Imgproc.equalizeHist(greyPlate, greyPlate);
//		binaryPlate = Util.binary(greyPlate, getThreashold(greyPlate));
//		Model.saveImage("Plate Binary Image", binaryPlate);
	}
	
	void preparePlateMats(){
		model.slopePlateArea = plateArea;
		if(fontAreas.get(0).width < 20){
			greyPlateMat = model.grey0.submat(plateArea);
		}else{
			greyPlateMat = model.greyImage.submat(plateArea); //use blur image
		}
//		Model.saveImage("Grey Image2", Model.greyImage);
		model.saveImage("Plate Grey Image", greyPlateMat);
		
		edgesPlate = model.edges.submat(plateArea);
	}
	
	boolean correct = true;
	public boolean isCorrect(){
		return correct;
	}
	
//	static class SubRegion{
//		Rect rect;
//		int diffX;
//		public SubRegion(Rect rect, int diffX) {
//			super();
//			this.rect = rect;
//			this.diffX = diffX;
//		}
//	}
	
//	Mat binaryEdgesPlate(){
//		Mat cnFonts = edgesPlate.submat(fontAreas.get(0));
//		Mat enFonts = edgesPlate.submat(0, edgesPlate.rows(), cnFonts.cols(), edgesPlate.cols());
//		float enPercent = Model.scale < 2 ? 0.7f : 0.76f;
//		int enT = Util.getThreashold(edgesPlate, enPercent);
//	}
	
	List<Rect> getSubRegions(){
		List<Rect> result = new ArrayList<>();
		int group = 4;
		int rects = 7 - group + 1;
		int last = rects - 1;
		
//		float enPercent = model.getScale() < 2 ? 0.7f : 0.76f;
//		int t = Util.getThreashold(edgesPlate, enPercent);
		Mat binaryEdges = model.binaryEdges.submat(plateArea);//Util.binary(edgesPlate, enPercent, Imgproc.THRESH_BINARY);
		for(int i=0; i<rects; i+=1){
			Rect r0 = fontAreas.get(i);
			Rect r2 = fontAreas.get(i+group-1);
			Rect r = new Rect(r0.x, r0.y, r2.x + r2.width - r0.x, r2.y + r2.height - r0.y);
			if(i == last){
				r.width -= r2.width / 3;
			}
			Mat area = binaryEdges.submat(r);
//			int t = threshold;
//			if(i == 0 && Model.scale < 2){
//				t = Util.getThreashold(edgesPlate, 0.65f);
//			}
//			EdgeRegion2 regionFinder = new EdgeRegion2(Model.scale, area, group, true, fontHeight);
//			Rect[] regions = regionFinder.getRegions();
//			if(regions.length > 0){
//				result.add(regions[0]);
//			}
			Rect rect = getRegion(area, group, fontHeight);
			if(rect != null){
				result.add(rect);
			}
		}
		return result;
	}
	
	Rect getRegion(Mat area, int group, int fontHeight){
		EdgeRegion2 regionFinder = new EdgeRegion2(model.getScale(), area, group, true, fontHeight-1);
		List<Rect> regions = regionFinder.getRegions();
		regionFinder.markFounds();
		if(regions.size() > 0){

            Rect rect = Util.getLargest(regions);
            if(!model.useColorFilter){
                return rect;
            }
            Mat colorImg = model.colorImage.submat(plateArea);
            return ColorFilter.filter(colorImg.submat(rect), rect, model.backColor);

//			return regions[0];
		}else{
			return null;
		}
	}
	
	static Mat[] emptyFonts = new Mat[0];
	Mat[] getFonts(){
		List<Rect> rects = getSubRegions();
		if(rects.size() < 3){
			return emptyFonts;
		}
		Slope.SlopeInfo[] slopeInfos = Slope.calcSlope(rects, fontAreas);
		float slope = Slope.average(slopeInfos);
		Util.mark("++++++++++++ slope: ", slope);
		model.slope = slope;
//		if(Math.abs(slope) > 0.05 && regetCount < 1){
//			Util.mark("++++++++++++----------------+++++++ re getting fonts: ", slope);
//			++regetCount;
//			return reGetfonts(slope);
//		}
//		transform(slope);
		float absSlope = Math.abs(slope);
		if(absSlope > 0.09){
			addHeight();
			if(absSlope > 0.19){
				addHeight();
			}
			preparePlateMats();
		}
		transformedPlate = Util.transform(greyPlateMat, slope, model.tiltDiff);
		model.saveImage(transformedPlate, "transformed", slope, model.tiltDiff);
		model.finalPlate = transformedPlate;
		int topY, height;
		if(Math.abs(slope) > 0.06){
			float enPercent = model.getScale() < 2 ? 0.7f : 0.76f;
//			int t = Util.getThreashold(edgesPlate, enPercent);
			Mat edges = EdgeRegion2.hdiff(transformedPlate);
			Mat binaryEdges = Util.binary(edges, enPercent, Imgproc.THRESH_BINARY);
			Rect region = getRegion(binaryEdges, 7, -1);
			if(region == null){
				return emptyFonts;
			}
			topY = region.y;
			height = region.height;
		}else{
			List<Point> tops = new ArrayList<>();
			List<Point> bottoms = new ArrayList<>();
			for(Slope.SlopeInfo si : slopeInfos){
				if(si.isTop){
					tops.add(si.p0);
					tops.add(si.p1);
				}else{
					bottoms.add(si.p0);
					bottoms.add(si.p1);
				}
			}
	//		Point[] tops = sloper.getSelectedTops();
	//		Point[] bottoms = sloper.getSelectedBottoms();
			if(tops.isEmpty() || bottoms.isEmpty()){
				return emptyFonts;
			}
			topY = calcY(slope, tops);
			int bottomY = calcY(slope, bottoms);
			height = bottomY - topY;
//			if(Math.abs(slope) > 0.07){
//				height += 2;
//				if(topY + height > greyPlateMat.rows()){
//					height -= greyPlateMat.rows() - topY;
//				}
//			}
			if(height < 5){
				return emptyFonts;
			}
		}
		Mat[] result = new Mat[fontAreas.size()];
//		Model.plateRegion = transformedPlate;
//		Rect r0 = fontAreas.get(0);
//		Rect rn = fontAreas.get(fontAreas.size() - 1);
////		Model.plateRect = new Rect(f0.x, topY, fn.x + fn.width - f0.x, bottomY - topY);
////		Model.fonts = new Rect[fontAreas.size()];
//		r0.y = topY - 3;
//		if(r0.y < 0){
//			r0.y = 0;
//		}
//		r0.height += 6;
//		if(r0.y + r0.height >= transformedPlate.height()){
//			r0.height = transformedPlate.height() - r0.y;
//		}
//		rn.y = r0.y;
//		rn.height = r0.height;
//		Mat last = transformedPlate.submat(rn);
//		Mat first = transformedPlate.submat(r0);
		for(int i=0; i<model.fontsCount; ++i){
			Rect r = fontAreas.get(i).clone();
			r.y = topY;
			r.height = height;
//			Model.fonts[i] = r;
//			if(i == 0){
//				removeLeftBar(r);
//			}else if(i == result.length - 1){
//				removeRightBar(r);
//			}
			model.fontRects[i] = r;
			result[i] = transformedPlate.submat(r);
			model.saveImage("Final Splited " + i, result[i]);
		}
		return result;
	}
	
	int calcY(float slope, List<Point> points){
		int[] ys = new int[points.size()];
		for(int i=0; i<ys.length; ++i){
			Point p = points.get(i);
			ys[i] = Math.round((float)p.y - slope * (float)p.x);
		}
		
		int result = Util.simpleaverage(ys);
		if(result < 0){
			result = 0;
		}else if(result >= greyPlateMat.rows() - 1){
			result = greyPlateMat.rows() - 2;
		}
		return result;
	}
	
	Mat[] getFonts2(){
		Mat[] result = new Mat[fontAreas.size()];
		for(int i=0; i<result.length; ++i){
			Rect r = fontAreas.get(i);
			result[i] = greyPlateMat.submat(r);
			model.saveImage("Final Splited " + i, result[i]);
		}
		return result;
	}

}
