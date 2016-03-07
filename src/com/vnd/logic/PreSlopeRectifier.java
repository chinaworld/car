package com.vnd.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vnd.model.Config;
import com.vnd.model.SubModel;
import com.vnd.util.Util;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class PreSlopeRectifier {
	Rect plate;
	Rect plateArea;
//	Mat binaryPlate;
//	Mat transformedPlate = new Mat();
	int fontHeight;
//	Mat greyPlate;
	Mat greyPlateMat;
//	Mat edgesPlate;
	
	int dh = 0;
	SubModel model;
//	int threshold;
	private List<Rect> fontAreas = new ArrayList<>();
	
//	int tiltDiff = 0;
	
	public PreSlopeRectifier(SubModel model){
		this.model = model;
		this.plate = model.getPlate();
		fontHeight = plate.height + 1;
		this.plateArea = plate.clone();
		int w = plate.width / 7;
		for(int i=0; i<7; ++i){
			Rect rect = new Rect();
			rect.x = w * i;
			rect.width = w;
			rect.y = 0;
			rect.height = plate.height;
			fontAreas.add(rect);
		}
//		tiltDiff = Model.tiltDiff;
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
//		Rect prect = plateArea.clone();
		dh = plateArea.height / 2;
		if(dh > 8){
			dh = 8;
		}
		//dh = 10;
		addHeight();
		for(Rect r : fontAreas){
			r.height = plateArea.height;
		}
		
		preparePlateMats();
	}
	
	void preparePlateMats(){
		model.slopePlateArea = plateArea;
		if(plateArea.height < 20){
			greyPlateMat = model.grey0.submat(plateArea);
		}else{
			greyPlateMat = model.greyImage.submat(plateArea); //use blur image
		}
//		Model.saveImage("Grey Image2", Model.greyImage);
		model.saveImage("Plate Grey Image", greyPlateMat);
		
//		edgesPlate = model.edges.submat(plateArea);
	}
	
	List<Rect> getSubRegions(){
		List<Rect> result = new ArrayList<>();
		int group = 4;
		int rects = 7 - group + 1;
		int last = rects - 1;
		
//		float enPercent = model.getScale() < 2 ? 0.7f : 0.76f;
//		int t = Util.getThreashold(edgesPlate, enPercent);
		Mat binaryEdges = model.binaryEdges.submat(plateArea); //Util.binary(edgesPlate, enPercent, Imgproc.THRESH_BINARY);
		for(int i=0; i<rects; i+=1){
			Rect r0 = fontAreas .get(i);
			Rect r2 = fontAreas.get(i+group-1);
			Rect r = new Rect(r0.x, r0.y, r2.x + r2.width - r0.x, r2.y + r2.height - r0.y);
			if(i == last){
				r.width -= r2.width / 3;
			}
			Mat area = binaryEdges.submat(r);
			model.saveImage(area, "pre slope rectifier");
//			int t = threshold;
//			if(i == 0 && Model.scale < 2){
//				t = Util.getThreashold(edgesPlate, 0.65f);
//			}
//			EdgeRegion2 regionFinder = new EdgeRegion2(Model.scale, area, group, true, fontHeight);
//			Rect[] regions = regionFinder.getRegions();
//			if(regions.length > 0){
//				result.add(regions[0]);
//			}
			Rect rect = getRegion(area, r, group, fontHeight);
			if(rect != null){
				result.add(rect);
			}
		}
		return result;
	}

    List<Rect> filterSubRegions(List<Rect> rects){
        Mat binaryEdges = model.binaryEdges.submat(plateArea);
        Rect rect = getRegion(binaryEdges, plateArea, model.fontsCount, fontHeight);
        if(rect == null){
            return Collections.emptyList();
        }
        if(rect.height < 7){
            return rects;
        }
        List<Rect> result = new ArrayList<>();
        float maxAllowedDiff = rect.height / 7f;
        for(Rect r : rects){
            if(Math.abs(r.height - rect.height) < maxAllowedDiff){
                result.add(r);
            }
        }
        return result;
    }
	
	Rect getRegion(Mat area, Rect areaRect, int group, int fontHeight){
		EdgeRegion2 regionFinder = new EdgeRegion2(model.getScale(), area, group, true, fontHeight-1);
		List<Rect> regions = regionFinder.getRegions();

		regionFinder.markFounds();
		if(regions.size() > 0){

            Rect rect = Util.getLargest(regions);
            if(!model.useColorFilter){
                return rect;
            }
            Mat colorImg = model.colorImage.submat(plateArea);
            if(areaRect != plateArea){
                colorImg = colorImg.submat(areaRect);
            }
            Rect result = ColorFilter.filter(colorImg.submat(rect), rect, model.backColor);
            EdgeRegion2.saveMark("Color Filtered Rect", colorImg, result);
            return result;
//			return regions[0];
		}else{
			return null;
		}
	}
	
	Rect rectify(){
		List<Rect> rects = getSubRegions();
		if(rects.size() < 3){
			return null;
		}
        rects = filterSubRegions(rects);
        if(rects.size() < 2){
            return plate;
        }
        Slope.SlopeInfo[] slopeInfos = Slope.calcSlope(rects, fontAreas);
		float slope = Slope.average(slopeInfos);
		Util.mark("++++++++++++ slope: ", slope);
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
		}else if(absSlope <= Config.ALLOWED_MAX_SLOPE){
			return model.getPlate();
		}
		Mat img = Util.transformSlope(model.colorImage, slope);
		model.setImage(img);
//		transformedPlate = Util.transform(greyPlateMat, slope, model.tiltDiff);
		model.saveImage(img, "transformed with slope", slope);
//		float enPercent = model.getScale() < 2 ? 0.7f : 0.76f;
//		int t = Util.getThreashold(edgesPlate, enPercent);
//		Mat binaryEdges = model.binaryEdges; // Util.binary(model.edges, enPercent, Imgproc.THRESH_BINARY);
		model.saveImage(model.binaryEdges, "pre binary edges");
		EdgeRegion2 er = new EdgeRegion2(model.getScale(), model.binaryEdges, null);
		List<Rect> results = er.getRegions();
		if(results.size() == 0){
			return null;
		}else{

            Rect rect = Util.getLargest(results);

            EdgeRegion2.saveMark("Before color filter, after slope", model.colorImage, rect);
            if(!model.useColorFilter){
                model.setPlate(rect);
                return rect;
            }
            Rect result = ColorFilter.filter(model.colorImage.submat(rect), rect, model.backColor);
//            result.x += rect.x;
//            result.y += rect.y;
            //EdgeRegion2.saveMark("Color Filtered Rect", model.colorImage, result);
			model.setPlate(result);
            model.judgeReverseColor();
			return result;
		}
	}

}
