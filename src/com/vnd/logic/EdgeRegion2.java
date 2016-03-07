package com.vnd.logic;

import com.vnd.model.*;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EdgeRegion2 {
    //	Mat image;
//	int unit = 5;
    int minPixels = Config.MIN_PIXELS;
    int minWidth = Config.MIN_WIDTH;
    int maxWidth = Config.MAX_WIDTH;

    float ratio = Config.WIDTH_HEIGHT_RATIO;
    int minHeight = (int) (minWidth / ratio);
    int maxHeight = (int) (maxWidth / ratio);
//	int threshold = 15;

    int maxD = maxWidth / minPixels * 2;
    //	int maxPixels = Config.MAX_PIXELS;
//	double actualRatio = 440 / 140;
    Mat binaryEdges;

    int expectedHeight = -1;

    //	EdgeRegion2(Mat image, int threshold){
//		Mat equ = new Mat();
//		Imgproc.equalizeHist(image, equ);
//		edges = hdiff(equ);
////		this.image = image;
//		this.threshold = Util.getThreashold(edges, 0.5f); // threshold;
//	}
//	
//	EdgeRegion2(){
//		this(Model.greyImage, 10);
//	}
    int heightLimit, widthLimit;

    int scale = 1;
    ImageSegments imageSegments;

    public EdgeRegion2(int scale, Mat binaryEdges, ImageSegments imageSegments) {
        this.scale = scale;
        this.binaryEdges = binaryEdges;
        this.imageSegments = imageSegments;
//		Imgproc.equalizeHist(edges, edges);
//		this.threshold = Util.getThreashold(edges, 0.98f); // threshold;
        minWidth *= scale;
        maxWidth *= scale;
        if (maxWidth > binaryEdges.cols()) {
            maxWidth = binaryEdges.cols();
        }
        minHeight = Math.round(minWidth / 4.5f);
        maxHeight = maxWidth / 4;
//		maxD = maxWidth / 14;
        maxD = maxWidth / 7 * 2 - scale * 2;
        heightLimit = maxHeight * 3;
        widthLimit = maxWidth * 3;
//		maxPixels = maxWidth * 4 / 5;
    }

//	public EdgeRegion2(int scale, Mat edges, int threashold){
//		this(scale, edges);
//		this.threshold = threashold;
//		Model.saveImage("Plate Edges only", edges);
//		maxD = maxlength / minPixels * 3;
//	}

    static int mark = 1;

    public EdgeRegion2(int scale, Mat binaryEdges, int fontCount) {
//		this.threshold = threashold;
//		Model.saveImage("Plate Edges: " + mark++, binaryEdges);

        this.scale = scale;
        this.binaryEdges = binaryEdges; //edges.clone();
//		Imgproc.equalizeHist(edges, edges);
//		this.threshold = Util.getThreashold(edges, 0.98f); // threshold;
        minWidth = binaryEdges.cols() / 2;
        maxWidth = binaryEdges.cols();
        minHeight = binaryEdges.rows() / 3;
        maxHeight = binaryEdges.rows();
        maxD = maxWidth / fontCount * 2;
//		maxPixels = maxWidth;
        minPixels = fontCount * 2;
        heightLimit = maxHeight;
        widthLimit = maxWidth;
    }

    public EdgeRegion2(int scale, Mat binaryEdges, int fontCount, boolean needMergeGroup, int expectedHeight) {
        this(scale, binaryEdges, fontCount);
        this.needMergeGroup = needMergeGroup;
        this.expectedHeight = expectedHeight;
    }

    public boolean isScaleAllowed() {
        return minWidth < binaryEdges.cols() - 10;
    }

//	@Override
//	public Rect getRegion() {
//		LinkedHashMap<Rect, Double> rects = getRegionsMap();
//		
//		Collection<Rect> coll = rects.keySet();
//		if(coll.size() > 1){
//			return findByContrast(coll);
//		}else if(coll.size() == 0){
//			return null;
//		}
//		return coll.toArray(new Rect[0])[0];
//	}
//	public Rect[] getRegions() {
//		LinkedHashMap<Rect, Double> rects = getRegionsMap();
//
//		Collection<Rect> coll = rects.keySet();
////		for(Rect r : coll){
////			Main.saveMark("rect " + r.x + " scale " + scale, Model.greyImage, r);
////		}
//        markFounds();
//		return coll.toArray(new Rect[0]);
//	}

//	Rect findByContrast(Collection<Rect> rects){
//		float max = 0;
//		Rect result = null;
//		
//		for(Rect rect : rects){
//			long total = 0;
//			Mat clip = edges.submat(rect);
//			int rows = clip.rows();
//			int cols = clip.cols();
//			
//			for(int r=0; r<rows; ++r){
//				for(int c=0; c<cols; ++c){
//					edges.get(r, c, pixels);
//					int pv = pixels[0] & 0xff;
//					total += pv;
//				}
//			}
//			float avg = (float)total / (rows * cols);
//			if(avg > max){
//				max = avg;
//				result = rect;
//			}
//		}
//		return result;
//	}

    List<Rect> allRects = new ArrayList<>();
    Mat markImage = null;

    public void markFounds() {
        markFounds("");
    }

    public void markFounds(String title) {
        if (Config.isDebug) {
            mark(title + "All rects scale: " + scale, binaryEdges, allRects);
            mark(title + "All groups scale: " + scale, markImage, allRects);
//            mark(title + "All rects scale: " + scale, model., allRects);
        }
    }

    long totalTimeNoCache = 0;
    long totalTimeWithCache = 0;
    public List<Rect> getRegions() {
//		Model.saveImage("Edges", edges);
//		Mat binaryEdges = Util.binary(edges, threshold);
//		Model.saveImage("Binary Edges " + scale + " threshold: " + threshold + " " + mark++, binaryEdges);
//        long time0 = System.nanoTime();
        List<LineSegments> lines = findLineSegments5(binaryEdges); //, historySegments);
//        long time1 = System.nanoTime();
//        List<LineSegments> lines2 = findLineSegments3(binaryEdges);
//        long time2 = System.nanoTime();
//
//        if(imageSegments != null) {
//            totalTimeNoCache += time2 - time1;
//            totalTimeWithCache += time1 - time0;
//            System.out.println("Scale: " + scale);
//            System.out.println("totalTimeWithCache: " + totalTimeWithCache);
//            System.out.println("totalTimeNoCache: " + totalTimeNoCache);
//            System.out.println(lines2.size());
//        }

        if (Config.isDebug) {
            markImage = markFoundSegments(lines);
//            markFoundSegments(lines2);
        }
        List<LineGroup> groups = findLineGroups(lines);

//		LinkedHashMap<Rect, Double> rects = new LinkedHashMap<>();
        List<Rect> rects = new ArrayList<>();

        double aminRatioD = 100;
//		double minRatioD = 100;
        Rect result = null;

//		int i=0;
        for (LineGroup g : groups) {
//			if(scale == 1 && g.lines.get(0).line > 118 && g.lines.get(g.lines.size() - 1).line <288){
//				System.out.print("aaa");
//			}
            List<Rect> groupRects = findRect(g);
            allRects.addAll(groupRects);
            for (Rect r : groupRects) {
                if (r != null && r.y < binaryEdges.rows() && isRectSizeCorrect(r)) {
                    double rd = r.width / (double) r.height - ratio;
                    //				Main.saveMark("rect " + rd, Model.colorImage, r);
                    double ard = Math.abs(rd);
                    if (ard < Config.MAX_WIDTH_HEIGHT_RATIO) {
                        rects.add(r);
//						rects.put(r, rd);
                    }
                    if (aminRatioD > ard) {
                        aminRatioD = ard;
                        result = r;
//						minRatioD = rd;
                    }
                }
            }
        }

        if (!rects.contains(result)) {
            rects.contains(result);
        }
//        markFounds();
//		if(!rects.containsKey(result) && result != null){
//			rects.put(result, minRatioD);
//		}

//		for(Entry<Rect, Double> e : rects.entrySet()){
//			Rect rect = e.getKey();
//			if(e.getValue() < 0){
//				double expectedLength = ratio * rect.height;
//				double dlen = expectedLength - rect.width;
//				int toAdd = (int)Math.ceil(dlen / 2);
//				if(rect.x > toAdd){
//					rect.x -= toAdd;
//					rect.width += toAdd * 2;
//				}
//			}
//			double dh = (int)Math.ceil(rect.height / 4);
//			double dh2 = dh * 2;
//			if(rect.y > dh && rect.height + dh2 < edges.rows()){
//				rect.y -= dh;
//				rect.height += dh2;
//			}
////			Main.saveMark("modified rect " + e.getValue(), Model.colorImage, rect);
//		}

        return rects;
    }

    static byte[] pixels = new byte[1];

    List<Rect> findRect(LineGroup group) {
        List<Rect> rects = new ArrayList<>();
        if (group.lines.size() < minHeight) {
            return rects;
        }
//		int cols = binaryEdges.cols();
        int minCol = group.findMinCol();
        int maxCol = group.findMaxCol();
        int start = maxCol;
        int end = minCol;

//		int count = 0;
        int[] colPixelCount = new int[maxCol];
        for (int c = minCol; c < maxCol; ++c) {
            int count = 0;
            for (Segment s : group.lines) {
                if (s.contains(c)) {
                    ++count;
//                    if(count > minHeight){
//                        break;
//                    }
                }
            }
            colPixelCount[c] = count;
            if (count > minHeight) {
                if (c < start) {
                    start = c;
                }
                if (c > end) {
                    end = c;
                }
            } else if (count <= minHeight) {
                if (end - start > minWidth) {
                    Rect rct = new Rect(start, group.start, end - start + 1, group.lines.size());
                    rct = trimLines(group, rct);
                    trim(group, rct, colPixelCount);
                    rects.add(rct);
                }
                start = maxCol;
                end = minCol;
            }
        }
        if (end - start > minWidth) {
            Rect rct = new Rect(start, group.start, end - start + 1, group.lines.size());
            rct = trimLines(group, rct);
            trim(group, rct, colPixelCount);
//			if(rct.y < binaryEdges.rows() && isRectSizeCorrect(rct))
            rects.add(rct);
        }
//		for(Rect rct : rects){
//			patchChinese(group, rct);
//		}

        return rects;
    }

    void trim(LineGroup group, Rect r) {
        int start = r.x + r.width;
        int end = 0;
        int min = (int) (r.height * 0.6);
        for (int c = r.x, len = r.x + r.width; c < len; ++c) {
            int count = 0;
            for (Segment s : group.lines) {
                if (s.contains(c)) {
                    ++count;
                }
            }
            if (count > min) {
                if (c < start) {
                    start = c;
                }
                if (c > end) {
                    end = c;
                }
            } else if (count <= min && end - start > minWidth) {
                break;
            }
        }
        if (start >= end) {
            r.width = 1;
        } else {
//			int diff = r.width - (end - start);
            r.x = start;
            r.width = end - start;
        }
    }

    void trim(LineGroup group, Rect r, int[] colPixelCount) {
        int start = r.x + r.width;
        int end = 0;
        int min = (int) (r.height * 0.6);
        for (int c = r.x, len = r.x + r.width; c < len; ++c) {
//			int count = 0;
//			for(Segment s : group.lines){
//				if(s.contains(c)){
//					++count;
//				}
//			}
            int count = colPixelCount[c];
            if (count > min) {
                if (c < start) {
                    start = c;
                }
                if (c > end) {
                    end = c;
                }
            } else if (count <= min && end - start > minWidth) {
                break;
            }
        }
        if (start >= end) {
            r.width = 1;
        } else {
//			int diff = r.width - (end - start);
            r.x = start;
            r.width = end - start;
        }
    }

    boolean isRectSizeCorrect(Rect r) {
//		return true;
        return r.width > r.height && r.height >= minHeight && r.height <= heightLimit && r.width >= minWidth && r.width <= widthLimit;
    }

//	Rect patchChinese(LineGroup group, Rect rect){
//		Rect cn = rect.clone();
//		int width = rect.width / (Model.fontsCount + 1);
//		if(width > cn.x){
//			width = cn.x;
//		}
//		cn.x -= width;
//		cn.width = width;
//		int colCount = 0;
//		int min = cn.height / 6;
//		for(int c=cn.x; c<cn.width; ++c){
//			int count = 0;
//			for(Segment s : group.lines){
//				if(s.contains(c)){
//					++count;
//				}
//			}
//			if(count > min){
//				++colCount;
//			}
//		}
//		if(colCount > width / 4){
//			rect.x -= width;
//			rect.width += width;
//		}
//		return rect;
//	}

    Rect trimLines(LineGroup group, Rect rect) {
        Rect r = trimLines(group, rect.clone(), MIN_OVERLAP);
        if (expectedHeight < 5 || r.height <= expectedHeight) {
            return r;
        } else {
            int minDiff = rect.height - expectedHeight;
            Rect minR = r;
            for (float minOverlap = MIN_OVERLAP + 0.1f; r.height > expectedHeight && minOverlap < 0.9; minOverlap += 0.1) {
                r = trimLines(group, rect.clone(), minOverlap);
                int diff = Math.abs(r.height - expectedHeight);
                if (diff < minDiff) {
                    minDiff = diff;
                    minR = r;
                }
            }
            return minR;
        }
    }

    Rect trimLines(LineGroup group, Rect rect, float minOverlap) {
        List<Segment> lss = group.lines;
        for (Segment s : lss) {
            if (!hasRange(s, rect.x, rect.width, minOverlap)) {
                ++rect.y;
                --rect.height;
            } else {
                break;
            }
        }
        for (int i = lss.size() - 1; i >= 0; --i) {
            Segment s = lss.get(i);
            if (!hasRange(s, rect.x, rect.width, minOverlap)) {
                --rect.height;
            } else {
                break;
            }
        }
        return rect;
    }

    static float MIN_OVERLAP = 0.6f;

    boolean hasRange(Segment seg, int start, int length, float minOverlap) {
        int e2 = start + length;
//		for(Segment seg : ls.segments){
        int s = seg.start > start ? seg.start : start;
        int e1 = seg.start + seg.length;
        int e = e1 > e2 ? e2 : e1;
        if ((float) (e - s) / length > minOverlap) {
            return true;
        }
//		}
        return false;
    }

    int allowEmptyLines = 1;
    boolean needMergeGroup = false;
    int allowedGroupGap = 4;

    List<LineGroup> mergeLineGroups(List<LineGroup> groups) {
        List<LineGroup> result = new ArrayList<>();
        allowedGroupGap = binaryEdges.rows() / 8;
        if (allowedGroupGap < 4) {
            allowedGroupGap = 4;
        } else if (allowedGroupGap > 6) {
            allowedGroupGap = 6;
        }
        for (int i = 0, size = groups.size() - 1; i < size; ++i) {
            LineGroup lg = groups.get(i);
            LineGroup next = groups.get(i + 1);
            result.add(lg);
            if (next.start - lg.end < allowedGroupGap && lg.lines.size() > allowedGroupGap && next.lines.size() > allowedGroupGap) {
                lg.lines.addAll(next.lines);
            } else {
                result.add(next);
            }
        }
        if (groups.size() == 1) {
            return groups;
        }
        return result;
    }

//	List<LineGroup> findLineGroups(){
//		List<LineSegments> lines = findLineSegments3(binaryEdges);
//		return findLineGroups(lines);
//	}

    List<LineGroup> findLineGroups(List<LineSegments> lines) {
        List<LineGroup> result = new ArrayList<>();
        LineGroup group;
        while ((group = findLineGroup(lines)) != null) {
            result.add(group);
        }
        if (expectedHeight > 5 && allowEmptyLines < 4 && !hasExpectedLineGroup(result)) {
            ++allowEmptyLines;
            return findLineGroups(lines);
        }
        return result;
    }

    boolean hasExpectedLineGroup(List<LineGroup> groups) {
        for (LineGroup g : groups) {
            if (g.lines.size() >= expectedHeight * 0.20) {
                return true;
            }
        }
        return false;
    }

    LineGroup findLineGroup(List<LineSegments> lines) {
        if (lines.size() < minHeight) {
            return null;
        }
        LineSegments pre = null;
        Segment preSeg = null;
        LineGroup group = null;
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < lines.size(); ++i) {
            LineSegments lss = lines.get(i);
            int segIndex = -1;
            if (preSeg == null) {
                group = new LineGroup(lss.line);
                segIndex = 0;
            } else if (lss.line - pre.line <= allowEmptyLines) {
                segIndex = findOverlap(lss, preSeg);
            } else {
                group.end = pre.line;
                break;
            }
            if (segIndex >= 0) {
                Segment s = lss.segments.get(segIndex);
                int overlapLine = findOverlapLine(s, lines, i);
                if (overlapLine < 0) {
                    lss.segments.remove(segIndex);
                    if (lss.segments.size() == 0) {
                        toRemove.add(i);
                    }
                } else if (overlapLine > i) {
                    int cut = cutPoint(s, lines.get(overlapLine));
                    if (shouldCut(cut, lines, i)) {
                        int diff = cut - s.start;
                        if (diff < 0) {
                            throw new RuntimeException();
                        }
                        s.start = cut;
                        s.length -= diff;
                    }
                }
                group.lines.add(s);
                preSeg = s;
                pre = lss;
            } else {
                group.lines.add(LineGroup.EmptySegment);
            }
        }
        if (group.end <= 0 && group.lines.size() > 0) {
            group.end = lines.get(lines.size() - 1).line;
        }
        for (int i = toRemove.size() - 1; i >= 0; --i) {
            int idx = toRemove.get(i);
            lines.remove(idx);
        }
        return group;
    }

    boolean shouldCut(int cutPoint, List<LineSegments> lines, int index) {
        LineSegments current = lines.get(index);
        int count = 0;
        for (int i = index - 1; i >= 0; --i) {
            LineSegments lss = lines.get(i);
            if (current.line - lss.line > allowEmptyLines) {
                break;
            }
            if (lss.contains(cutPoint)) {
                ++count;
            }
        }
        for (int i = index + 1; i < lines.size(); ++i) {
            LineSegments lss = lines.get(i);
            if (lss.line - current.line > allowEmptyLines) {
                break;
            }
            if (lss.contains(cutPoint)) {
                ++count;
            }
        }
        return count < minHeight;
    }

    int findOverlap(LineSegments lss, Segment seg) {
        for (int i = 0, len = lss.segments.size(); i < len; ++i) {
            Segment s = lss.segments.get(i);
            if (s.overlapWith(seg, minWidth)) {
                return i;
            }
        }
        return -1;
    }

    int cutPoint(Segment seg, LineSegments lss) {
        List<Segment> overlaps = lss.overlaps(seg, minWidth);
        if (overlaps.size() < 2) {
            return -1;
        }
        Segment seg1 = overlaps.get(0);
        Segment seg2 = overlaps.get(1);
        return seg1.start + seg1.length + seg2.start / 2;
    }

