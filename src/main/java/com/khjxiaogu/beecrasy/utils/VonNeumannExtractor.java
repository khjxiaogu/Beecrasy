/** 
* Copyright (c) 2026 khjxiaogu
*
* This file is part of Beecrasy.
*
* Beecrasy is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, version 3.
* 
* Beecrasy is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with Beecrasy. If not, see <https://www.gnu.org/licenses/>.
*/
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
