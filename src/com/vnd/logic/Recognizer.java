package com.vnd.logic;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.vnd.model.*;
import com.vnd.util.ColorUtil;
import com.vnd.util.MatIO2;
import com.vnd.util.Util;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class Recognizer {
	
//	static boolean isBlue(Rect rect, Mat image){
////		assert image.type() == CvType.CV_8UC3;
//		Mat sub = image.submat(rect);
//		int blues = ColorRegionFinder.countBlue(sub);
//		int total = sub.rows() * sub.cols();
//		float ratio = (float)blues / total;
////		System.out.println("Ratio ---------- " + ratio);
//		return ratio > 0.4;
//	}
	
	static Rect getBlue(List<Rect> rects, Mat image){
		for(Rect r : rects){
//			if(isBlue(r, image)){
			if(ColorUtil.isRegionBlue(image.submat(r))){
				return r;
			}
		}
		return null;
	}

	static ColorUtil.Color[] getColors(Rect[] rects, Mat image){
        ColorUtil.Color[] result = new ColorUtil.Color[rects.length];
		for(int i=0; i<result.length; ++i){
			result[i] = ColorUtil.getColor(image.submat(rects[i]));
		}
		return result;
	}
	
	public MainModel mainModel;
	
	Map<Rect, RecognizedResult[]> results = new ConcurrentHashMap<>();

//	Rect resizePlate(SubModel subModel){
//		Rect rect = subModel.getPlate();
////		Model.edges
////		Mat plateEdges = EdgeRegion2.hdiff(Model.greyImage.submat(rect));
////		Mat plateEdges = subModel.edges.submat(rect);
////		Mat plateEdges = Model.edges.submat(rect).clone();
////		Mat binaryEdges = new Mat();
//		Mat binaryEdges = subModel.binaryEdges.submat(rect);//Util.binary(plateEdges, 0.8f, Imgproc.THRESH_BINARY);
//		subModel.saveImage(binaryEdges, "binary edges", ++SubModel.mark);
//		Rect r = refine(binaryEdges, subModel.fontsCount);
//		Rect result = rect.clone();
//		result.x += r.x;
//		result.width = r.width;
//		result.y += r.y;
//		result.height = r.height;
//		subModel.setPlate(result);
//		paddingCn(subModel);
//		return subModel.getPlate();
//	}
//
//	void paddingCn(SubModel subModel){
//		Rect plate = subModel.getPlate();
//		int oldx = plate.x;
//		int d = plate.width / 8;
//		plate.x -= d;
//		if(plate.x < 0){
//			plate.x = 0;
//		}
//		plate.width += oldx - plate.x;
//		if(plate.x + plate.width > subModel.containRect.width){
//			plate.width = subModel.containRect.width - plate.x;
//		}
//	}
	
//	Rect resizePlate(Rect rect){
//		Mat plateEdges = mainModel.edges.submat(rect).clone();
//		Mat binaryEdges = Util.binary(plateEdges, 0.7f, Imgproc.THRESH_BINARY);
//		MainModel.saveImage(binaryEdges, "binary edges", ++SubModel.mark);
//		Rect r = refine(binaryEdges, mainModel.fontsCount);
//		Rect result = rect.clone();
//		result.x += r.x;
//		result.width = r.width;
//		result.y += r.y;
//		result.height = r.height;
//		return result;
//	}

//	Rect refine(Mat edges, int fontCount){
//		EdgeRegion2 regionFinder = new EdgeRegion2(mainModel.scale, edges, fontCount, false, -1);
//		Rect[] rects = regionFinder.getRegions();
//		if(Config.isDebug){
//			regionFinder.markFounds("Resize in Recognizer ");
//		}
//		if(rects.length > 0){
//			return rects[0];
//		}else{
//			return new Rect(0, 0, edges.cols(), edges.rows());
//		}
//	}

	Set<Rect> checked = new HashSet<>();
	Set<Rect> fontChecked = new HashSet<>();

    static boolean contains(Rect container, Rect rect){
        return container.x <= rect.x && container.y <= rect.y &&
                container.x + container.width >= rect.x + rect.width &&
                container.y + container.height >= rect.y + rect.height;
    }

    boolean isChecked(Set<Rect> checked, Rect rect){
        for(Rect r : checked){
            if(contains(r, rect)){
                return true;
            }
        }
        return false;
    }

	private Rect expand(Rect rect, int dw, int dh){
		Rect containRect = rect.clone();
//		int dw = rect.height / 2;
//		int dh = rect.height / 3;
		containRect.x -= dw;
		if(containRect.x < 0){
			containRect.x = 0;
		}
		containRect.width += dw * 2;
		if(containRect.width + containRect.x >= mainModel.colorImage.width()){
			containRect.width = mainModel.colorImage.width() - containRect.x;
		}
		containRect.y -= dh;
		if(containRect.y < 0){
			containRect.y = 0;
		}
		containRect.height += dh * 2;
		if(containRect.y + containRect.height >= mainModel.colorImage.height()){
			containRect.height = mainModel.colorImage.height() - containRect.y;
		}
		return containRect;
	}

    Rect getGlobalPosition(SubModel subModel, List<Rect> fonts){
        Rect result = new Rect();
        Rect f0 = fonts.get(0);
        Rect fn = fonts.get(fonts.size() - 1);
        result.x = subModel.containRect.x + f0.x;
        result.y = subModel.containRect.y + f0.y;
        result.width = fn.x + fn.width - f0.x;
        result.height = fn.y + fn.height - f0.y;
        return result;
    }
	
//	static int mark = 0;
	public SubModel extract(Rect rect, ColorUtil.Color color, boolean useColorFilter) {
		if (rect == null || rect.width < 5 || isChecked(checked, rect)) {
			return null;
		}

//        Rect clonedRect = rect.clone();
		
//		rect = resizePlate(rect);
		SubModel subModel = new SubModel(mainModel, rect, color, useColorFilter);
		if(Config.isDebug){
			EdgeRegion2.saveMark("Found " + SubModel.mark++ + " scale: " + mainModel.scale, subModel.colorImage, rect);
		}

        if(color == ColorUtil.Color.UNKNOWN)
		    rect = subModel.resizePlate();

//        PlateEdgeRemover remover = new PlateEdgeRemover(rect, subModel.greyImage);
//        remover.removeEdge();

        EdgeRegion2.saveMark("Before Pre rectified rect", subModel.colorImage, subModel.getPlate());
		PreSlopeRectifier rectifier = new PreSlopeRectifier(subModel);
		rect = rectifier.rectify();
        PreTiltCorrecter tiltCorrecter = new PreTiltCorrecter(subModel);
        tiltCorrecter.correct();
		
		if(rect == null){
			return null;
		}else if(subModel.backColor == ColorUtil.Color.UNKNOWN || useColorFilter == true){
            Rect gRect = rect.clone();
            gRect.x += subModel.containRect.x;
            gRect.y += subModel.containRect.y;
            if (rect.width < 5 || isChecked(checked, gRect)) {
                return null;
            }
            checked.add(expand(gRect, gRect.height / 3, gRect.height / 3));
        }
		EdgeRegion2.saveMark("Pre rectified rect", subModel.colorImage, rect);
		CharExtractor2 ce = new CharExtractor2(subModel);
		List<Rect> fonts = ce.process();
//		if (fonts.size() != subModel.fontsCount && Config.adaptiveBinary) {
//			ce = new CharExtractor2(subModel, true);
//			fonts = ce.process();
//		}

        if (fonts.size() != subModel.fontsCount){
            return null;
        }

		Rect fontArea = getGlobalPosition(subModel, fonts);

        if(isChecked(fontChecked, fontArea)){
            return null;
        }

        if(subModel.backColor == ColorUtil.Color.UNKNOWN || useColorFilter == true) {
            fontChecked.add(expand(fontArea, 3, 2));
        }

//		resizePlate(subModel);
		CharRefine4 refiner = new CharRefine4(subModel, fonts);
//		if(!refiner.isCorrect()){
//			return new char[0];
//		}
//		Mat[] rawImages = refiner.getFonts();

        subModel.rawFontImages = refiner.getFonts();
        if(subModel.rawFontImages.length != subModel.fontsCount){
            return null;
        }

        return subModel;
	}

    RefinedFontInfo[] refineSize(SubModel subModel, boolean useAdaptive){
        if(subModel == null){
            return new RefinedFontInfo[0];
        }
        CharSizeRefine4 sizeRefiner = new CharSizeRefine4(subModel);
        RefinedFontInfo[] result = sizeRefiner.process(subModel.rawFontImages, useAdaptive);
        if(result.length == subModel.fontsCount){
            MainModel.addAllImages(subModel.getSavedImages());
        }
        return result;
    }

//    RefinedFontInfo[] reExtract(SubModel subModel, Rect originalRect, ColorUtil.Color color){
//        if(subModel.backColor == ColorUtil.Color.UNKNOWN || useColorFilter == true) {
//            return new RefinedFontInfo[0];
//        }else{
//            subModel.useColorFilter = true;
//            return extract(originalRect, color);
//        }
//    }
	
//	public static class PlateResult{
//		Rect rect;
//		
//	}
	
	RecognizedResult[] recognize(Rect rect, ColorUtil.Color color, boolean useColorFilter) {
        SubModel subModel = extract(rect, color, useColorFilter);
		RefinedFontInfo[] imgs = refineSize(subModel, false);
		if(imgs.length != 7){
			return new RecognizedResult[0];
		}
		return recognize(imgs);
	}
	
//	public char[] detect(EdgeRegion2 finder) {
//		ColorRegionFinder cFinder = new ColorRegionFinder(mainModel.colorImage);
//		Rect blue = cFinder.getRegion();
//		if(blue == null || blue.x < 0 || blue.width < 10)
//			return NO_CHAR;
//		Mat binaryEdges = finder.binaryEdges.submat(blue);
//		EdgeRegion2 eFinder = new EdgeRegion2(mainModel.scale, binaryEdges, 7);
//		Rect[] rect = eFinder.getRegions();
//		if(rect.length == 0){
//			return NO_CHAR;
//		}
//		Rect r = rect[0];
//		r.x += blue.x;
//		r.y += blue.y;
//		return recognize(r);
//	}

    enum Level {
        unknown, low, middle, high
    }

    Level judgeLevel(RecognizedResult[] recoginzes){
        int errors = 0;
        int unknowns = 0;
        int normal = 0;
        int good = 0;
        int notGood = 0;
        for(RecognizedResult r : recoginzes){
            if(r.diff == Config.CHAR_RECOGNIZE_ERROR_LEVEL){
                ++errors;
            }else if(r.diff > Config.CHAR_RECOGNIZE_ERROR_LEVEL){
                ++unknowns;
            }else if(r.diff < 2){
                ++good;
            }else if(r.diff < 4){
                ++normal;
            }else{
                ++notGood;
            }
        }
        int total = errors + unknowns;
        if(total > 2){
            return Level.unknown;
        }else if(good > 5){
            return Level.high;
        }else if(total > 0 || notGood > 4){
            return Level.low;
        }else if(good > 3){
            return Level.high;
        }else{
            return Level.middle;
        }
    }

    char[] getChars(RecognizedResult[] recogs){
        char[] result = new char[recogs.length];
        for(int i=0; i<result.length; ++i){
            result[i] = recogs[i].result;
        }
        return result;
    }

    public char[] detect(EdgeRegion2 finder) {
        List<Rect> rects = finder.getRegions();
        ColorFilter filter = new ColorFilter(mainModel.colorImage, rects);
        filter.filter();

        List<Rect> colorRects = filter.getResultRects();
        rects = filter.getOriginalRects();
        List<ColorUtil.Color> colors = filter.getResultColors();

//                ColorUtil.Color[] colors = getColors(rects, mainModel.colorImage);
        char[] chars = detectByColor(rects, colorRects, colors, ColorUtil.Color.BLUE);
        if(chars.length > 1 && Config.ONE_CAR){
            return chars;
        }
        chars = detectByColor(rects, colorRects, colors, ColorUtil.Color.YELLOW);
        if(chars.length > 1 && Config.ONE_CAR){
            return chars;
        }
        if(Config.colorStrategy == Config.UseColorStrategy.NotUseColor){
            chars = detectByColor(rects, colorRects, colors, ColorUtil.Color.UNKNOWN);
            if(chars.length > 1 && Config.ONE_CAR){
                return chars;
            }
        }
        return new char[0];
    }

    public char[] detectByColor(List<Rect> rects, List<Rect> colorRects, List<ColorUtil.Color> colors, ColorUtil.Color color){
        for(int i=0; i<colors.size(); ++i){
            Rect rect = rects.get(i);
            if(colors.get(i) == color){
                char[] result = detectSingleRect(rect, colorRects.get(i), color);
                if(result.length > 1 && Config.ONE_CAR){
                    return result;
                }
            }
        }
        return new char[0];
    }

    private char[] detectSingleRect(Rect rect, Rect colorRect, ColorUtil.Color color){
        return detectSingleRect(rect, colorRect, color, false);
    }

    private char[] detectSingleRect(Rect rect, Rect colorRect, ColorUtil.Color color, boolean useColorFilter){
        Rect clonedRect = rect.clone();
        Rect clonedColorRect = colorRect.clone();
        if(useColorFilter){
            rect = colorRect;
        }
        RecognizedResult[] result = recognize(rect, color, useColorFilter);
        boolean recheck = color != ColorUtil.Color.UNKNOWN && !useColorFilter;
        if(result.length == mainModel.fontsCount){
            Level level = judgeLevel(result);
            switch (level){
                case unknown:
                    break;
                case low:
                    results.put(rect, result);
                    break;
                case middle:
                case high:
                    recheck = false;
                    if(Config.ONE_CAR){
                        return getChars(result);
                    }
                    results.put(rect, result);
                    break;
            }
        }
        if(recheck){
            return detectSingleRect(clonedRect, clonedColorRect, color, !useColorFilter);
        }
        return new char[0];
    }
	
//	public char[] detect0(EdgeRegion2 finder) {
//		Rect[] rects = finder.getRegions();
//		Rect blue = getBlue(rects, mainModel.colorImage);
//		System.out.println("==========================================================has blue region: " + (blue != null));
////		if(blue != null){
//			char[] result = recognize(blue);
//			if(result.length == 7){
//				return result;
//			}
////		}else{
//			for(Rect rect : rects){
//				if(rect == blue){
//					continue;
//				}
//				result = recognize(rect);
//				if(result.length == 7){
////					if(ColorUtil.isRegionBlue(mainModel.colorImage.submat(rect))){
////						System.out.println("================Is Blue");
////					}
//					return result;
//				}
//			}
////		}
//		return NO_CHAR;
//	}
	
	static CharRecognizer2[] recognizers = null;
	
	static CharRecognizer2 recognizer(int i){
		if(recognizers == null){
			recognizers = new CharRecognizer2[]{
					CharRecognizer2.chinese,
					CharRecognizer2.alphabetic,
					CharRecognizer2.alphaNumeric,
					CharRecognizer2.alphaNumeric,
					CharRecognizer2.alphaNumeric,
					CharRecognizer2.alphaNumeric,
					CharRecognizer2.alphaNumeric
				};
		}
		return recognizers[i];
	}
	
	RecognizedResult[] recognize(RefinedFontInfo[] imgs){
//		if(imgs[0].result == Model.FontOne){
//			return NO_CHAR;
//		}
		RecognizedResult[] result = new RecognizedResult[imgs.length];
//		int notSure = 0;
//		result[0] = CharRecognizer2.chinese.recognize(imgs[0]);
//		result[1] = CharRecognizer2.alphabetic.recognize(imgs[1]);
		for(int i=0; i<imgs.length; ++i){
			if(imgs[i].result == SubModel.FontOne){
				if(i == 0){
					result[i] = new RecognizedResult('?', 100);
				}else if(i == 1){
					result[i] = new RecognizedResult('I', 0);
				}else{
					result[i] = new RecognizedResult('1', 0);
				}
			}else{
				result[i] = recognizer(i).recognize(imgs[i].result, imgs[i].reverseColor, imgs[i].original, imgs[i].isLeft, imgs[i].isRight, imgs[i].averageWidth);
//				if(result[i] == SubModel.NOT_CHAR || result[i] ==  '1' || result[i] == 'I'){
//					++notSure;
//					if(notSure >= Config.NOT_SURE_LIMIT){
//						return NO_CHAR;
//					}
//				}else if(result[i] == SubModel.ERROR_CHAR){
//					return NO_CHAR;
//				}
			}
		}
        if(result.length == mainModel.fontsCount && result[0].diff > 4){
            List<Mat> optionalMats = imgs[0].getOptionalResults();
            int count = 0;
            for(Mat mat : optionalMats){
                mainModel.saveImage(mat, "Optional Image", ++count);
                RecognizedResult recognized = recognizer(0).recognize(mat, imgs[0].reverseColor, imgs[0].original, imgs[0].isLeft, imgs[0].isRight, imgs[0].averageWidth, true);
                if(recognized.diff <= 4){
                    result[0] = recognized;
                    break;
                }
            }
        }
		return result;
	}
	
	static final char[] NO_CHAR = new char[0];
	
	char[] d(Mat binaryEdges){
		char[] result = NO_CHAR;
        mainModel.imageSegments = new ImageSegments(binaryEdges);
		for(int scale = 1; result.length != mainModel.fontsCount; ++scale){
			mainModel.scale = scale;
			EdgeRegion2 finder = new EdgeRegion2(scale, binaryEdges, mainModel.imageSegments);
			if(!finder.isScaleAllowed() || scale > Config.MAX_SCALE){
				return NO_CHAR;
			}
			result = detect(finder);
		}
		return result;
	}
	
	public Recognizer(Mat image){
		mainModel = new MainModel(image);
	}
	
	public Recognizer(String absolutePath) throws IOException {
		Mat image;
		image = MatIO2.readMat(new File(absolutePath));
		mainModel = new MainModel(image);
	}
//	public char[] detect(){
//		return detect(image);
//	}
	
	public char[] detect() {
//		Model.init(image);
//		Model.edges = EdgeRegion2.hdiff(Model.greyImage);
//		Model.saveImage(Model.edges, "Initial Edges");
		Mat binaryEdges = Util.binary(mainModel.edges, Config.BINARY_EDGE_THRESHOLD, Imgproc.THRESH_BINARY);
		MainModel.saveImage(binaryEdges, "Binary Edge");
		
		char[] result = d(binaryEdges);
		if(result.length != mainModel.fontsCount && results.size() > 0 && Config.ONE_CAR){
			List<RecognizedResult[]> list = new ArrayList<>(results.values());
			return getChars(list.get(0));
		}
		return result;
//		if(result.length == 7){
//			return result;
//		}else{
//			mainModel.reverseColor = true;
//			return d(binaryEdges);
//		}
	}
}
