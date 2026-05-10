package com.khjxiaogu.beecrasy.genome.gene;

public class BaseAllele implements Allele {
	private final String id;
	
	@Override
	public String getId() {
		return id;
	}

	public BaseAllele(String id) {
		super();
		this.id = id;
	}

}
