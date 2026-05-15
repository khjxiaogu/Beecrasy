package com.khjxiaogu.beecrasy.genome;

public interface AllelesHolder {

	<T> T getAllele(Gene<T> type);

}