//	int cutPoint(Segment seg, LineSegments lss){
//		return seg1.start + seg1.length + seg2.start / 2;
//	}

    int findOverlapLine(Segment seg, List<LineSegments> lines, int index) {
        LineSegments current = lines.get(index);
        for (int i = index + 1; i < lines.size(); ++i) {
            LineSegments lss = lines.get(i);
            if (lss.line - current.line > allowEmptyLines) {
                break;
            }
            if (lss.overlaps(seg, minWidth).size() > 1) {
                return i;
            }
        }
        for (int i = index - 1; i >= 0; --i) {
            LineSegments lss = lines.get(i);
            if (current.line - lss.line > allowEmptyLines) {
                break;
            }
            List<Segment> overlaps = lss.overlaps(seg, minWidth);
            if (overlaps.size() > 0) {
                Segment over = overlaps.get(0);
                if (over.start + over.length < seg.start + seg.length) {
                    return i;
                }
            }
        }
        return -1;
    }

//	List<LineGroup> findLineGroups(){
//		List<LineGroup> groups = new ArrayList<>();
//		List<LineSegments> lines = findLineSegments3(binaryEdges);
//		
//		if(lines.size() <= 0){
//			return groups;
//		}
//		LineGroup group = new LineGroup();
//		group.lines.add(lines.get(0));
//		groups.add(group);
//		for(int i=1; i<lines.size(); ++i){
//			LineSegments pre = lines.get(i-1);
////			group.lines.add(pre);
//			if(lines.get(i).line - pre.line > allowEmptyLines){
//				group = new LineGroup();
//				groups.add(group);
//			}
//			group.lines.add(lines.get(i));
//		}
//		
//		if(needMergeGroup){
//			groups = mergeLineGroups(groups);
//		}
//
//		List<LineGroup> result = new ArrayList<>();
//		for(LineGroup g : groups){
//			if(g.lines.size() >= minHeight){
//				result.add(g);
//			}
//		}
//		return result;
//	}

    //	List<LineSegments> findLineSegments(Mat image){
