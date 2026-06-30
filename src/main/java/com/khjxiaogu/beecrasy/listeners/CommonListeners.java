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

package com.khjxiaogu.beecrasy.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.ibm.icu.util.Calendar;
import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Attachments;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Capability;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Entities;
import com.khjxiaogu.beecrasy.beedi.ServerBeediManager;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.components.WorldCalendar;
import com.khjxiaogu.beecrasy.entity.BeeSwarmEntity;
import com.khjxiaogu.beecrasy.events.BeeEnvironmentValidateEvent;
import com.khjxiaogu.beecrasy.events.NaturalBeeGenomeGenerateEvent;
import com.khjxiaogu.beecrasy.genome.Gene;
import com.khjxiaogu.beecrasy.genome.GeneRegistry;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.gene.Allele;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;
import com.khjxiaogu.beecrasy.genome.gene.Humidity;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.genome.gene.Temperature;
import com.khjxiaogu.beecrasy.mail.PlayerPostalOffice;
import com.khjxiaogu.beecrasy.mail.PostalOffice;
import com.khjxiaogu.beecrasy.network.OpenApistleMessage;
import com.khjxiaogu.beecrasy.network.PacketHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.DataResult;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Beecrasy.MODID)
public class CommonListeners {
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event) {
		Player p=event.getEntity();
		Long comp=p.getData(Attachments.RANDOM_SEED);
		//Neoforge会初始化一个默认值，这里只做备用
		if(comp==null) {
			comp=RandomSupport.generateUniqueSeed();
			p.setData(Attachments.RANDOM_SEED, comp);
		}
	}
	@SubscribeEvent
	public static void onAttributeCreation(EntityAttributeCreationEvent event) {
		event.put(Entities.BEE_SWARM.get(), BeeSwarmEntity.createAttributes().build());
	}
	
	@SubscribeEvent
	public static void onCapabilityInject(RegisterCapabilitiesEvent event) {
		//skep
		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.SKEP_BLOCKENTITY.get(), (be,ctx)->{
			if(ctx==Direction.DOWN)
				return be.component.getProductInv();
			return be.component.getExternInv();
		});
		//hive
		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.HIVE_BLOCKENTITY.get(), (be,ctx)->{
			if(ctx==Direction.DOWN)
				return be.component.getProductInv();
			return be.component.getExternInv();
		});
		//press
		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.PRESS_BLOCKENTITY.get(), (be,_)->{
			return be.getExternInv();
		});
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, Blocks.PRESS_BLOCKENTITY.get(), (be,_)->{
			return be.getExternTank();
		});
		//sequencer

		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.SEQUENCER_BLOCKENTITY.get(), (be,_)->{
			return be.invTransport;
		});
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, Blocks.SEQUENCER_BLOCKENTITY.get(), (be,_)->{
			return be.tank;
		});
		event.registerBlockEntity(Capabilities.Energy.BLOCK, Blocks.SEQUENCER_BLOCKENTITY.get(), (be,_)->{
			return be.energyTransport;
		});
		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.BEEDIBOX_BLOCKENTITY.get(), (be,_)->{
			return be.disk;
		});
		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.BEE_CITY_CORE_BLOCKENTITY.get(), (be,ctx)->{
			if(ctx==Direction.DOWN)
				return be.component.getProductInv();
			return be.component.getExternInv();
		});
		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.BEE_CITY_COMB_BLOCKENTITY.get(), (be,_)->{
			return be.container;
		});
		event.registerBlockEntity(Capabilities.Item.BLOCK, Blocks.BEE_CITY_QUEEN_BLOCKENTITY.get(), (be,ctx)->{
			if(ctx==Direction.DOWN)
				return be.component.getProductInv();
			return be.component.getExternInv();
		});
		event.registerBlockEntity(Capability.BEE_CITY_BLOCK, Blocks.BEE_CITY_COMB_BLOCKENTITY.get(), (be,_)->{
			return be.slots;
		});
		event.registerBlockEntity(Capability.BEE_CITY_BLOCK, Blocks.BEE_CITY_QUEEN_BLOCKENTITY.get(), (be,_)->{
			return be.slots;
		});
	}
	@SubscribeEvent
	public static void onGenomeBuild(NaturalBeeGenomeGenerateEvent event) {
		if(event.params.type().is(BuiltinDimensionTypes.NETHER)) {
			event.genome.add(Genes.TEMPERATURE, Alleles.NETHER_TEMPERATURE);
		}else if(event.params.type().is(BuiltinDimensionTypes.NETHER)) {
			event.genome.add(Genes.TEMPERATURE, Alleles.ENDER_TEMPERATURE);
		}else{
			for(Humidity humid:Alleles.HUMIDITY) {
				if(humid.isNatural()&&humid.isValidFor(event.params)) {
					event.genome.add(Genes.HUMIDITY, humid);
					break;
				}
			}
			
			Humidity humid=event.genome.get(Genes.HUMIDITY);
			for(Temperature temp:Alleles.TEMPERATURE) {
				if(temp.isNatural()&&temp.isValidFor(event.params,humid)) {
					event.genome.add(Genes.TEMPERATURE, temp);
					break;
				}
			}
		}
	}
	@SubscribeEvent
	public static void onItemClick(RightClickItem ev) {
	
		String apistle=ev.getItemStack().get(Components.APISTLE);
		if(apistle!=null) {
			if(ev.getEntity() instanceof ServerPlayer player) {
				PacketHandler.sendToPlayer(player, new OpenApistleMessage(apistle,ev.getItemStack().getHoverName()));
			}
			ev.setCanceled(true);
			ev.setCancellationResult(InteractionResult.SUCCESS);
		}
			
	}
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void addCommands(RegisterCommandsEvent event) {
		var inspect = Commands.literal("inspect").executes((ctx)->{
			ServerPlayer sp=ctx.getSource().getPlayerOrException();
			ItemStack stack=sp.getMainHandItem();
			GenomeComponent genome=stack.get(Components.GENOME);
			
			if(genome!=null) {
				Consumer<Component> textAdder=sp::sendSystemMessage;
				genome=genome.asInspected();
				int i=0;
				for(Genome cgenome:genome) {
					textAdder.accept(Component.translatable("genome.beecrasy.genome"+i).withStyle(ChatFormatting.GOLD));
					for(Identifier gene:GeneRegistry.getDisplayOrder()) {
						textAdder.accept(Component.empty().append(GeneRegistry.get(gene).getReadableText()).append(": ").append(GeneRegistry.get(gene).getReadableText(cgenome)));
					}
					i++;
				}
				stack.set(Components.GENOME,genome);
			}
			return Command.SINGLE_SUCCESS;
		});
		var editProduct = Commands.literal("product")
			.then(Commands.literal("remove")
				.then(Commands.argument("index", IntegerArgumentType.integer(0))
					.then(Commands.argument("product", IntegerArgumentType.integer(0))
						.executes((ctx)->{
							int index=IntegerArgumentType.getInteger(ctx, "index");
							int product=IntegerArgumentType.getInteger(ctx, "product");
							ServerPlayer sp=ctx.getSource().getPlayerOrException();
							ItemStack stack=sp.getMainHandItem();
							GenomeComponent genome=stack.get(Components.GENOME);
							Genome.Builder b=null;
							List<ProductItem> list=new ArrayList<>();
							if(genome!=null) {
								if(genome.size()>index) {
									b=genome.getGenome(index).createBuilder();
									list.addAll(genome.getGenome(index).getAllele(Genes.PRODUCTS));
								}
							}else {
								genome=GenomeComponent.HAPLOID_EMPTY;
							}
							if(b==null)
								b=Genome.builder();
							list.remove(product);
							b.add(Genes.PRODUCTS, List.copyOf(list));
							stack.set(Components.GENOME,genome.setGenome(index, b.build()));
							return Command.SINGLE_SUCCESS;
				})
			)))
			.then(Commands.literal("get")
				.then(Commands.argument("index", IntegerArgumentType.integer(0))
					.executes((ctx)->{
						int index=IntegerArgumentType.getInteger(ctx, "index");
						ServerPlayer sp=ctx.getSource().getPlayerOrException();
						ItemStack stack=sp.getMainHandItem();
						GenomeComponent genome=stack.get(Components.GENOME);
						if(genome!=null) {
							List<ProductItem> data=genome.getGenome(index).getAllele(Genes.PRODUCTS);
							ctx.getSource().sendSuccess(()->NbtUtils.toPrettyComponent(ProductItem.CODEC.listOf().encodeStart(NbtOps.INSTANCE,data).getOrThrow()), false);
						}
						return Command.SINGLE_SUCCESS;
					})
					.then(Commands.argument("product", IntegerArgumentType.integer(0))
						.executes((ctx)->{
							int index=IntegerArgumentType.getInteger(ctx, "index");
							int product=IntegerArgumentType.getInteger(ctx, "product");
							ServerPlayer sp=ctx.getSource().getPlayerOrException();
							ItemStack stack=sp.getMainHandItem();
							GenomeComponent genome=stack.get(Components.GENOME);
							if(genome!=null) {
								List<ProductItem> data=genome.getGenome(index).getAllele(Genes.PRODUCTS);
								if(data.size()>product) {
									ctx.getSource().sendSuccess(()->NbtUtils.toPrettyComponent(ProductItem.CODEC.encodeStart(NbtOps.INSTANCE, data.get(product)).getOrThrow()), false);
								}else {
									ctx.getSource().sendFailure(Component.literal("EMPTY"));
								}
							}
							return Command.SINGLE_SUCCESS;
				})
			)))
			.then(Commands.literal("add")

				.then(Commands.argument("index", IntegerArgumentType.integer(0))
				.then(Commands.argument("item", ItemArgument.item(event.getBuildContext()))
					.executes((ctx)->{
						int index=IntegerArgumentType.getInteger(ctx, "index");
						ItemInput item=ItemArgument.getItem(ctx,"item");
						Biotope bt=null;
						ServerPlayer sp=ctx.getSource().getPlayerOrException();
						ItemStack stack=sp.getMainHandItem();
						GenomeComponent genome=stack.get(Components.GENOME);
						Genome.Builder b=null;
						List<ProductItem> list=new ArrayList<>();
						if(genome!=null) {
							if(genome.size()>index) {
								b=genome.getGenome(index).createBuilder();
								list.addAll(genome.getGenome(index).getAllele(Genes.PRODUCTS));
								bt=genome.getGenome(index).getAllele(Genes.BIOTOPE);
							}
						}else {
							genome=GenomeComponent.HAPLOID_EMPTY;
						}
						if(b==null)
							b=Genome.builder();
						if(bt==null)
							bt=Alleles.WILD;
						list.add(new ProductItem(bt,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
						b.add(Genes.PRODUCTS, List.copyOf(list));
						stack.set(Components.GENOME,genome.setGenome(index, b.build()));
						return Command.SINGLE_SUCCESS;
			})
				.then(Commands.argument("biotope", StringArgumentType.string()).suggests((_,builder)->{
					for(Biotope bt:Alleles.BIOTOPE) {
						builder.suggest(Alleles.BIOTOPE.getId(bt),()->Alleles.BIOTOPE.getReadableText(bt).getString());
					}
					return builder.buildFuture();
				}).executes((ctx)->{
					int index=IntegerArgumentType.getInteger(ctx, "index");
					ItemInput item=ItemArgument.getItem(ctx,"item");
					Biotope bt=Alleles.BIOTOPE.getAlleleType(StringArgumentType.getString(ctx, "biotope")).getOrThrow();
					ServerPlayer sp=ctx.getSource().getPlayerOrException();
					ItemStack stack=sp.getMainHandItem();
					GenomeComponent genome=stack.get(Components.GENOME);
					Genome.Builder b=null;
					List<ProductItem> list=new ArrayList<>();
					if(genome!=null) {
						if(genome.size()>index) {
							b=genome.getGenome(index).createBuilder();
							list.addAll(genome.getGenome(index).getAllele(Genes.PRODUCTS));
						}
					}else {
						genome=GenomeComponent.HAPLOID_EMPTY;
					}
					if(b==null)
						b=Genome.builder();
					list.add(new ProductItem(bt,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
					b.add(Genes.PRODUCTS, List.copyOf(list));
					stack.set(Components.GENOME,genome.setGenome(index, b.build()));
					return Command.SINGLE_SUCCESS;
		}).then(Commands.argument("recipe", IdentifierArgument.id()).executes((ctx)->{
					int index=IntegerArgumentType.getInteger(ctx, "index");
					ItemInput item=ItemArgument.getItem(ctx,"item");
					Identifier recipe=IdentifierArgument.getId(ctx, "recipe");
					Biotope bt=Alleles.BIOTOPE.getAlleleType(StringArgumentType.getString(ctx, "biotope")).getOrThrow();
					ServerPlayer sp=ctx.getSource().getPlayerOrException();
					ItemStack stack=sp.getMainHandItem();
					GenomeComponent genome=stack.get(Components.GENOME);
					Genome.Builder b=null;
					List<ProductItem> list=new ArrayList<>();
					if(genome!=null) {
						if(genome.size()>index) {
							b=genome.getGenome(index).createBuilder();
							list.addAll(genome.getGenome(index).getAllele(Genes.PRODUCTS));
						}
					}else {
						genome=GenomeComponent.HAPLOID_EMPTY;
					}
					if(b==null)
						b=Genome.builder();
					list.add(new ProductItem(bt,Optional.of(recipe),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
					b.add(Genes.PRODUCTS, List.copyOf(list));
					stack.set(Components.GENOME,genome.setGenome(index, b.build()));
					return Command.SINGLE_SUCCESS;
		})))))).then(Commands.literal("set")
			.then(Commands.argument("index", IntegerArgumentType.integer(0))
				.then(Commands.argument("product", IntegerArgumentType.integer(0))
			.then(Commands.argument("item", ItemArgument.item(event.getBuildContext()))
				.executes((ctx)->{
					int index=IntegerArgumentType.getInteger(ctx, "index");

					int product=IntegerArgumentType.getInteger(ctx, "product");
					ItemInput item=ItemArgument.getItem(ctx,"item");
					Biotope bt=null;
					ServerPlayer sp=ctx.getSource().getPlayerOrException();
					ItemStack stack=sp.getMainHandItem();
					GenomeComponent genome=stack.get(Components.GENOME);
					Genome.Builder b=null;
					List<ProductItem> list=new ArrayList<>();
					if(genome!=null) {
						if(genome.size()>index) {
							b=genome.getGenome(index).createBuilder();
							list.addAll(genome.getGenome(index).getAllele(Genes.PRODUCTS));
							bt=genome.getGenome(index).getAllele(Genes.BIOTOPE);
						}
					}else {
						genome=GenomeComponent.HAPLOID_EMPTY;
					}
					if(b==null)
						b=Genome.builder();
					if(bt==null)
						bt=Alleles.WILD;
					if(list.size()>product)
						list.set(product,new ProductItem(bt,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
					else
						list.add(product,new ProductItem(bt,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
					b.add(Genes.PRODUCTS, List.copyOf(list));
					stack.set(Components.GENOME,genome.setGenome(index, b.build()));
					return Command.SINGLE_SUCCESS;
		})
			.then(Commands.argument("biotope", StringArgumentType.string()).suggests((_,builder)->{
				for(Biotope bt:Alleles.BIOTOPE) {
					builder.suggest(Alleles.BIOTOPE.getId(bt),()->Alleles.BIOTOPE.getReadableText(bt).getString());
				}
				return builder.buildFuture();
			}).executes((ctx)->{
				int index=IntegerArgumentType.getInteger(ctx, "index");
				int product=IntegerArgumentType.getInteger(ctx, "product");
				ItemInput item=ItemArgument.getItem(ctx,"item");
				Biotope bt=Alleles.BIOTOPE.getAlleleType(StringArgumentType.getString(ctx, "biotope")).getOrThrow();
				ServerPlayer sp=ctx.getSource().getPlayerOrException();
				ItemStack stack=sp.getMainHandItem();
				GenomeComponent genome=stack.get(Components.GENOME);
				Genome.Builder b=null;
				List<ProductItem> list=new ArrayList<>();
				if(genome!=null) {
					if(genome.size()>index) {
						b=genome.getGenome(index).createBuilder();
						list.addAll(genome.getGenome(index).getAllele(Genes.PRODUCTS));
					}
				}else {
					genome=GenomeComponent.HAPLOID_EMPTY;
				}
				if(b==null)
					b=Genome.builder();
				if(list.size()>product)
					list.set(product,new ProductItem(bt,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
				else
					list.add(product,new ProductItem(bt,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
				b.add(Genes.PRODUCTS, List.copyOf(list));
				stack.set(Components.GENOME,genome.setGenome(index, b.build()));
				return Command.SINGLE_SUCCESS;
	}).then(Commands.argument("recipe", IdentifierArgument.id()).executes((ctx)->{
				int index=IntegerArgumentType.getInteger(ctx, "index");
				int product=IntegerArgumentType.getInteger(ctx, "product");
				ItemInput item=ItemArgument.getItem(ctx,"item");
				Identifier recipe=IdentifierArgument.getId(ctx, "recipe");
				Biotope bt=Alleles.BIOTOPE.getAlleleType(StringArgumentType.getString(ctx, "biotope")).getOrThrow();
				ServerPlayer sp=ctx.getSource().getPlayerOrException();
				ItemStack stack=sp.getMainHandItem();
				GenomeComponent genome=stack.get(Components.GENOME);
				Genome.Builder b=null;
				List<ProductItem> list=new ArrayList<>();
				if(genome!=null) {
					if(genome.size()>index) {
						b=genome.getGenome(index).createBuilder();
						list.addAll(genome.getGenome(index).getAllele(Genes.PRODUCTS));
					}
				}else {
					genome=GenomeComponent.HAPLOID_EMPTY;
				}
				if(b==null)
					b=Genome.builder();
				if(list.size()>product)
					list.set(product,new ProductItem(bt,Optional.of(recipe),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
				else
					list.add(product,new ProductItem(bt,Optional.of(recipe),ItemStackTemplate.fromNonEmptyStack(item.createItemStack(1))));
				b.add(Genes.PRODUCTS, List.copyOf(list));
				stack.set(Components.GENOME,genome.setGenome(index, b.build()));
				return Command.SINGLE_SUCCESS;
	})))))));
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var editGenome = Commands.literal("genome").then(
			Commands.argument("index", IntegerArgumentType.integer(0)).then(
				Commands.argument("genome", IdentifierArgument.id())
				.suggests((_,builder)->{
					for(Identifier id:GeneRegistry.getEnumTypes().keySet()) {
						builder.suggest(id.toString(),()->GeneRegistry.get(id).getReadableText().getString());
					}
					return builder.buildFuture();
				}).executes((ctx)->{
					int index=IntegerArgumentType.getInteger(ctx, "index");
					Identifier type=IdentifierArgument.getId(ctx, "genome");
					ServerPlayer sp=ctx.getSource().getPlayerOrException();
					ItemStack stack=sp.getMainHandItem();
					GenomeComponent genome=stack.get(Components.GENOME);
					if(genome!=null) {
						if(genome.size()>index) {
							ctx.getSource().sendSuccess(()->GeneRegistry.get(type).getReadableText(genome.getGenome(index)), false);
							return Command.SINGLE_SUCCESS;
						}
					}
					ctx.getSource().sendFailure(Component.literal("EMPTY"));
					return Command.SINGLE_SUCCESS;
				}).then(Commands.argument("value", StringArgumentType.string()).suggests((ctx,builder)->{
					Identifier id=IdentifierArgument.getId(ctx, "genome");
					@SuppressWarnings({"rawtypes"})
					EnumAlleleType alleles=GeneRegistry.getEnumTypes().get(id);
					if(alleles!=null)
						for(Object allele:alleles) {
							builder.suggest(alleles.getId((Allele) allele),()->alleles.getReadableText((Allele) allele).getString());
						}
					return builder.buildFuture();
				}).executes((ctx)->{
					int index=IntegerArgumentType.getInteger(ctx, "index");
					Identifier type=IdentifierArgument.getId(ctx, "genome");
					DataResult<?> obj=GeneRegistry.getEnumTypes().get(type).getAlleleType(StringArgumentType.getString(ctx, "value"));
					ServerPlayer sp=ctx.getSource().getPlayerOrException();
					ItemStack stack=sp.getMainHandItem();
					GenomeComponent genome=stack.get(Components.GENOME);
					Genome.Builder b=null;
					if(genome!=null) {
						if(genome.size()>index) {
							b=genome.getGenome(index).createBuilder();
						}
					}else {
						genome=GenomeComponent.HAPLOID_EMPTY;
					}
					if(b==null)
						b=Genome.builder();
					b.<Object>add((Gene)GeneRegistry.get(type), obj.getOrThrow());
					stack.set(Components.GENOME,genome.setGenome(index, b.build()));
					return Command.SINGLE_SUCCESS;
				}))
				)
			);
		var calend=Commands.literal("calendar").executes((ctx)->{
			WorldCalendar secs=ctx.getSource().getServer().getDataStorage().computeIfAbsent(WorldCalendar.TYPE);
			Calendar calendar=Calendar.getInstance();
			calendar.set(2025, 5, 20,8,0,0);
			calendar.setTimeInMillis(calendar.getTimeInMillis()+secs.getSeconds() * 60000L);
			calendar.add(Calendar.SECOND, secs.getPartialSecs()*3);
			ctx.getSource().sendSystemMessage(Component.literal(String.format("%s-%02d-%02d %02d:%02d:%02d B.C.",
					calendar.get(Calendar.YEAR)-1734,calendar.get(Calendar.MONTH)+1,
					calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND))));
			return Command.SINGLE_SUCCESS;
		});
		var midi=Commands.literal("midi").then(
			Commands.argument("midi", IdentifierArgument.id())
			.then(
			Commands.argument("pos", BlockPosArgument.blockPos()).executes((ctx)->{
				ServerBeediManager.playSong(ctx.getSource().getLevel(),BlockPosArgument.getBlockPos(ctx, "pos"),
					IdentifierArgument.getId(ctx, "midi"),
					null, 0, 1);
				return Command.SINGLE_SUCCESS;
            }).then(Commands.argument("speed", FloatArgumentType.floatArg())
            	.suggests((_,builder)->builder.suggest(1).buildFuture())
            	.executes((ctx)->{
    				ServerBeediManager.playSong(ctx.getSource().getLevel(),BlockPosArgument.getBlockPos(ctx, "pos"),
    					IdentifierArgument.getId(ctx, "midi"), null, 0, FloatArgumentType.getFloat(ctx, "speed"));
    				return Command.SINGLE_SUCCESS;
                }).then(Commands.argument("pitch", IntegerArgumentType.integer())
                	.suggests((_,builder)->builder.suggest(0).buildFuture())
                	.executes((ctx)->{
    				ServerBeediManager.playSong(ctx.getSource().getLevel(),BlockPosArgument.getBlockPos(ctx, "pos"),
    					IdentifierArgument.getId(ctx, "midi"), null, IntegerArgumentType.getInteger(ctx, "pitch"), FloatArgumentType.getFloat(ctx, "speed"));
    				return Command.SINGLE_SUCCESS;
                }).then(Commands.argument("sound", IdentifierArgument.id())
            .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
            .executes((ctx)->{
    				ServerBeediManager.playSong(ctx.getSource().getLevel(),BlockPosArgument.getBlockPos(ctx, "pos"),
    					IdentifierArgument.getId(ctx, "midi"), IdentifierArgument.getId(ctx, "sound"),IntegerArgumentType.getInteger(ctx, "pitch"), FloatArgumentType.getFloat(ctx, "speed"));
    				return Command.SINGLE_SUCCESS;
                }))
            	))
			));
		event.getDispatcher().register(Commands.literal("beecrasy").then(inspect).then(calend).then(midi).then(editGenome).then(editProduct));
	}
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void tick(ServerTickEvent.Pre event) {
		long clock=event.getServer().overworld().getOverworldClockTime();
		event.getServer().getDataStorage().computeIfAbsent(WorldCalendar.TYPE).tick(clock);
	}
	@SubscribeEvent
	public static void checkTempAndHumid(BeeEnvironmentValidateEvent event) {
		Temperature temp=event.getAllele(Genes.TEMPERATURE);
		Humidity humid=event.getAllele(Genes.HUMIDITY);
		if(!temp.isValidFor(event.getParams(), humid))
			event.setCanceled(true);
	}
	@SubscribeEvent
	public static void playerLoggedIn(PlayerLoggedInEvent event) {
		if(event.getEntity() instanceof ServerPlayer serverPlayer){
			if(serverPlayer.isFakePlayer())
				return;
			PostalOffice po=PostalOffice.getPostalOffice(serverPlayer.level());
			po.updatePendingMails(serverPlayer);

			PlayerPostalOffice ppo=serverPlayer.getData(Attachments.MAIL);
			int count=ppo.getMailCount();
			if(count>0)
				serverPlayer.sendSystemMessage(Component.translatable("message.postal.mail_recived_count",count));
		}
	}
	
}
