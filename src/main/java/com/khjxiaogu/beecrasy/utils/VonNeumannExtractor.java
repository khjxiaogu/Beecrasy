package com.khjxiaogu.beecrasy.utils;

import java.util.BitSet;
import java.util.function.LongSupplier;

public class VonNeumannExtractor {
	LongSupplier random;
	BitSet bits;
	int index;
	long[] num=new long[1];
	public VonNeumannExtractor(LongSupplier random) {
		this.random=random;
	}
	public boolean nextBoolean() {
		
		while(true) {
			boolean first=nextBit();
			boolean second=nextBit();
			if(first^second) {
				return first;
			}
		}
	}
	
	private boolean nextBit() {
		refillBitsetIfNeeded();
		return bits.get(index++);
	}
	public void refillBitsetIfNeeded() {
		if(bits==null||index>=bits.length())
			refillBitset();
	}
	public void refillBitset() {
		num[0]=random.getAsLong();
		bits=BitSet.valueOf(num);
		index=0;
	}

}