//		int rows = image.rows();
//		byte[] line = new byte[image.cols()];
//		List<LineSegments> result = new ArrayList<>();
//		
//		for(int i=0; i<rows; ++i){
//			image.get(i, 0, line);
//			List<Segment> segs = findSegments(line);
//			if(segs.size() > 0){
//				LineSegments ls = new LineSegments();
//				ls.line = i;
//				ls.segments = segs;
//				result.add(ls);
//			}
//		}
//		return result;
//	}

    private int findSegmentInList(List<LineSegments> segments, int fromIndex, int row){
        for(int i=fromIndex; i< segments.size(); ++i){
            if(segments.get(i).line == row){
                return i;
            }
        }
        return fromIndex - 1;
    }

    List<LineSegments> findLineSegments5(Mat edges){
        if(imageSegments != null && imageSegments.edges == edges){
            return imageSegments.find(maxD, minWidth);
        }else{
            return findLineSegments3(edges);
        }
    }

    List<LineSegments> findLineSegments4(Mat edges, HashMap<Mat, List<List<LineSegments>>> history) {
        if(history == null){
            return findLineSegments3(edges);
        }
        int cols = edges.cols();
        int rows = edges.rows();
//		byte[] pixels = new byte[1];

//		BinaryNeighbor neighbor = new BinaryNeighbor(edges, maxD);

        if(!history.containsKey(edges)){
            history.put(edges, new ArrayList<List<LineSegments>>());
        }
        List<List<LineSegments>> edgeSegments = history.get(edges);
        if(scale - 1 <edgeSegments.size()){
            return edgeSegments.get(scale - 1);
        }else if(edgeSegments.size() == 0){
            List<LineSegments> result = findLineSegments3(edges);
//            assert scale == 1;
            edgeSegments.add(result);
            return result;
        }

        List<LineSegments> previous = edgeSegments.get(edgeSegments.size() - 1);

        int mappedIndex = -1;

        List<LineSegments> result = new ArrayList<>();
//        assert scale - 1 == edgeSegments.size();
        edgeSegments.add(result);
        byte[] line = new byte[cols];
        for (int r = 0; r < rows; ++r) {
            edges.get(r, 0, line);
            LinePixels neighbor = new LinePixels(line);
            int pre = neighbor.nextPixelPos();
            if (pre < 0) {
                continue;
            }
            LineSegments lineSegs = new LineSegments();
            List<Segment> segs = new ArrayList<>();
//			lineSegs.segments = segs;
            Segment seg = new Segment(pre, 1);
            segs.add(seg);

            LineSegments mapped = null;
            if(previous.size() > 0) {
                int fromIndex = mappedIndex + 1;
                int maxIndex = previous.get(previous.size() - 1).line;
                if (fromIndex <= maxIndex) {
                    mappedIndex = findSegmentInList(previous, fromIndex, r);
                    if (mappedIndex >= fromIndex) {
                        mapped = previous.get(mappedIndex);
                    }
                }
            }

            if(mapped == null) {
                for (int p = neighbor.nextPixelPos(); p >= 0; p = neighbor.nextPixelPos()) {
                    if (p - pre > maxD) {
                        seg.setEnd(pre + 1);
                        seg = new Segment(p, 1);
                        segs.add(seg);
                    } else {
                        ++seg.pixels;
                    }
                    pre = p;
                }
                if (pre >= 0) {
                    seg.setEnd(pre + 1);
                }

                List<Segment> found = new ArrayList<>();
                for (Segment s : segs) {
                    if (s.length > minWidth) {
                        found.add(s);
                    }
                }

                if (found.size() > 0) {
                    lineSegs.segments = found;
                    lineSegs.line = r;
                    result.add(lineSegs);
                }
            }else{
                Segment mappedSegment = mapped.find(pre);
                if(mappedSegment != null){
                    pre = mappedSegment.getEnd() - 1;
                    neighbor.setCurrent(pre);
                }
                for (int p = neighbor.nextPixelPos(); p >= 0; p = neighbor.nextPixelPos()) {
                    if (p - pre > maxD) {
                        seg.setEnd(pre + 1);
                        seg = new Segment(p, 1);
                        segs.add(seg);
                    } else {
                        ++seg.pixels;
                    }
                    mappedSegment = mapped.find(p);
                    if(mappedSegment != null){
                        p = mappedSegment.getEnd() - 1;
                        neighbor.setCurrent(p);
                    }
                    pre = p;
                }
                if (pre >= 0) {
                    seg.setEnd(pre + 1);
                }

                List<Segment> found = new ArrayList<>();
                for (Segment s : segs) {
                    if (s.length > minWidth) {
                        found.add(s);
                    }
                }

                if (found.size() > 0) {
                    lineSegs.segments = found;
                    lineSegs.line = r;
                    result.add(lineSegs);
                }
            }
        }
//		if(Model.isDebug){
//			markFoundSegments(result);
//		}
        return result;
    }

    List<LineSegments> findLineSegments3(Mat edges) {
        int cols = edges.cols();
        int rows = edges.rows();
//		byte[] pixels = new byte[1];

//		BinaryNeighbor neighbor = new BinaryNeighbor(edges, maxD);
        List<LineSegments> result = new ArrayList<>();

//		int[] line = new int[cols];
        byte[] line = new byte[cols];
        for (int r = 0; r < rows; ++r) {
            edges.get(r, 0, line);
            LinePixels neighbor = new LinePixels(line);
            int pre = neighbor.nextPixelPos();
            if (pre < 0) {
                continue;
            }
            LineSegments lineSegs = new LineSegments();
            List<Segment> segs = new ArrayList<>();
//			lineSegs.segments = segs;
            Segment seg = new Segment(pre, 1);
            segs.add(seg);

            for (int p = neighbor.nextPixelPos(); p >= 0; p = neighbor.nextPixelPos()) {
                if (p - pre > maxD) {
                    seg.setEnd(pre + 1);
                    seg = new Segment(p, 1);
                    segs.add(seg);
                } else {
                    ++seg.pixels;
                }
                pre = p;
            }
            if (pre >= 0) {
                seg.setEnd(pre + 1);
            }

            List<Segment> found = new ArrayList<>();
            for (Segment s : segs) {
                if (s.length > minWidth) {
                    found.add(s);
                }
            }

            if (found.size() > 0) {
                lineSegs.segments = found;
                lineSegs.line = r;
                result.add(lineSegs);
            }
        }
//		if(Model.isDebug){
//			markFoundSegments(result);
//		}
        return result;
    }

