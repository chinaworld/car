package com.vnd.logic;

import com.vnd.model.*;
import com.vnd.util.Util;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/11/22.
 */
public class SingleCharResizer {
    //    SubModel model;
//    public CharSizeRefine4(SubModel model){
//        this.model = model;
//    }
    static float HW_RATIO = 2 / Config.HEIGHT_SCALE;

    float getHWRatioDiff(RefinedFontInfo[] processed, int fontOneWidth) {
        float total = 0;
        for (RefinedFontInfo p : processed) {
            Mat clipped = p.getClipped();
            if (clipped.cols() < fontOneWidth) {
                continue;
            }
            float r = (float) clipped.rows() / clipped.cols();
            float d = r - HW_RATIO;
            total += d * d;
        }
        return total;
    }

    RefinedFontInfo process(Mat fontImage, boolean useAdaptive, boolean reverseColor, boolean isCn, boolean isLeft, boolean isRight, int ddiff, int averageWidth) {
        float ratio = SubModel.FONT_HEIGHT / (float) fontImage.height();
        RefinedFontInfo processed = new RefinedFontInfo();
        processed.averageWidth = averageWidth;

        if (ratio > 1) {
            Mat igrey = fontImage;
            processed.original = igrey;
            Mat resized = new Mat((int) (igrey.height() * ratio), (int) (igrey.width() * ratio), CvType.CV_8UC1);
            Imgproc.resize(igrey, resized, resized.size());
//				resizes[i] = resized;
            processed.resized = resized;
            processed.reverseColor = reverseColor;
        } else {
            processed.original = fontImage;
            processed.resized = fontImage;
            processed.reverseColor = reverseColor;
        }
        int fontOneWidth = (int) (processed.resized.width() * 0.38);
        Mat resized = processed.resized;
        FontBinaryer3 bineryer = new FontBinaryer3(resized, isCn, reverseColor);
        bineryer.setDiff(bineryer.getDiff() + ddiff);
        Mat mybinary = useAdaptive ? bineryer.adaptiveBinary() : bineryer.binary();
        processed.binary = mybinary;
//			processed[i].binary = Util.adaptiveBinary(resized, 0, Imgproc.THRESH_BINARY);

        int gap = (isCn ? 2 : 1);
        MinMax leftRight = getFontLeftRight(processed.binary, gap, isCn ? 0 : 1);
        processed.setSize(leftRight);
        if(processed.averageWidth > 2 && leftRight.width() > processed.averageWidth){
            if(isLeft){
                leftRight.min += leftRight.width() - processed.averageWidth;
            }else if(isRight){
                leftRight.max -= leftRight.width() - processed.averageWidth;
            }
        }
//		processed[0].topBottom =
        processed.topBottom = getTopBottom(processed.getClipped(), isCn);

        Mat clipped = processed.getClipped();
        int width = clipped.width();
        if (width <= fontOneWidth) {
            processed.result = SubModel.FontOne;
        } else {
            processed.result = new Mat();
            Imgproc.resize(clipped, processed.result, SubModel.FONT_SIZE);
        }
        if (Config.isDebug) {
            MainModel.saveImage(processed.resized, "Resized");
            MainModel.saveImage(processed.binary, "Binary");
            MainModel.saveImage(processed.getClipped(), "clipped");
            MainModel.saveImage(processed.result, "result");
        }

        return processed;
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

    List<MinMax> getVBars(int[] filteredProj) {
        List<MinMax> result = new ArrayList<>();
        for (int i = 0; i < filteredProj.length; ++i) {
            if (filteredProj[i] != 0) {
                MinMax mm = new MinMax(i, i + filteredProj[i]);
                result.add(mm);
                i = mm.max;
            }
        }
        return result;
    }

    MinMax getMaxVBar(List<MinMax> bars) {
        if (bars.isEmpty()) {
            return new MinMax(-1, -1);
        }
        int max = -1;
        MinMax result = null;
        for (MinMax mm : bars) {
            int d = mm.max - mm.min;
            if (d > max) {
                max = d;
                result = mm;
            }
        }
        return result;
    }

    static int HALF_FONT_WIDTH = SubModel.FONT_WIDTH / 2;


    MinMax getCnFontLeftRight(Mat binary, int gap) {
        int diff = 3;
        binary = binary.rowRange(diff, binary.rows() - diff);
        int[] proj = Projection.verticalProjection(binary);
        int[] copy = proj.clone();
//		int[] projection = proj;//Arrays.copyOf(proj, proj.length);
        int[] projection = Projection.filterProjection(proj, 4, HALF_FONT_WIDTH, gap);
        MinMax minmax = MinMax.getMinMaxIndices(projection);
        if (MinMax.getMax(projection) == 0) { // Is number 1
            projection = copy;
            projection = Projection.filterProjection(projection, binary.rows() / 2, 4, gap);
            minmax = MinMax.getCnMinMaxIndices(projection);
        } else {
            int maxRemovable = Math.round(binary.width() * 0.25f);
            if (minmax.min > maxRemovable) {
                int removed[] = Arrays.copyOf(copy, minmax.min);
                removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);

                if (!MinMax.allZero(removed)) {
                    MinMax lr = MinMax.getMinMaxIndices(removed);
                    if (lr.max - lr.min >= 12 || lr.min > 8) {
                        minmax.min = lr.min;
                    }
                }
            }
            int rightRemoved = binary.cols() - minmax.max;
            if (rightRemoved > maxRemovable) {
                int removed[] = Arrays.copyOfRange(copy, minmax.max, binary.cols());
                removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
                if (!MinMax.allZero(removed)) {
                    MinMax lr = MinMax.getMinMaxIndices(removed);
                    if (lr.max - lr.min >= 3) {
                        minmax.max = binary.cols() - lr.max;
                    }
                }
            }
        }

        if (minmax.min >= minmax.max) {
            minmax = new MinMax(0, projection.length - 1);
        }
        return minmax;
    }


