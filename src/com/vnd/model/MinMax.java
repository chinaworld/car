package com.vnd.model;

public class MinMax {
	public int min, max;

	// boolean needReBinary = false;
	public MinMax(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int width() {
		return max - min + 1;
	}

    public MinMax clone(){
        return new MinMax(min, max);
    }
	
	public static int getMax(int[] a){
		int max = Integer.MIN_VALUE;
		for(int v : a){
			if(v > max){
				max = v;
			}
		}
		return max;
	}
	
	public static boolean allZero(int[] a){
		for(int i : a){
			if(i != 0){
				return false;
			}
		}
		return true;
	}
	
	public static MinMax getMinMaxIndices(int[] filteredProjection) {
		int min = 0;
		int max = filteredProjection.length - 1;
		int mvalue = getMax(filteredProjection);
		if(mvalue == 0){
			return new MinMax(min, max);
		}
		for (int i = 0; i < filteredProjection.length; ++i) {
			if (filteredProjection[i] == mvalue) {
				min = i;
				break;
			} else {
				++min;
			}
		}
		for (int i = filteredProjection.length - 1; i >= 0; --i) {
			if (filteredProjection[i] == mvalue) {
				max = i;
				break;
			} else {
				--max;
			}
		}
		return new MinMax(min, max);
	}

	public static MinMax getCnMinMaxIndices(int[] filteredProjection) {
		int min = 0;
		int max = filteredProjection.length - 1;
		for (int i = 0; i < filteredProjection.length; ++i) {
			if (filteredProjection[i] > 0) {
				min = i;
				break;
			} else {
				++min;
			}
		}
		for (int i = filteredProjection.length - 1; i >= 0; --i) {
			if (filteredProjection[i] > 0) {
				max = i;
				break;
			} else {
				--max;
			}
		}
		return new MinMax(min, max);
	}
}