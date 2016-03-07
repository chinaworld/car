package com.vnd.logic;

import com.vnd.model.LinePixels;
import com.vnd.model.MainModel;
import com.vnd.util.Util;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Administrator on 2015/11/17.
 */
public class PlateEdgeRemover {
    private Rect plateRect;
    private Mat plateBinaryImage;

    int maxDistance = 5;

    public PlateEdgeRemover(Rect plateRect, Mat carGreyImage){
        maxDistance = plateRect.height / 3;
        this.plateRect = plateRect;
        Mat plateGreyImg = carGreyImage.submat(plateRect);
        int wd = plateRect.width / 3;
        Mat sample = plateGreyImg.colRange(wd, plateRect.width - wd);
        int threshold = Util.getThreashold(sample, 0.7f);
        plateBinaryImage = Util.binary(plateGreyImg, threshold, Imgproc.THRESH_BINARY);
    }

    void removeEdge(){
        Mat toImg = new Mat(plateRect.size(), CvType.CV_8UC1);
        int height = plateRect.height / 3;
        enhanceEdge(plateBinaryImage.rowRange(0, height), toImg.rowRange(0, height));
        int start = plateRect.height - height;
        enhanceEdge(plateBinaryImage.rowRange(start, plateRect.height), toImg.rowRange(start, plateRect.height));

        MainModel.saveImage(plateBinaryImage, "***Plate Binary Image");
        MainModel.saveImage(toImg, "***Enhanced Binary Image");
    }

    void enhanceEdge(Mat edgeImage, Mat toImg){
        byte[] values = new byte[(int)edgeImage.total()];
        edgeImage.get(0, 0, values);
        for(int i=0; i<edgeImage.height(); ++i){
            int start = i*plateRect.width;
            LinePixels linePixels = new LinePixels(values, start, start + plateRect.width);
            int pos;
            int pre = start -1;
            while((pos = linePixels.nextPixelPos()) >= 0){
                if(pos - pre < maxDistance) {
                    for (int p = pre + 1; p < pos; ++p) {
                        values[p] = (byte) 255;
                    }
                    pre = pos;
                }else{
                    break;
                }
            }
        }
        toImg.put(0, 0, values);
    }
}
