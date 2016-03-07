package com.vnd.model;

import com.vnd.model.MinMax;
import com.vnd.model.SubModel;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class RefinedFontInfo {
    public Mat original;
    public Mat resized;
    //		private Mat clipped = null;
    private MinMax size = null;
    public Mat result;
    public Mat binary;
    private List<Mat> optionalResults;
    private List<MinMax> optionalSizes;
    private List<Mat> optionalBinaries;
    public MinMax topBottom = null;
    public boolean reverseColor = false;
    public int averageWidth = 0;
    public boolean isLeft = false;
    public boolean isRight = false;

    private Mat resizeClipped = null;

    public Mat getClipped() {
        return getClipped(binary, size);
    }

    public Mat getClipped(Mat binary, MinMax size) {
//			if(clipped == null){
////				clipped = binary.colRange(size.min, size.max + 1);
//				clipped = binary.submat(topBottom.min, topBottom.max + 1, size.min, size.max + 1);
//			}
//			return clipped;

        if (topBottom != null && size != null && topBottom.max > topBottom.min) {
            return binary.submat(topBottom.min, topBottom.max + 1, size.min, size.max + 1);
        } else if (size != null) {
            return binary.colRange(size.min, size.max + 1);
        } else {
            return binary;
        }
    }

    public List<Mat> getOptionalResults(){
        if(optionalResults == null){
            optionalResults = new ArrayList<>();
            for(int i=0; i<optionalBinaries.size(); ++i){
                Mat clip = getClipped(optionalBinaries.get(i), optionalSizes.get(i));

                Mat resized = new Mat();
                Imgproc.resize(clip, resized, SubModel.FONT_SIZE);
                optionalResults.add(resized);
            }
        }
        return optionalResults;
    }

    public Mat getResizeClipped() {
        if (resizeClipped == null) {
            Mat tmp = resized.colRange(size.min, size.max + 1);
            resizeClipped = new Mat();
            Imgproc.resize(tmp, resizeClipped, SubModel.FONT_SIZE);
        }
        return resizeClipped;
    }

    public MinMax getSize() {
        return size;
    }

    public void setSize(MinMax size) {
        if (size != null && size.max >= binary.width()) {
            size.max = binary.width() - 1;
        }
        this.size = size;
    }

    public void setSizes(List<MinMax> sizes, List<Mat> binaries){
        if(sizes.size() == 0 || binaries.size() == 0 || sizes.size() != binaries.size()){
            throw new RuntimeException("Set sizes error");
        }
        if(sizes.size() == 1){
            binary = binaries.get(0);
            setSize(sizes.get(0));
            optionalSizes = new ArrayList<>();
            optionalBinaries = new ArrayList<>();;
        }else if(sizes.size() > 1){
            binary = binaries.get(0);
            setSize(sizes.get(0));
            optionalSizes = sizes.subList(1, sizes.size());
            optionalBinaries = binaries.subList(1, binaries.size());
        }
    }
}