//	List<LineSegments> findLineSegments2(Mat edges){
//		int cols = edges.cols();
//		int rows = edges.rows();
////		byte[] pixels = new byte[1];
//		
//		BinaryNeighbor neighbor = new BinaryNeighbor(edges, maxD);
//		List<LineSegments> result = new ArrayList<>();
//		
////		int[] line = new int[cols];
//		byte[] data = new byte[1];
//		for (int r = 0; r < rows; ++r) {
////			Arrays.fill(line, 0);
//			int c = 0;
//			for (; c < cols; ++c) {
//				edges.get(r, c, data);
////				int pv = Util.getValue(edges, r, c);
//				if(data[0] != 0){
//					break;
//				}
//			}
//			if(c >= cols)
//				continue;
//			LineSegments lineSegs = new LineSegments();
////			pixels[0] = 1;
//			List<Segment> segs = new ArrayList<>();
//			Segment seg = new Segment(c, 1);
//			segs.add(seg);
////			boolean newSegment = false;
//			while(c < cols){
//				NeighborPixel np = neighbor.getRightNeighbor(r, c);
//				if(np.position > 0){
//					if(seg != null){
//						seg.setEnd(np.position);
////						seg.length += np.position - c;
//					}else{
//						seg = new Segment(np.position, 1);
//						segs.add(seg);
//					}
//					++seg.pixels;
//					c = np.position;
//				}else{
//					seg = null;
//					c += maxD - 1;
//				}
//			}
//			List<Segment> found = new ArrayList<>();
//			for(Segment s : segs){
//				if(s.length > minWidth && s.pixels >= minPixels && s.pixels <= maxPixels){
//					found.add(s);
//				}
//			}
//			
//			if(found.size() > 0){
//				lineSegs.segments = found;
//				lineSegs.line = r;
//				result.add(lineSegs);
//			}
//		}
//		if(Model.isDebug){
//			markFoundSegments(result);
//		}
//		
//		return result;
//	}

    Mat markFoundSegments(List<LineSegments> result) {
        pixels[0] = (byte) 255;
        Mat markImage = binaryEdges.clone();
        for (LineSegments lineSegs : result) {
            for (Segment s : lineSegs.segments) {
                for (int i = s.start, to = s.start + s.length; i < to; ++i) {
                    markImage.put(lineSegs.line, i, pixels);
                }
            }
        }
        MainModel.saveImage(markImage, "Found by neighbor", mark++);
        return markImage;
    }

