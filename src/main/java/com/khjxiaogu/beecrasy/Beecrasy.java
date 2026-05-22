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
package com.khjxiaogu.beecrasy;

import org.slf4j.Logger;

import com.khjxiaogu.beecrasy.network.PacketHandler;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Beecrasy.MODID)
public class Beecrasy {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "beecrasy";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();



    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Beecrasy(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
    	LOGGER.info("Say hello to Beecracy(Not a typo)!");
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(PacketHandler::registerPackets);
        BeecrasyRegistries.register(modEventBus);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        BeecrasyConfig.register();
        
    }
    private void commonSetup(FMLCommonSetupEvent event) {
    }

    public static Identifier rl(String name) {
    	return Identifier.fromNamespaceAndPath(MODID, name);
    }
}
