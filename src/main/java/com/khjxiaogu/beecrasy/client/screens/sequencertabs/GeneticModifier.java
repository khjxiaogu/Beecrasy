package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import com.khjxiaogu.beecrasy.genome.AllelesHolder;
import com.khjxiaogu.beecrasy.genome.Gene;

public interface GeneticModifier extends AllelesHolder{

	<T> void setAllele(Gene<T> type,T value);

}
