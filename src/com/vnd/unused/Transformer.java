package com.vnd.unused;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vnd.logic.EdgeRegion2;
import com.vnd.model.SubModel;
import com.vnd.util.Util;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Transformer {
	Mat greyPlate;
	Mat edges;
	List<Rect> fontAreas;
	Rect plateArea;
	private Mat colorImage = new Mat();
//	private Mat greyPlate;
	SubModel model;
	
	public Transformer(SubModel model, Rect plate, List<Rect> fonts){
		this.model = model;
//		fontHeight = plate.height + 1;
		this.plateArea = plate.clone();
		fontAreas = new ArrayList<>();
		for(Rect f : fonts){
			fontAreas.add(f.clone());
		}
		process(plate);
	}
	
	private void process(Rect plate){
//		Rect plateArea = plate.clone();
		int dh = plateArea.height / 2;
		plateArea.y -= dh;
		if(plateArea.y < 0){
			plateArea.y = 0;
		}
		plateArea.height *= 2;
		if(plateArea.height >= model.greyImage.height()){
			plateArea.height = model.greyImage.height();
		}
		
//		int dx = plateArea.width / 14;
//		plateArea.x -= dx;
//		plateArea.width += 2*dx;
//		if(plateArea.x < 0){
//			plateArea.x = 0;
//		}
//		if(plateArea.width >= Model.greyImage.width()){
//			plateArea.width = Model.greyImage.width();
//		}
		
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
		int width = (widths[mid] + widths[mid + 1]) / 2;
		Rect left = fontAreas.get(0);
		if(left.width < width){
			int d = width - left.width;
			left.x -= d;
			plateArea.x -= d;
			plateArea.width += d;
			for(Rect r : fontAreas){
				r.x += d;
			}
		}else{
			left.x += left.width - width;
		}
		left.width = width;

		Rect right = fontAreas.get(widths.length - 1);
		if(width > right.width){
			plateArea.width += width - right.width;
		}
		right.width = width;

		plate.x = 0;
		plate.width = plateArea.width;
		plate.y = dh;
		greyPlate = model.greyImage.submat(plateArea);

		model.saveImage("greyPlate", greyPlate);
//		greyPlate = Util.binary(greyPlate, getThreashold(greyPlate));
//		Model.saveImage("Grey Image2", Model.greyImage);
//		Model.saveImage("Plate Grey Image", greyPlate);
//		Imgproc.equalizeHist(greyPlate, greyPlate);

		Imgproc.cvtColor(greyPlate, colorImage , Imgproc.COLOR_GRAY2BGR);
		edges = EdgeRegion2.vdiff(greyPlate);
		edges = Util.binary(edges, 30, Imgproc.THRESH_BINARY);
		
//		binaryPlate = Util.binary(greyPlate, getThreashold(greyPlate));
//		currentCutPoints = calcCutPoints(greyPlate, "0");
	}
	
	int getThreashold(Mat grey){
		int[] pixels = new int[grey.rows() * grey.cols()];
		byte[] pixel = new byte[1];
		int i=0;
		for(int r=0; r<grey.rows(); ++r){
			for(int c=0; c<grey.cols(); ++c){
				grey.get(r, c, pixel);
				int v = pixel[0] & 0xff;
				pixels[i++] = v;
//				total += v;
			}
		}
		Arrays.sort(pixels);
		int idx = (int)(pixels.length * 0.4);
		return pixels[idx];
	}
	
	public Mat drawLines(){
		Mat lines = new Mat();
		model.saveImage("Edges", edges);
		Imgproc.HoughLinesP(edges, lines , 1, Math.PI/180, 30, edges.cols() / 3, 1);
		
		for (int x = 0; x < lines.cols(); x++) 
	    {
	          double[] vec = lines.get(0, x);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1, y1);
	          Point end = new Point(x2, y2);

	          Core.line(colorImage, start, end, new Scalar(0,0,255), 1);

	    }
		model.saveImage("Lines", colorImage);
		return colorImage;
	}

}
