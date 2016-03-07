package com.vnd.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Rect;

public class Merger {
	private List<Rect> rects;
//	private int fontSize;
	private int[] vprojection;
	private List<List<Rect>> possibleMerges = new ArrayList<>();
	private int fontCount = 7;
	private float maxVBarWidth;
//	private float maxDiff;

	// static class Continous{
	// int start = 0;
	// int count = 0;
	// }
	public Merger(List<Rect> rects, int[] vprojection){
		this(rects, vprojection, 7);
	}

	public Merger(List<Rect> rects, int[] vprojection, int fontCount) {
		this.rects = rects;
		this.vprojection = vprojection;
		int fontSize = CharExtractor2.getAvgFontSize(rects);
		this.fontCount = fontCount;
		possibleMerges.add(rects);
		if(fontSize < 10){
			maxVBarWidth = 0.65f * fontSize;
		}else if(fontSize < 20) {
            maxVBarWidth = 0.5f * fontSize;
        }else{
			maxVBarWidth = 0.4f * fontSize;
		}
//		maxDiff = fontSize * 0.2f;
	}

	public boolean isVBar(Rect rect) {
//		float diff = fontSize < 10 ? 0.6f : 0.5f;
		if (rect.width > maxVBarWidth) {
			return false;
		}
		int[] hproj2 = Arrays.copyOfRange(vprojection, rect.x, rect.x + rect.width);
		int minHeight = rect.height / 3;
		for (int v : hproj2) {
			if (v > minHeight) {
				return true;
			}
		}
		return false;
	}

    int getVBarHeight(Rect rect){
        int[] hproj2 = Arrays.copyOfRange(vprojection, rect.x, rect.x + rect.width);
        int maxHeight = 0;
        for (int v : hproj2) {
            if (v > maxHeight) {
                maxHeight = v;
            }
        }
        return maxHeight;
    }

    void removeShortVBars(List<Rect> rects, List<Integer> vBarIndices){
        if(vBarIndices.size() == 0){
            return;
        }
        int height = rects.get(0).height;
        int minHeight = (int)(height * 0.6);
        List<Integer> toRemove = new ArrayList<>();
        for(Integer i : vBarIndices){
            if(getVBarHeight(rects.get(i)) < minHeight){
                toRemove.add(i);
            }
        }
        Collections.sort(toRemove);
        for(int i=toRemove.size() - 1; i>=0; --i){
            rects.remove(toRemove.get(i));
        }
    }
	
	public List<List<Rect>> getMerges(){
		merge(rects);
		return possibleMerges;
	}
	
	List<Integer> getVBarIndecis(List<Rect> rects){
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < rects.size(); ++i) {
			Rect r = rects.get(i);
			if (isVBar(r)) {
				result.add(i);
			}
		}
		return result;
	}

	private void merge(List<Rect> rects) {
		if(rects.size() < fontCount + 1){
			return;
		}
		List<Integer> vbarIndecis = getVBarIndecis(rects);
        if(vbarIndecis.size() > 7){
            removeShortVBars(rects, vbarIndecis);
        }
		if(vbarIndecis.size() > 7 || vbarIndecis.size() < 1){
			return;
		}
		for(int i : vbarIndecis){
//		for (int i = 0; i < rects.size(); ++i) {
//			Rect r = rects.get(i);
//			if (isVBar(r)) {
			if(vbarIndecis.indexOf(i-1) < 0){
				List<Rect> preMerged = mergePre(rects, i);
				if(preMerged != null){
					possibleMerges.add(preMerged);
					merge(preMerged);
				}
			}
				
				List<Rect> nextMerged = mergeNext(rects, i);
				if(nextMerged != null){
					possibleMerges.add(nextMerged);
					merge(nextMerged);
				}
//			}
		}
	}
	
	boolean isFine(int width, int fontSize, float maxDiff){
		if(width < fontSize){
			return true;
		}else{
			return width - fontSize < maxDiff;
		}
	}
	
	List<Rect> cloneRects(List<Rect> rects){
		List<Rect> copied = new ArrayList<>();
		for(Rect r : rects){
			copied.add(r.clone());
		}
		return copied;
	}
	
	List<Rect> mergePre(List<Rect> rects, int index){
		List<Rect> copied = cloneRects(rects);
		Rect cur = copied.get(index);
		if(index > 0){
			Rect pre = copied.get(index - 1);
			pre.width = cur.x + cur.width - pre.x;
			copied.remove(index);
			int fontSize = CharExtractor2.getAvgFontSize(copied);
			if(isFine(pre.width, fontSize, fontSize * 0.2f)){
				return copied;
			}
		}
		return null;
	}
	
	List<Rect> mergeNext(List<Rect> rects, int index){
		List<Rect> copied = cloneRects(rects);
		Rect cur = copied.get(index);
		if(index < copied.size() - 1){
			Rect next = copied.get(index + 1);
			cur.width = next.x + next.width - cur.x;
			int fontSize = CharExtractor2.getAvgFontSize(copied);
			if(isFine(next.width, fontSize, fontSize * 0.2f)){
				copied.remove(index + 1);
				return copied;
			}
		}
		return null;
	}
}
