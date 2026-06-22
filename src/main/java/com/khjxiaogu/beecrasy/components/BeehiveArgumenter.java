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

package com.khjxiaogu.beecrasy.components;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public record BeehiveArgumenter(BeeHiveArgumentation modifiers,boolean consumeOnUse) {
	public static final Codec<BeehiveArgumenter> CODEC=RecordCodecBuilder.create(t->
	t.group(BeeHiveArgumentation.CODEC.fieldOf("modifiers").forGetter(BeehiveArgumenter::modifiers),
	Codec.BOOL.fieldOf("consumeOnUse").forGetter(BeehiveArgumenter::consumeOnUse)
	).apply(t, BeehiveArgumenter::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,BeehiveArgumenter> STREAM_CODEC=StreamCodec.composite(
		BeeHiveArgumentation.STREAM_CODEC,BeehiveArgumenter::modifiers,
		ByteBufCodecs.BOOL,BeehiveArgumenter::consumeOnUse,
		BeehiveArgumenter::new);
	public static BeeHiveArgumentation extractArgumentation(ServerLevel serverLevel,ResourceHandler<ItemResource> inv,int slot,TransactionContext root) {
		ItemResource ir=inv.getResource(slot);
		BeehiveArgumenter argu=ir.get(Components.ARGUMENTATION);
		if(argu!=null) {
			if(argu.consumeOnUse()) {
				try(Transaction trans=Transaction.open(root)){
					int extracted=inv.extract(slot, ir, 1, trans);
					if(extracted==1) {
						if(ir.has(DataComponents.DAMAGE)&&ir.has(DataComponents.MAX_DAMAGE)) {
							if(ir.has(DataComponents.UNBREAKABLE)) {
								return argu.modifiers();
							}
							ItemStack stack=ir.toStack();
							stack.hurtAndBreak(1, serverLevel, null,_->{});
							if(!stack.isEmpty()) {
								if(inv.insert(slot, ItemResource.of(stack), extracted, trans)==extracted) {
									trans.commit();
									return argu.modifiers();
								}
							}
						}else {
							trans.commit();
							return argu.modifiers();
						}
					}
				}
				return null;
			}
			return argu.modifiers();
		}
		return null;
	}
}
