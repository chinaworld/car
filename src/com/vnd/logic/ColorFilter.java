package com.vnd.logic;

import com.vnd.model.Config;
import com.vnd.model.MainModel;
import com.vnd.util.ColorUtil;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/11/9.
 */
public class ColorFilter {
    private Mat colorImage;
    private List<Rect> rects;
    private List<Rect> refined = new ArrayList<>();
    private ColorUtil.Color[] colors;

    List<Rect> resultRects;
    List<Rect> originalRects;
    List<ColorUtil.Color> resultColors;

    private boolean hasColorPlate = false;

//    static ColorUtil.Color[] getColors(Rect[] rects, Mat image){
//        ColorUtil.Color[] result = new ColorUtil.Color[rects.length];
//        for(int i=0; i<result.length; ++i){
//            result[i] = ColorUtil.getColor(image.submat(rects[i]));
//        }
//        return result;
//    }

    public ColorFilter(Mat colorImage, List<Rect> rects) {
        this.colorImage = colorImage;
        this.rects = rects;
        colors = new ColorUtil.Color[rects.size()];
    }

    public List<Rect> getResultRects() {
        return resultRects;
    }

    public List<Rect> getOriginalRects(){
        return originalRects;
    }

    public List<ColorUtil.Color> getResultColors() {
        return resultColors;
    }

    private static float MIN_COLOR_PERCENT = 0.4f;

    public static Rect filter(Mat colorImage, Rect rect, ColorUtil.Color color){
        ColorUtil.IsColor colorJudge = ColorUtil.getColorJudge(color);
        if(colorJudge == null){
            return rect;
        }
        return filter(colorImage, rect, colorJudge);
    }

    private static Rect filter(Mat colorImage, Rect rect, ColorUtil.IsColor colorJudge){
//        if(color == ColorUtil.Color.UNKNOWN){
//            return r;
//        }
//        ColorUtil.IsColor colorJudge = ColorUtil.getColorJudge(color);
        markColor(colorImage, colorJudge);

        int top = 0;
        for(int i=0, limit=rect.height; i<limit; ++i){
            Mat rowMat = colorImage.row(i); //.rowRange(i, i+2);
            if(colorJudge.isColor(rowMat, MIN_COLOR_PERCENT)){
                top = i;
                break;
            }
        }
        int bottom = rect.height - 1;
        for(int i=bottom; i>0; --i){
            Mat rowMat = colorImage.row(i); //.rowRange(i, i+2);
            if(colorJudge.isColor(rowMat, MIN_COLOR_PERCENT)){
                bottom = i;
                break;
            }
        }
        int left = 0;
        for(int i=0; i<rect.width; ++i){
            Mat colMat = colorImage.col(i);
            if(colorJudge.isColor(colMat, 0.2f)){
                left = i;
                break;
            }
        }
        int right = rect.width - 1;
        for(int i=right; i>0; --i){
            Mat colMat = colorImage.col(i);
            if(colorJudge.isColor(colMat, 0.4f)){
                right = i;
                break;
            }
        }
        int width = right - left + 1;
        int height = bottom - top + 1;
        if(width < 5 || height < 5){
            return rect;
        }
        return new Rect(left + rect.x, top + rect.y, width, height);
    }



//    private void getColor(int idx){
//        Rect rect = rects.get(idx);
//        Mat img = colorImage.submat(rect);
//
////        Mat small = new Mat();
////        float ratio = rect.height / 5f;
////        Imgproc.resize(img, small, new Size(rect.width / ratio, 5));
////        MainModel.saveImage(small, "Resized to small");
////
////        Mat large = new Mat();
////        Imgproc.resize(small, large, rect.size());
////        MainModel.saveImage(large, "Resized to large");
//
//        ColorUtil.IsColor colorJudge;
//        if(ColorUtil.isRegionBlue(img)){
//            colorJudge = ColorUtil.IsBlue.instance;
//            colors[idx] = ColorUtil.Color.BLUE;
//            hasColorPlate = true;
//        }else if(ColorUtil.isRegionYellow(img)){
//            colorJudge = ColorUtil.IsYellow.instance;
//            colors[idx] = ColorUtil.Color.YELLOW;
//            hasColorPlate = true;
//        }else{
//            colors[idx] = ColorUtil.Color.UNKNOWN;
//        }
//
//    }

    private Rect filter(int idx){
        Rect rect = rects.get(idx);
        Mat img = colorImage.submat(rect);

//        Mat small = new Mat();
//        float ratio = rect.height / 5f;
//        Imgproc.resize(img, small, new Size(rect.width / ratio, 5));
//        MainModel.saveImage(small, "Resized to small");
//
//        Mat large = new Mat();
//        Imgproc.resize(small, large, rect.size());
//        MainModel.saveImage(large, "Resized to large");

        ColorUtil.IsColor colorJudge;
        if(ColorUtil.isRegionBlue(img)){
            colorJudge = ColorUtil.IsBlue.instance;
            colors[idx] = ColorUtil.Color.BLUE;
            hasColorPlate = true;
        }else if(ColorUtil.isRegionYellow(img)){
            colorJudge = ColorUtil.IsYellow.instance;
            colors[idx] = ColorUtil.Color.YELLOW;
            hasColorPlate = true;
        }else{
            colors[idx] = ColorUtil.Color.UNKNOWN;
            return rect;
        }

        return filter(img, rect, colorJudge);
    }

    private static void markColor(Mat img, ColorUtil.IsColor colorJudger){
        if(!Config.isDebug){
            return;
        }
        byte[] data = new byte[(int)img.total() * 3];
        img.get(0, 0, data);
        byte[] result = new byte[data.length];
        img.get(0, 0, result);
        for(int i=0; i<data.length; i+=3){
            if(colorJudger.isColor(data[i + 2] & 0xff, data[i + 1] & 0xff, data[i] & 0xff)){
                result[i] = (byte)0xff;
                result[i+1] = 0;
                result[i+2] = 0;
            }
        }
        Mat toImg = img.clone();
        toImg.put(0,0,result);
        MainModel.saveImage(toImg, "Color Point in Image");
    }

//    public List<Rect> filterRegions(){
//        List<Rect> results = new ArrayList<>();
//
//        for(int i=0; i<refined.size(); ++i){
//            results.add(filter(i));
//        }
//        resultRects = results;
//        return results;
//    }

    public void filter(){
        for(int i=0; i<rects.size(); ++i){
            refined.add(filter(i));
        }
        if(Config.colorStrategy == Config.UseColorStrategy.UseColor ||
                (Config.colorStrategy == Config.UseColorStrategy.Auto && hasColorPlate)){
            resultRects = new ArrayList<>();
            resultColors = new ArrayList<>();
            originalRects = new ArrayList<>();
            for(int i=0; i<rects.size(); ++i){
                if(colors[i] != ColorUtil.Color.UNKNOWN){
                    resultRects.add(refined.get(i));
                    originalRects.add(rects.get(i));
                    resultColors.add(colors[i]);
                }
            }
        }else{
            resultRects = refined;
            originalRects = rects;
            resultColors = Arrays.asList(colors);
        }
        EdgeRegion2.mark("Filtered by color", colorImage, resultRects);
    }
}
