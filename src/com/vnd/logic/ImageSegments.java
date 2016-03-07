package com.vnd.logic;

import com.vnd.model.LineGroup;
import com.vnd.model.LinePixels;
import com.vnd.model.LineSegments;
import com.vnd.model.Segment;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Quietlife on 2016/2/17.
 */
public class ImageSegments {
    static class Item{
        int maxD;
        List<LineSegments> lines;

//        public Item(int maxD, List<LineSegments> lines){
//            this.maxD = maxD;
//            this.lines = lines;
//        }
    }
    private List<Item> items = new ArrayList<>();
    public Mat edges;

    public ImageSegments(Mat edges){
        this.edges = edges;
    }

    private List<LineSegments> findLineSegments(int maxD, List<LineSegments> previous) {
        List<LineSegments> result = new ArrayList<>(previous.size());
        for (LineSegments line : previous) {
            result.add(merge(maxD, line));
        }
        return result;
    }

    private LineSegments merge(int maxD, LineSegments old) {
        if (old.segments.size() == 0) {
            return old;
        }
        LineSegments line = new LineSegments();
        line.line = old.line;
        List<Segment> segments = new ArrayList<>();
        line.segments = segments;
        Segment pre = old.segments.get(0).clone();
        segments.add(pre);
        for (int i = 1; i < old.segments.size(); ++i) {
            Segment current = old.segments.get(i);
            if (current.start - pre.getEnd() + 2 > maxD) {
                pre = current.clone();
                segments.add(pre);
            } else {
                pre.setEnd(current.getEnd());
            }
        }
        return line;
    }

    LineSegments filterSegments(LineSegments line, int minWidth) {
        List<Segment> found = new ArrayList<>();
        for (Segment s : line.segments) {
            if (s.length > minWidth) {
                found.add(s.clone());
            }
        }
        LineSegments result = new LineSegments();
        result.segments = found;
        result.line = line.line;
        return result;
    }

    private List<LineSegments> filterLines(List<LineSegments> lines, int minWidth){
        List<LineSegments> result = new ArrayList<>();
        for(LineSegments line : lines){
            LineSegments filtered = filterSegments(line, minWidth);
            if(filtered.segments.size() > 0){
                result.add(filtered);
            }
        }
        return result;
    }

    public List<LineSegments> find(int maxD, int minWidth){
        List<LineSegments> lines = findWithCache(maxD);
        return filterLines(lines, minWidth);
    }

    private List<LineSegments> findWithCache(int maxD){
        for(Item item : items){
            if(item.maxD == maxD){
                return item.lines;
            }
        }
        int index = -1;
        for(int i=0; i<items.size(); ++i){
            if(items.get(i).maxD > maxD){
                break;
            }else{
                index = i;
            }
        }
        Item item = new Item();
        item.maxD = maxD;
        if(index == -1){
            item.lines = findWithoutCache(maxD);
            items.add(item);
        }else {
            List<LineSegments> previous = items.get(index).lines;
            item.lines = findLineSegments(maxD, previous);
            items.add(index + 1, item);
        }
        return item.lines;
    }

    private List<LineSegments> findWithoutCache(int maxD) {
        int cols = edges.cols();
        int rows = edges.rows();
//		byte[] pixels = new byte[1];

//		BinaryNeighbor neighbor = new BinaryNeighbor(edges, maxD);
        List<LineSegments> result = new ArrayList<>(edges.rows());

//		int[] line = new int[cols];
        byte[] pixels = new byte[(int)edges.total()];
        edges.get(0, 0, pixels);
        for (int r = 0; r < rows; ++r) {
            LineSegments lineSegments = new LineSegments();
            lineSegments.line = r;
            int start = r * cols;
            LinePixels neighbor = new LinePixels(pixels, start, start + cols);
            int pre = neighbor.nextPixelPos() - start;
            if (pre < 0) {
                lineSegments.segments = Collections.emptyList();
                result.add(lineSegments);
                continue;
            }
            List<Segment> segments = new ArrayList<>();
//			lineSegs.segments = segs;
            Segment seg = new Segment(pre, 1);
            segments.add(seg);

            for (int p = neighbor.nextPixelPos() - start; p >= 0; p = neighbor.nextPixelPos() - start) {
                if (p - pre > maxD) {
                    seg.setEnd(pre + 1);
                    seg = new Segment(p, 1);
                    segments.add(seg);
                } else {
                    ++seg.pixels;
                }
                pre = p;
            }
            if (pre >= 0) {
                seg.setEnd(pre + 1);
            }

//            List<Segment> found = new ArrayList<>();
//            for (Segment s : segs) {
//                if (s.length > minWidth) {
//                    found.add(s);
//                }
//            }

            lineSegments.segments = segments;
            result.add(lineSegments);
        }
        return result;
    }
}
