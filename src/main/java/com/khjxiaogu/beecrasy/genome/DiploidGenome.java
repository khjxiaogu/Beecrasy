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

import com.khjxiaogu.beecrasy.components.GenomeComponent;

/**
 * 二倍体基因组记录，包含母方（maternal）和父方（paternal）两个单倍基因组构建器。
 *
 * @param maternal 母方基因组构建器
 * @param paternal 父方基因组构建器
 */
public record DiploidGenome(Genome.Builder maternal,Genome.Builder paternal) {
	/**
	 * 将二倍体基因组转换为 {@link GenomeComponent} 组件。
	 *
	 * @return 基因组组件
	 */
	public GenomeComponent toComponent() {
		return new GenomeComponent(false,maternal.build(),paternal.build());
	}
}
