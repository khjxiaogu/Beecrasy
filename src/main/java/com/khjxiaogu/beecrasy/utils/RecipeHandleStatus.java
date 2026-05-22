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

public enum RecipeHandleStatus {
	SUCCEED,BLOCKED,FAILED;
	public static RecipeHandleStatus success(boolean isSuccess) {
		return isSuccess?SUCCEED:BLOCKED;
	}
	public boolean resetsProcess() {
		return this==SUCCEED||this==FAILED;
	}
}