//	List<Segment> findSegments(byte[] line){
//		int[] units = new int[unitCount];
//		for(int i=0; i<units.length; ++i){
//			units[i] = countUnitPixel(line, i * unit);
//		}
//		List<Segment> segments = new ArrayList<>();
//		
////		int start = -1;
//		int continues = 0;
//		for(int i=0; i<units.length; ++i){
//			if(units[i] > minPixelsPerUnit){
//				++continues;
////				if(start == -1){
////					start = i;
////				}
//			}else{
//				Segment seg = new Segment();
//				seg.start = (i - continues) * unit;
//				seg.length = continues * unit;
//				segments.add(seg);
//				continues = 0;
//			}
//		}
//
//		List<Segment> result = new ArrayList<>();
//		for(Segment seg : segments){
//			if(seg.length >= minlength && seg.length <= maxlength){
//				result.add(seg);
//			}
//		}
//		return result;
//	}

//	int countUnitPixel(byte[] line, int start){
//		int end = start + unit;
//		if(end > line.length){
//			end = line.length;
//		}
//		int count = 0;
//		for(int i=start; i<end; ++i){
//			int pv = line[i] & 0xff;
//			if(pv > 0){
//				++count;
//			}
//		}
//		return count;
//	}

    public static Mat vdiff(Mat image) {
//        int cols = image.cols();
//        int rows = image.rows();
////		byte[] pixels = new byte[1];
//        Mat temp = new Mat(rows, cols, CvType.CV_8U);
//
//        int rows1 = rows - 1;
//        for (int c = 0; c < cols; ++c) {
//            for (int r = 0; r < rows1; ++r) {
//                image.get(r, c, pixels);
//                int pv = pixels[0] & 0xff;
//                image.get(r + 1, c, pixels);
//                int pv2 = pixels[0] & 0xff;
//
//                pixels[0] = (byte) Math.abs(pv2 - pv);
//                temp.put(r, c, pixels);
//            }
//        }
//        return temp;
        Mat sub0 = image.rowRange(0, image.rows() - 1);
        Mat sub1 = image.rowRange(1, image.rows());
        Mat dst = new Mat(sub0.size(), image.type());
        Core.absdiff(sub1, sub0, dst);
        return dst;
    }