    MinMax getFontLeftRight(Mat binary, int gap, int idx) {
        if (idx == 0) {
            return getCnFontLeftRight(binary, gap);
        } else {
            return getEnFontLeftRight(binary, gap, idx);
        }
    }

    MinMax getEnFontLeftRight(Mat binary, int gap, int idx) {
//		Mat binary = mat.rowRange(0, mat.rows() - 1);
        int[] proj = Projection.verticalProjection(binary);
        int[] copy = proj.clone();
//		int[] projection = proj;//Arrays.copyOf(proj, proj.length);
        int[] projection = Projection.filterProjection(proj, 4, HALF_FONT_WIDTH, gap);
        MinMax minmax = MinMax.getMinMaxIndices(projection);
        if (minmax.min >= minmax.max || MinMax.getMax(projection) == 0) { // Is number 1
            projection = copy;
            projection = Projection.filterProjection(projection, binary.rows() / 2, 4, gap);
            if (idx != 0) {
                List<MinMax> bars = getVBars(projection);
                minmax = getMaxVBar(bars);
            } else {
                minmax = MinMax.getCnMinMaxIndices(projection);
            }
        } else if (idx != 0) {
            int maxRemovable = Math.round(binary.width() * 0.25f);
            if (minmax.min > maxRemovable) {
                int removed[] = Arrays.copyOf(copy, minmax.min);
                removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
                if (!MinMax.allZero(removed)) {
                    MinMax lr = MinMax.getMinMaxIndices(removed);
                    if (lr.max - lr.min >= 3) {
                        minmax.min = lr.min;
                    }
                }
            }
            int rightRemoved = binary.cols() - minmax.max;
            if (rightRemoved > maxRemovable) {
                int removed[] = Arrays.copyOfRange(copy, minmax.max, binary.cols());
                removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
                if (!MinMax.allZero(removed)) {
                    MinMax lr = MinMax.getMinMaxIndices(removed);
                    if (lr.max - lr.min >= 3) {
                        minmax.max = binary.cols() - lr.max;
                    }
                }
            }
        } else if (idx == 0) {
            int maxRemovable = Math.round(binary.width() * 0.25f);
            if (minmax.min > maxRemovable) {
                int removed[] = Arrays.copyOf(copy, minmax.min);
                removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);

                if (!MinMax.allZero(removed)) {
                    MinMax lr = MinMax.getMinMaxIndices(removed);
                    if (lr.max - lr.min >= 5 || lr.min > 8) {
                        minmax.min = lr.min;
                    }
                }
            }
            int rightRemoved = binary.cols() - minmax.max;
            if (rightRemoved > maxRemovable) {
                int removed[] = Arrays.copyOfRange(copy, minmax.max, binary.cols());
                removed = Projection.filterProjection(removed, binary.rows() / 2, 2, gap);
                if (!MinMax.allZero(removed)) {
                    MinMax lr = MinMax.getMinMaxIndices(removed);
                    if (lr.max - lr.min >= 3) {
                        minmax.max = binary.cols() - lr.max;
                    }
                }
            }
        }
        if (minmax.min >= minmax.max) {
            minmax = new MinMax(0, projection.length - 1);
        }
        return minmax;
    }
}
