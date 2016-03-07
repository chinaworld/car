package com.vnd.logic;

import com.vnd.model.Config;
import com.vnd.model.SubModel;
import com.vnd.util.Util;
import org.opencv.core.Mat;

/**
 * Created by Administrator on 2016/1/9.
 */
public class PreTiltCorrecter {
    SubModel model;

    public PreTiltCorrecter(SubModel model){
        this.model = model;
    }

    public void correct(){
        Mat plateGrey = model.greyImage.submat(model.getPlate());
        int threshold;
        if(CharExtractor2.isUseAdaptive(plateGrey) && plateGrey.rows() > 10){
            threshold = -1;
            plateGrey = Util.adaptiveBinary(plateGrey, -10, model.getThresholdType());
            model.saveImage(plateGrey, "adaptive binary plate");
        }else{
            int quart = plateGrey.cols() / 4;
            Mat sub = plateGrey.colRange(quart, plateGrey.cols() - quart);
            if(model.reverseColor()){
                float t = model.getScale() > 2 ? 0.3f : 0.35f;
                threshold = Util.getThreashold(sub, t);
            }else{
                float t = model.getScale() > 2 ? 0.7f : 0.65f;
                threshold = Util.getThreashold(sub, t);
            }
            plateGrey = Util.binary(plateGrey, threshold, model.getThresholdType());
        }

        TiltCorrecter correcter = new TiltCorrecter(
                plateGrey,
                model.getPlate().width / (model.fontsCount + 1));
        int tilt = correcter.bestTilt();
        Mat matTilt = Util.transformH(model.colorImage, tilt);

        model.saveImage(model.colorImage, "Before pre tilt correct");
        model.setImage(matTilt);

        model.saveImage(model.colorImage, "After pre tilt correct");
    }
}