//	static Mat diff(Mat image){
//		int cols = image.cols();
//		int rows = image.rows();
//		
//		Mat hd = hdiff(image);
//		Mat vd = vdiff(image);
//		Mat result = new Mat(rows, cols, CvType.CV_8U);
//		
//		for (int r = 0; r < rows; ++r) {
//			for (int c = 0; c < cols; ++c) {
//				hd.get(r, c, pixels);
//				int pv1 = pixels[0] & 0xff;
//				vd.get(r, c + 1, pixels);
//				int pv2 = pixels[0] & 0xff;
//				
//				int pv = pv1 + pv2;
//				if(pv > 255){
//					pv = 255;
//				}
//				pixels[0] = (byte)pv;
//				result.put(r, c, pixels);
//			}
//		}
////		Model.saveImage("Edges", temp);
////		temp = Util.binary(temp, threshold);
////		Model.saveImage("Binary Edges", temp);
//		return result;
//	}

    public static Mat hdiff(Mat image) {
        Mat sub0 = image.colRange(0, image.cols() - 1);
        Mat sub1 = image.colRange(1, image.cols());
        Mat dst = new Mat(sub0.size(), image.type());
        Core.absdiff(sub1, sub0, dst);
        return dst;
    }

//	static Mat hdiff2(Mat image){
//		int cols = image.cols();
//		int rows = image.rows();
////		byte[] pixels = new byte[1];
//		Mat temp = Mat.zeros(rows, cols, CvType.CV_8U);
//		
//		int cols1 = cols - 1;
//		for (int r = 0; r < rows; ++r) {
//			for (int c = 0; c < cols1; ++c) {
//				image.get(r, c, pixels);
//				int pv = pixels[0] & 0xff;
//				image.get(r, c + 1, pixels);
//				int pv2 = pixels[0] & 0xff;
//				
//				pixels[0] = (byte)Math.abs(pv2 - pv);
//				temp.put(r, c, pixels);
//			}
//		}
//		return temp;
//	}


//---------------------------For Debug-----------------------

    public static void saveMark(String name, Mat image, Rect rect) {
        if (Config.isDebug) {
            if (rect.width > 0) {
                Mat clone = image.clone();
                mark(clone, rect);
                MainModel.saveImage(name, clone);
            }
        }
    }

    public static void mark(Mat image, Rect rect) {
        if (rect.width > 0) {
            int right = rect.x + rect.width;
            int bottom = rect.y + rect.height;
            Core.rectangle(image, new Point(rect.x, rect.y), new Point(right, bottom), new Scalar(0, 0, 255));
        }
    }

    static Mat getColorImage(Mat image) {
        Mat cloned;
        if (image.channels() == 1) {
            cloned = new Mat();
            Imgproc.cvtColor(image, cloned, Imgproc.COLOR_GRAY2BGR);
        } else {
            cloned = image.clone();
        }
        return cloned;
    }

    public static void mark(String name, Mat image, List<Rect> rects) {
        if (Config.isDebug) {
            Mat cloned = getColorImage(image);
            for (Rect r : rects) {
                mark(cloned, r);
            }
            MainModel.saveImage(name, cloned);
        }
    }

}
