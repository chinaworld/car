package com.vnd.logic;

import java.util.ArrayList;
import java.util.List;

import com.vnd.model.SubModel;
import com.vnd.util.Util;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

public class PostSlopeRefiner {
	Rect plateArea;
	List<Rect> fontAreas;
	Mat transformedPlate;
	int fontHeight;
	Mat greyPlateMat;
	Mat edgesPlate;
	
	int dh = 0;
	private SubModel model;
	
	public PostSlopeRefiner(SubModel model){
		this.model = model;
		fontHeight = model.fontRects[0].height;
		this.plateArea = model.slopePlateArea.clone();
		fontAreas = new ArrayList<>();
		for(Rect f : model.fontRects){
			fontAreas.add(f.clone());
		}
		process();
	}
	
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
		dh = plateArea.height / 2;
		if(dh > 8){
			dh = 8;
		}
		addHeight();
		
		if(fontHeight < 40){
			greyPlateMat = model.grey0.submat(plateArea);
		}else{
			greyPlateMat = model.greyImage.submat(plateArea); //use blur image
		}
		model.saveImage("Post Plate Grey Image", greyPlateMat);
		greyPlateMat = Util.transform(greyPlateMat, model.slope, model.tiltDiff);
		model.saveImage("Post Plate after transform", greyPlateMat);
		edgesPlate = EdgeRegion2.hdiff(greyPlateMat);
		for(Rect r : fontAreas){
			r.y = 0;
			r.height = greyPlateMat.height();
		}
	}
	
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
			EdgeRegion2 regionFinder = new EdgeRegion2(model.getScale(), area, group, true, -1);
			List<Rect> regions = regionFinder.getRegions();
			if(regions.size() > 0){
                Rect rect = regions.get(0);

                if(!model.useColorFilter){
                    result.add(rect);
                }else {
                    Mat colorImg = model.colorImage.submat(plateArea);
                    result.add(ColorFilter.filter(colorImg.submat(rect), rect, model.backColor));
                }
//				result.add(regions[0]);
			}
		}
		return result;
	}

    void filterRects(List<Rect> rects){

    }
	
	static Mat[] emptyFonts = new Mat[0];
	Mat[] getFonts(){
		List<Rect> rects = getSubRegions();
		if(rects.size() < 3){
			return emptyFonts;
		}
		Slope.SlopeInfo[] slopeInfos = Slope.calcSlope(rects, fontAreas);
		float slope = Slope.average(slopeInfos);
        float absSlope = Math.abs(slope);
        if(absSlope < 0.01){
            Mat[] result = new Mat[fontAreas.size()];
            for(int i=0; i<fontAreas.size(); ++i){
                result[i] = greyPlateMat.submat(fontAreas.get(i));
                model.saveImage("Final Splited " + i, result[i]);
            }
            return result;
        }
		Util.mark("Post ++++++++++++ slope: ", slope);
		transformedPlate = Util.transformSlope(greyPlateMat, slope);
		model.saveImage(transformedPlate, "Post transformed final");
		model.finalPlate = transformedPlate;
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
		int topY = calcY(slope, tops);
		int bottomY = calcY(slope, bottoms);
		int height = bottomY - topY;
		if(height < 5){
			return emptyFonts;
		}
		Mat[] result = new Mat[fontAreas.size()];
		for(int i=0; i<fontAreas.size(); ++i){
			Rect r = fontAreas.get(i).clone();
			r.y = topY;
			r.height = height;
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
