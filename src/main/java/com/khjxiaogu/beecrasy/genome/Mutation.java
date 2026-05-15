package com.khjxiaogu.beecrasy.genome;

import net.minecraft.util.RandomSource;

public interface Mutation {
	/**
	 * 执行突变
	 * @param genome 子代的基因组
	 * @param rnd 随机序列
	 * @return 是否继续执行后续突变
	 * 
	 * */
	boolean mutate(BeeHiveParameters params,DiploidGenome genome,RandomSource rnd);
	
	default int priority(){
		return 1000;
	}
}
