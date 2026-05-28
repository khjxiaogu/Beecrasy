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

package com.khjxiaogu.beecrasy.blocks;

import com.khjxiaogu.beecrasy.Beecrasy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;

public abstract class BeecrasyBlockEntity extends BlockEntity {

	public BeecrasyBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
		super(pType, pWorldPosition, pBlockState);
	}

	public void sendUpdated() {
		if (this.level != null) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
		this.setChanged();
	}

	public abstract void readCustomNBT(ValueInput nbt, boolean isClient);

	public abstract void writeCustomNBT(ValueOutput nbt, boolean isClient);

	private boolean loadingMessage = false;

	@Override
	public void onDataPacket(Connection net, ValueInput valueInput) {
		try {
			loadingMessage = true;
			super.onDataPacket(net, valueInput);
		} finally {
			loadingMessage = false;
		}
	}

	public abstract void tick();

	public Object getCapability(BlockCapability<?, Direction> type, Direction d) {
		return null;
	};

	@Override
	public void loadAdditional(ValueInput valueInput) {
		this.readCustomNBT(valueInput, loadingMessage);
		super.loadAdditional(valueInput);

	}

	@Override
	protected void saveAdditional(ValueOutput valueOutput) {
		this.writeCustomNBT(valueOutput, false);
		super.saveAdditional(valueOutput);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Beecrasy.LOGGER)) {
			TagValueOutput tvo = TagValueOutput.createWithContext(reporter, registries);
			writeCustomNBT(tvo, true);
			return tvo.buildResult();
		}
	}

	public boolean isHandlingMessage() {
		return loadingMessage;
	}
}
