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

package com.khjxiaogu.beecrasy.client.apistle;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * 标签页管理器：控制固定大小的标签栏中，哪些标签可见，并在需要时显示前后翻页按钮。
 * 翻页行为：点击“下一页/上一页”会跳转一整个页面（移动的标签数 = 当前页显示的标签个数）。
 */
public class TabPageManager {
    // 特殊标记：表示翻页按钮
    public static final int PREV = -1;
    public static final int NEXT = -2;

    private int totalTabs;      // 总标签数 M
    private int visibleCount;   // 界面可显示的标签槽位数 N
    private int currentStart;   // 当前可见窗口的第一个标签索引（0-based）

    /**
     * @param totalTabs   总标签数 M
     * @param visibleCount 界面能同时显示的标签槽位数 N
     */
    public TabPageManager(int totalTabs, int visibleCount) {
        if (totalTabs < 0) totalTabs = 0;
        if (visibleCount < 1) visibleCount = 1;
        this.totalTabs = totalTabs;
        this.visibleCount = visibleCount;
        this.currentStart = 0;
        adjustCurrentStart();
    }

    /**
     * 获取当前界面应显示的项列表。
     * 列表中每个元素是 Integer：
     *   PREV (-1) 代表上一页按钮，
     *   NEXT (-2) 代表下一页按钮，
     *   非负整数   代表对应标签的索引。
     */
    public IntList getDisplayItems() {
    	IntList items = new IntArrayList();

        if (totalTabs <= visibleCount) {
            for (int i = 0; i < totalTabs; i++) {
                items.add(i);
            }
            return items;
        }

        boolean hasPrev = currentStart > 0;

        int maxTabs = visibleCount;
        if (hasPrev) {
            maxTabs--; 
        }

        int tabsToShow;
        boolean hasNext;

        if (currentStart + maxTabs >= totalTabs) {
            tabsToShow = totalTabs - currentStart;
            hasNext = false;
        } else {
            // 右边还需要预留一个 NEXT 位置
            tabsToShow = maxTabs - 1;
            hasNext = true;
        }

        if (hasPrev) {
            items.add(PREV);
        }
        for (int i = 0; i < tabsToShow; i++) {
            items.add(currentStart + i);
        }
        if (hasNext) {
            items.add(NEXT);
        }

        return items;
    }

    public void pageForward() {
        if (hasNext()) {
            int step = getPageSize();
            currentStart += step;
            adjustCurrentStart();
        }
    }

    public void pageBackward() {
        if (hasPrev()) {
            int step = getPageSize();
            currentStart -= step;
            adjustCurrentStart();
        }
    }

    public void goToTab(int index) {
        if (index >= 0 && index < totalTabs) {
            currentStart = index;
            adjustCurrentStart();
        }
    }

    public boolean hasNext() {
        if (totalTabs <= visibleCount) return false;
        int maxTabs = visibleCount - (currentStart > 0 ? 1 : 0);
        return currentStart + maxTabs < totalTabs;
    }

    public boolean hasPrev() {
        return totalTabs > visibleCount && currentStart > 0;
    }
    private int getPageSize() {
        return (totalTabs > visibleCount) ? visibleCount - 1 : totalTabs;
    }
    public int getTotalTabs() {
        return totalTabs;
    }

    public void setTotalTabs(int totalTabs) {
        this.totalTabs = Math.max(0, totalTabs);
        adjustCurrentStart();
    }

    public int getVisibleCount() {
        return visibleCount;
    }

    public void setVisibleCount(int visibleCount) {
        this.visibleCount = Math.max(1, visibleCount);
        adjustCurrentStart();
    }

    public int getCurrentStart() {
        return currentStart;
    }

    private void adjustCurrentStart() {
        if (totalTabs == 0) {
            currentStart = 0;
            return;
        }
        if(currentStart==1)
        	currentStart=0;
        if (currentStart < 0) currentStart = 0;
        if (currentStart >= totalTabs) currentStart = totalTabs - 1;
    }
}