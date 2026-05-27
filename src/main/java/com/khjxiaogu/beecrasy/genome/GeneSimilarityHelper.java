/*
 *
 * Copyright (C) 2026 khjxiaogu
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

package com.khjxiaogu.beecrasy.genome;

import java.util.List;
import java.util.Objects;

import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;

public class GeneSimilarityHelper {
	public static <T extends AllelesHolder> long checkSimilarityPoint(T[] template,T[] subject) {
		return (checkSimilarityPoint(template[0],subject[0])<<32)+checkSimilarityPoint(template[1],subject[1]);
	}
	public static long checkSimilarityPoint(AllelesHolder template,AllelesHolder subject) {
		int point=0;
		List<ProductItem> tproduct=template.getAllele(Genes.PRODUCTS);
		List<ProductItem> sproduct=subject.getAllele(Genes.PRODUCTS);
		if(Objects.equals(tproduct, sproduct))
			point+=100000;
		else {
			point+=10000*BeecrasyMath.countCommonElements(tproduct, sproduct);
		}
		if(template.getAllele(Genes.BIOTOPE)==subject.getAllele(Genes.BIOTOPE))
			point+=1000;
		if(template.getAllele(Genes.TEMPERATURE)==subject.getAllele(Genes.TEMPERATURE))
			point+=100;
		if(template.getAllele(Genes.HUMIDITY)==subject.getAllele(Genes.HUMIDITY))
			point+=100;
		for(Gene<?> id:GeneRegistry.getGeneTypesUnordered()) {
			if(id==Genes.PRODUCTS)
				continue;
			if(id==Genes.BIOTOPE)
				continue;
			if(id==Genes.TEMPERATURE)
				continue;
			if(id==Genes.HUMIDITY)
				continue;
			if(template.getAllele(id)==subject.getAllele(id))
				point++;
		}
		return point;
	}
}
