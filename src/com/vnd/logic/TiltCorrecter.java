package com.vnd.logic;

import java.util.Arrays;
import java.util.List;

import com.vnd.model.Config;
import com.vnd.model.MainModel;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.vnd.logic.CharExtractor2.Peak;

public class TiltCorrecter {
	Mat plate;
	int checkLineY;
	int originalPeakValue;
	int maxTiltX;
	int tiltDelt = 1;
	byte[] pixels;
	boolean isNormalImage = true;
	byte[] bestTiltedPixels;
	
	public static int getTiltDiff(Mat plate, int checkLineY, int fontWidth) {
		TiltCorrecter correcter = new TiltCorrecter(plate, checkLineY, fontWidth);
		return correcter.bestTilt();
	}

    public TiltCorrecter(Mat plate, int fontWidth) {
        this.plate = plate;
        maxTiltX = fontWidth / 2;
        if(fontWidth >= plate.width()){
            isNormalImage = false;
        }else{
            pixels = new byte[(int)plate.total()];
            plate.get(0, 0, pixels);
            int[] projection = Projection.verticalProjection(pixels, plate.cols(), plate.rows());

            this.checkLineY = findCheckLine(projection);
            if(this.checkLineY < 0){
                isNormalImage = false;
                this.checkLineY = plate.height() / 2;
            }
            originalPeakValue = sumPeaks(projection);
        }
        if(Config.isDebug){
            Mat matProjection = CharExtractor2.drawProjection(plate);
            MainModel.saveImage(matProjection, "Before Tilt");
        }
    }
	
	public TiltCorrecter(Mat plate, int checkLineY, int fontWidth) {
		this.plate = plate;
		this.checkLineY = checkLineY;
//		originalPeakValue = sumPeaks(plate);
		maxTiltX = fontWidth / 2;
		if(fontWidth >= plate.width()){
			isNormalImage = false;
		}else{
			pixels = new byte[(int)plate.total()];
			plate.get(0, 0, pixels);
			originalPeakValue = sumPeaks(pixels);
		}
        if(Config.isDebug){
            Mat matProjection = CharExtractor2.drawProjection(plate);
            MainModel.saveImage(matProjection, "Before Tilt");
        }
//		Model.saveImage(plate, "Before Tilt");
	}
	
//	boolean isSplitable(){
//		int[] proj = Projection.verticalProjection(bestTiltedPixels, plate.cols(), plate.rows());
//		List<Peak> peaks = CharExtrator.findPeaks(3, proj);
//		return peaks.size() > Model.fontsCount - 2;
//	}
	
	public int bestTilt(){
		if(!isNormalImage){
			bestTiltedPixels = pixels;
			return 0;
		}
		int tiltDiff = bestTilt(-tiltDelt);
		if(tiltDiff != 0){
			return tiltDiff;
		}
		return bestTilt(tiltDelt);
	}
	
	int bestTilt(int xstep){
		int minPeakValue = originalPeakValue;
		int tiltDiff = 0;
		byte[] bestTilted = pixels;
		
		for(int dx=xstep; Math.abs(dx) < maxTiltX; dx += xstep){
			byte[] transed = tilt(dx);

			if(Config.isDebug){
				Mat tran = new Mat(plate.rows(), plate.cols(), CvType.CV_8UC1);
				tran.put(0, 0, transed);
                Mat matProjection = CharExtractor2.drawProjection(tran);
                MainModel.saveImage(matProjection, "Tilted with", dx);
			}
			int peakValue = sumPeaks(transed);
			if(peakValue < minPeakValue){
				minPeakValue = peakValue;
				tiltDiff = dx;
				bestTilted = transed;
			}else{
				break;
			}
		}
		bestTiltedPixels = bestTilted;
		return tiltDiff;
	}
	
	byte[] tilt(int dx){
		int btmdx = -dx / 2;
		int topdx = dx - btmdx;
		int height = plate.height();
		int[] dxs = new int[height];
		float h = (float)(height - 1);
		for(int i=0; i<height; ++i){
			dxs[i] = Math.round(-i / h * dx + topdx);
		}
		byte[] mapped = new byte[pixels.length];
		int width = plate.width();
		for(int y=0; y<height; ++y){
			int dxy = dxs[y];
			int start = y * width;
			for(int x=0; x<width; ++x){
				int mapx = x + dxy;
				if(mapx < 0 || mapx >= width){
					continue;
				}
				mapped[start + mapx] = pixels[start + x];
			}
		}
		return mapped;
	}
	
//	Mat transform(int xdiff){
//		int halfdiff = xdiff / 2;
//		int absdiff = Math.abs(halfdiff);
//		int xleft = absdiff + 2;
//		int xright = plate.width() - absdiff - 3;
//		int xtop = xdiff % 2;
//		Point psLeft = new Point(xleft, 0);
//		Point psRight = new Point(xright, 0);
//		Point psBtm = new Point(xleft, plate.height() - 1);
//
//		Point pdLeft = new Point(xleft + halfdiff + xtop, 0);
//		Point pdRight = new Point(xright + halfdiff + xtop, 0);
//		Point pdBtm = new Point(xleft - halfdiff, plate.height() - 1);
//		
//		Mat warpMat = Imgproc.getAffineTransform(
//				new MatOfPoint2f(psLeft, psRight, psBtm), new MatOfPoint2f(pdLeft, pdRight, pdBtm));
//		Mat corrected = new Mat();
//		Imgproc.warpAffine(plate, corrected, warpMat, plate.size());
//		if(Model.isDebug){
//			Mat projs = CharExtrator.drawProjection(corrected);
//			Model.saveImage("corrected " + xdiff, projs);
//		}
//		return corrected;
//	}
//	
//	int sumPeaks(Mat mat){
//		int[] proj = Projection.verticalProjection(mat);
//		int line1 = checkLineY - 1;
//		if(line1 <= 0){
//			line1 = checkLineY + 2;
//		}
//		int line2 = checkLineY + 1;
//		return sumPeaks(proj, line1) + sumPeaks(proj, checkLineY) + sumPeaks(proj, line2);
//	}
	int sumPeaks(byte[] pixels){
		int[] projection = Projection.verticalProjection(pixels, plate.cols(), plate.rows());
        return sumPeaks(projection);
	}
    int sumPeaks(int[] projection){
        if(checkLineY < 1) {
            return sumPeaks(projection, checkLineY);
        }else{
            return sumPeaks(projection, checkLineY) + sumPeaks(projection, checkLineY - 1);
        }
    }

    int findCheckLine(int[] projection){

        for(int i=0, height = plate.height() / 2; i<height; ++i){
            if(CharExtractor2.findPeaks(i, projection).size() > 4){
                return i;
            }
        }
        return -1;
    }
	
	int sumPeaks(int[] projection, int lineY){
		int[] sub = Arrays.copyOfRange(projection, maxTiltX, projection.length - maxTiltX);
		List<Peak> peaks = CharExtractor2.findPeaks(lineY, sub);
		int sum = 0;
		for(Peak p : peaks){
			sum += p.end - p.start;
		}
		return sum;
	}
}
