package com.khjxiaogu.beecrasy.genome.gene;

public class NumericAllele extends BaseAllele {
	private final float number;

	public NumericAllele(String id, float number) {
		super(id);
		this.number = number;
	}

	public float getNumber() {
		return number;
	}

}
