package com.khjxiaogu.beecrasy.menu;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ApistleMenu extends BeecrasyContainerMenu {
	String modid;
	ItemAccess item;
	public final static Identifier MODEL = Beecrasy.rl("apistle");
	public final static Identifier MODEL_ACTIVE =Beecrasy.rl("apistle_active");
	public ApistleMenu( int containerId, Inventory inventory,ItemAccess item) {
		super(Menus.APISTLE.get(), containerId, inventory, 0);
		this.item=item;
	}
	public ApistleMenu( int containerId, Inventory inventory,RegistryFriendlyByteBuf data) {
		super(Menus.APISTLE.get(), containerId, inventory, 0);
		this.modid=data.readUtf();
	}

	@Override
	public void receiveOperation(short opCode, int opData) {
	}

	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return false;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return item==null||item.getAmount()==1;
	}

	public String getModid() {
		return modid;
	}
	public static void openMenu(ServerPlayer player,String modid,ItemAccess access) {

		try(Transaction trans=Transaction.openRoot()){
			if(access.exchange(access.getResource().with(DataComponents.ITEM_MODEL, MODEL_ACTIVE), 1, trans)==1) {
				trans.commit();
			}
		}
		player.openMenu(new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
				return new ApistleMenu(containerId,inventory,access);
			}
			@Override
			public Component getDisplayName() {
				return access.getResource().getHoverName();
			}
			
		}, t->{
			t.writeUtf(modid);
			
		});
		
	}
	@Override
	public void removed(Player player) {
		if(player instanceof ServerPlayer)
			try(Transaction trans=Transaction.openRoot()){
				if(item.exchange(item.getResource().with(DataComponents.ITEM_MODEL, MODEL), 1, trans)==1)
					trans.commit();
			}
		super.removed(player);
	}

}
