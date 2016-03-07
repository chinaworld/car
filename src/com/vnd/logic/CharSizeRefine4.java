package com.vnd.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vnd.model.Config;
import com.vnd.model.MinMax;
import com.vnd.model.RefinedFontInfo;
import com.vnd.model.SubModel;
import com.vnd.util.Util;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CharSizeRefine4 {
	SubModel model;
	public CharSizeRefine4(SubModel model){
		this.model = model;
	}
	static float HW_RATIO = 2 / Config.HEIGHT_SCALE;
	float getHWRatioDiff(RefinedFontInfo[] processed, int fontOneWidth){
		float total = 0;
		for(RefinedFontInfo p : processed){
			Mat clipped = p.getClipped();
			if(clipped.cols() < fontOneWidth){
				continue;
			}
			float r = (float)clipped.rows() / clipped.cols();
			float d = r - HW_RATIO;
			total += d * d;
		}
		return total;
	}
	
	static int TOO_MUCH_SPACE = 10;
	boolean requireCorrectSlope(RefinedFontInfo[] processes){
		return false;
//		RefinedFontInfo p0 = processes[0];
//		RefinedFontInfo pn = processes[6];
//		return (p0.topBottom.min > TOO_MUCH_SPACE || Model.FONT_HEIGHT - p0.topBottom.max > TOO_MUCH_SPACE
//				|| pn.topBottom.min > TOO_MUCH_SPACE || Model.FONT_HEIGHT - pn.topBottom.max > TOO_MUCH_SPACE);
	}
	
	RefinedFontInfo[] process(Mat[] fontImages, boolean useAdaptive){
//		Model.saveImage("source", Model.colorImage);
		
//		Mat[] trans = new Mat[fontImages.length];
		float ratio = SubModel.FONT_HEIGHT / (float)fontImages[0].height();
//		Mat[] clips = new Mat[Model.fontsCount];
//		Mat[] resizes = fontImages;
		RefinedFontInfo[] processed = new RefinedFontInfo[model.fontsCount];
		
		if(ratio > 1){
			for(int i=0; i<fontImages.length; ++i){
				Mat igrey = fontImages[i];
				processed[i] = new RefinedFontInfo();
				processed[i].original = igrey;
				Mat resized = new Mat((int)(igrey.height() * ratio), (int)(igrey.width() * ratio), CvType.CV_8UC1);
				Imgproc.resize(igrey, resized, resized.size());
//				resizes[i] = resized;
				processed[i].resized = resized;
				processed[i].reverseColor = model.reverseColor();
			}
		}else{
			for(int i=0; i<fontImages.length; ++i){
				processed[i] = new RefinedFontInfo();
				processed[i].original = fontImages[i];
				processed[i].resized = fontImages[i];
				processed[i].reverseColor = model.reverseColor();
			}
		}
        if(model.oneRowPlate){
            processed[0].isLeft = true;
            processed[processed.length - 1].isRight = true;
        }
		int fontOneWidth = (int)(processed[0].resized.width() * 0.38);
		for(int i=0; i<processed.length; ++i){
			Mat resized = processed[i].resized;
			FontBinaryer3 bineryer = new FontBinaryer3(resized, i == 0, model.reverseColor());
			Mat mybinary = useAdaptive ? bineryer.adaptiveBinary() : bineryer.binary();
//			processed[i].binary = mybinary;
//			processed[i].binary = Util.adaptiveBinary(resized, 0, Imgproc.THRESH_BINARY);
			
			int gap = (i == 0 ? 2 : 1);
			List<MinMax> leftRights = getFontLeftRight(mybinary, gap, i);


            if(i == 0){
                List<MinMax> sizes = new ArrayList<>();
                List<Mat> nBinaries = new ArrayList<>();
                for(MinMax minMax : leftRights){
                    if(minMax.min > 4){
                        bineryer = new FontBinaryer3(resized, i == 0, model.reverseColor(), minMax);
                        Mat nBinary = bineryer.binary();
                        model.saveImage(nBinary, "For debug*****************");
                        List<MinMax> subSizes = getFontLeftRight(nBinary, gap, i);
                        sizes.addAll(subSizes);
                        for(int j=0; j<subSizes.size(); ++j){
                            nBinaries.add(nBinary);
                        }
                    }else {
                        sizes.add(minMax);
                        nBinaries.add(mybinary);
                    }
                }
                processed[i].setSizes(sizes, nBinaries);
            }else if(i==model.fontsCount-1){
                List<MinMax> sizes = new ArrayList<>();
                List<Mat> nBinaries = new ArrayList<>();
                for(MinMax minMax : leftRights){
                    if(resized.width() - minMax.max > 4){
                        bineryer = new FontBinaryer3(resized, i == 0, model.reverseColor(), minMax);
                        Mat nBinary = bineryer.binary();
                        List<MinMax> subSizes = getFontLeftRight(nBinary, gap, i);
                        sizes.addAll(subSizes);
                        for(int j=0; j<subSizes.size(); ++j){
                            nBinaries.add(nBinary);
                        }
                    }else {
                        sizes.add(minMax);
                        nBinaries.add(mybinary);
                    }
                }
                processed[i].setSizes(sizes, nBinaries);
            }else{
                List<Mat> nBinaries = new ArrayList<>();
                for(MinMax minMax : leftRights){
                    nBinaries.add(mybinary);
                }
                processed[i].setSizes(leftRights, nBinaries);
            }

//            if((i == 0 && leftRights.min > 4) || (i==model.fontsCount-1 && resized.width() - leftRights.max > 4)){
//                bineryer = new FontBinaryer3(resized, i == 0, model.reverseColor(), leftRights);
////                Mat mybinary = useAdaptive ? bineryer.adaptiveBinary() : bineryer.binary();
//                processed[i].binary = bineryer.binary();
//
//                leftRights = getFontLeftRight(processed[i].binary, gap, i);
//                processed[i].setSize(leftRights);
//            }
		}
		
		List<Integer> fsizes = new ArrayList<>();
		
		for(int i=1, len=model.fontsCount-1; i<len; ++i){
			if(processed[i].getSize().width() > processed[i].resized.width() / 2){
				fsizes.add(processed[i].getSize().width());
			}
		}
		if(fsizes.size() > 1){
			int mean = 0;
			for(int size : fsizes){
				mean += size;
			}
			mean /= fsizes.size();
			mean += 1;
            for(RefinedFontInfo fontInfo : processed){
                fontInfo.averageWidth = mean;
            }
			MinMax leftRight0  = processed[0].getSize();
//			Mat first = clips[0];
			if(leftRight0.width() > mean){
				leftRight0.min += leftRight0.width() - mean;
//				clips[0] = first.colRange(first.cols() - mean, first.cols());
			}
//			Mat last = clips[clips.length - 1];
			MinMax leftRightN  = processed[processed.length-1].getSize();
			if(leftRightN.width() > mean){
				leftRightN.max -= leftRightN.width() - mean;
//				clips[clips.length - 1] = last.colRange(0, mean);
			}
		}

		if(Config.isDebug){
			Util.mark("***********HW Ratio Diff: " + getHWRatioDiff(processed, fontOneWidth));
		}
		if(processed[0].original.height() > Config.CHECK_HW_RATIO_MIN_HEIGHT){
			float rd = getHWRatioDiff(processed, fontOneWidth);
			if(rd > Config.MAX_HW_RATIO_DIFF){
				return new RefinedFontInfo[0];
			}
		}
//		processed[0].topBottom = 
		for(int i=0; i<processed.length; ++i){
			processed[i].topBottom = getTopBottom(processed[i].getClipped(), i == 0);
		}
		
		if(model.slopeRefineCount < 1 && requireCorrectSlope(processed)){
			Util.mark("Post slope refine", model.slopeRefineCount);
			++model.slopeRefineCount;
			PostSlopeRefiner refiner = new PostSlopeRefiner(model);
			Mat[] rawImages = refiner.getFonts();
			if(rawImages.length != model.fontsCount){
				return new RefinedFontInfo[0];
			}
			CharSizeRefine4 sizeRefiner = new CharSizeRefine4(model);
			return sizeRefiner.process(rawImages, useAdaptive);
		}
		
		for(int i=0; i<model.fontsCount; ++i){
			Mat clipped = processed[i].getClipped();
			int width = clipped.width();
//			Mat result;
			if(width <= fontOneWidth){
				processed[i].result = SubModel.FontOne;
			}else{
//				Mat temp = Mat.zeros(clipped.rows(), width + 2, CvType.CV_8UC1);
//				Mat sub = temp.colRange(1, 1 + width);
//				clipped.copyTo(sub);
				processed[i].result = new Mat();
				Imgproc.resize(clipped, processed[i].result, SubModel.FONT_SIZE);
			}
//			Model.saveImage("result " + i, result);
//			trans[i] = result;
		}
		if(Config.isDebug){
			for(int i=0; i<model.fontsCount; ++i){
				model.saveImage(processed[i].resized, "Resized", i);
                model.saveImage(processed[i].binary, "Binary", i);
				model.saveImage(processed[i].getClipped(), "clipped", i);
				model.saveImage(processed[i].result, "result", i, ++SubModel.mark);
			}
		}
		
		model.processed = processed;
		return processed;
	}
	
//	boolean isChuan(int[] proj){
//		List<Peak> peaks = CharExtrator.findPeaks(3, proj);
//		return peaks.size() >= 3;
//	}
	
//	boolean containsBar(int[] proj, int start, int end){
//		int h = Model.FONT_HEIGHT - 1;
//		for(int i=start; i<=end; ++i){
//			if(proj[i] >= h){
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	boolean hasLeftBar(){
//		FontBinaryer2 bineryer = new FontBinaryer2(first, true, 10);
//		Mat binary = bineryer.binary();
//		int[] proj = Projection.verticalProjection(binary);
//		List<Peak> peaks = CharExtrator.findPeaks(3, proj);
//		if(peaks.size() >= 2){
//			Peak p0 = peaks.get(0);
//			return p0.end < proj.length / 3 && containsBar(proj, p0.start, p0.end);
//		}
//		return false;
//	}
//	
//	boolean hasRightBar(){
//		FontBinaryer2 bineryer = new FontBinaryer2(last, true, 20);
//		Mat binary = bineryer.binary();
//		int[] proj = Projection.verticalProjection(binary);
//		List<Peak> peaks = CharExtrator.findPeaks(3, proj);
//		if(peaks.size() >= 2){
//			Peak p0 = peaks.get(peaks.size() - 1);
//			return p0.end > proj.length * 0.66 && containsBar(proj, p0.start, p0.end);
//		}
//		return false;
//	}
//	
	boolean removeVBar(int[] proj, int start, int end){
		int h = SubModel.FONT_HEIGHT - 1;
		int s = proj.length;
		int e = 0;
		for(int i=start; i<end; ++i){
			if(proj[i] >= h){
				if(i<s){
					s = i;
				}
				if(i > e){
					e = i;
				}
			}
		}

		int count = e - s + 1;
		if(count >= 1){
			e += 1;
			for(int k=0; k<=e; ++k){
				proj[k] = 0;
			}
			return true;
		}
		return false;
	}
	MinMax getTopBottom(Mat binary, boolean isCn){
		int[] projection = Projection.horizontalProjection(binary);
        int[] proj = Projection.filterProjection(projection, 4, 4, 1);
        MinMax result;
		if(isCn){
            result = MinMax.getCnMinMaxIndices(proj);
		}else{
            result = MinMax.getMinMaxIndices(proj);
		}
        if(result.min > 10 || binary.height() - result.max > 10){
            projection = Projection.horizontalProjection(binary);
            proj = Projection.filterProjection(projection, 2, 4, 3);
            if(isCn){
                result = MinMax.getCnMinMaxIndices(proj);
            }else{
                result = MinMax.getMinMaxIndices(proj);
            }
        }
        return result;
	}
//	MinMax getTopBottom(Mat binary){
//		int[] proj = Projection.horizontalProjection(binary);
//		proj = Projection.filterProjection(proj, 4, 4, 1);
//		return MinMax.getMinMaxIndices(proj);
//	}
	
//	int countVBars(int[] projection){
//		
//	}
	List<MinMax> getVBars(int[] filteredProj){
		List<MinMax> result = new ArrayList<>();
		for(int i=0; i<filteredProj.length; ++i){
			if(filteredProj[i] != 0){
				MinMax mm = new MinMax(i, i + filteredProj[i]);
				result.add(mm);
				i = mm.max;
			}
		}
		return result;
	}
	
	MinMax getMaxVBar(List<MinMax> bars){
		if(bars.isEmpty()){
			return new MinMax(-1, -1);
		}
		int max = -1;
		MinMax result = null;
		for(MinMax mm : bars){
			int d = mm.max - mm.min;
			if(d > max){
				max = d;
				result = mm;
			}
		}
		return result;
	}
	
	static int HALF_FONT_WIDTH = SubModel.FONT_WIDTH / 2;

    List<MinMax> getCnFontLeftRight(Mat binary, int gap) {
        int diff = 3;
        Mat binary1 = binary.rowRange(diff, binary.rows() - diff);
        int[] proj1 = Projection.verticalProjection(binary1);
        List<MinMax> result = getCnFontLeftRight(binary1, proj1, gap);

        int[] proj2 = Projection.verticalProjection(binary);
        boolean hasRemoved = removeVBar(proj2, 0, proj2.length / 3);
        if(hasRemoved) {
            List<MinMax> result2 = getCnFontLeftRight(binary, proj2, gap);
            result.addAll(result2);
        }
        return result;
    }
	

	List<MinMax> getCnFontLeftRight(Mat binary, int[] proj, int gap){
        List<MinMax> result = new ArrayList<>();
//		int diff = 3;
//		binary = binary.rowRange(diff, binary.rows() - diff);
//		int[] proj = Projection.verticalProjection(binary);
		int[] copy = proj.clone();
//		int[] projection = proj;//Arrays.copyOf(proj, proj.length);
		int[] projection = Projection.filterProjection(proj, 4, HALF_FONT_WIDTH, gap);
		MinMax minmax = MinMax.getMinMaxIndices(projection);
		if(MinMax.getMax(projection) == 0){ // Is number 1
			projection = copy;
			projection = Projection.filterProjection(projection, binary.rows() / 2, 4, gap);
			minmax = MinMax.getCnMinMaxIndices(projection);
		}else{
			int maxRemovable = Math.round(binary.width() * 0.25f);
			if(minmax.min > maxRemovable){
				int removed[] = Arrays.copyOf(copy, minmax.min);
				removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);

				if(!MinMax.allZero(removed)){
					MinMax lr = MinMax.getMinMaxIndices(removed);
					if(lr.max - lr.min >= 12 || lr.min > 8){
                        result.add(minmax.clone());
						minmax.min = lr.min;
					}
				}
			}
			int rightRemoved = binary.cols() - minmax.max;
			if(rightRemoved > maxRemovable){
				int removed[] = Arrays.copyOfRange(copy, minmax.max, binary.cols());
				removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
				if(!MinMax.allZero(removed)){
					MinMax lr = MinMax.getMinMaxIndices(removed);
					if(lr.max - lr.min >= 3){
						minmax.max = binary.cols() - lr.max;
					}
				}
			}
		}

		if(minmax.min >= minmax.max){ 
			minmax = new MinMax(0, projection.length - 1);
		}
        result.add(0, minmax);
		return result;
	}
	

	List<MinMax> getFontLeftRight(Mat binary, int gap, int idx){
		if(idx == 0){
			return getCnFontLeftRight(binary, gap);
		}else{
			return getEnFontLeftRight(binary, gap, idx);
		}
	}
    List<MinMax> getEnFontLeftRight(Mat binary, int gap, int idx){
        List<MinMax> result = new ArrayList<>();
//		Mat binary = mat.rowRange(0, mat.rows() - 1);
		int[] proj = Projection.verticalProjection(binary);
		int[] copy = proj.clone();
//		int[] projection = proj;//Arrays.copyOf(proj, proj.length);
		int[] projection = Projection.filterProjection(proj, 4, HALF_FONT_WIDTH, gap);
		MinMax minmax = MinMax.getMinMaxIndices(projection);
		if(minmax.min >= minmax.max || MinMax.getMax(projection) == 0){ // Is number 1
			projection = copy;
			projection = Projection.filterProjection(projection, binary.rows() / 2, 4, gap);
			if(idx != 0){
				List<MinMax> bars = getVBars(projection);
				minmax = getMaxVBar(bars);
			}else{
				minmax = MinMax.getCnMinMaxIndices(projection);
			}
		}else if(idx != 0 && idx != model.fontsCount - 1){
			int maxRemovable = Math.round(binary.width() * 0.25f);
			if(minmax.min > maxRemovable){
				int removed[] = Arrays.copyOf(copy, minmax.min);
				removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
				if(!MinMax.allZero(removed)){
					MinMax lr = MinMax.getMinMaxIndices(removed);
					if(lr.max - lr.min >= 3){
						minmax.min = lr.min;
					}
				}
			}
			int rightRemoved = binary.cols() - minmax.max;
			if(rightRemoved > maxRemovable){
				int removed[] = Arrays.copyOfRange(copy, minmax.max, binary.cols());
				removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
				if(!MinMax.allZero(removed)){
					MinMax lr = MinMax.getMinMaxIndices(removed);
					if(lr.max - lr.min >= 3){
						minmax.max = binary.cols() - lr.max;
					}
				}
			}
		}else if(idx == 0){
			int maxRemovable = Math.round(binary.width() * 0.25f);
			if(minmax.min > maxRemovable){
				int removed[] = Arrays.copyOf(copy, minmax.min);
				removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);

				if(!MinMax.allZero(removed)){
					MinMax lr = MinMax.getMinMaxIndices(removed);
					if(lr.max - lr.min >= 5 || lr.min > 8){
						minmax.min = lr.min;
					}
				}
			}
			int rightRemoved = binary.cols() - minmax.max;
			if(rightRemoved > maxRemovable){
				int removed[] = Arrays.copyOfRange(copy, minmax.max, binary.cols());
				removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
				if(!MinMax.allZero(removed)){
					MinMax lr = MinMax.getMinMaxIndices(removed);
					if(lr.max - lr.min >= 3){
						minmax.max = binary.cols() - lr.max;
					}
				}
			}
		}
		if(minmax.min >= minmax.max){ 
			minmax = new MinMax(0, projection.length - 1);
		}
		result.add(minmax);
        return result;
	}
	
	
	
	static int getThreashold(Mat grey, int idx){
		float pct = idx == 0 ? 0.5f : 0.65f;
		return Util.getThreashold(grey, pct);
	}
}
