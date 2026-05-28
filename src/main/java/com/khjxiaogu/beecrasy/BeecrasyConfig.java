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

package com.khjxiaogu.beecrasy;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class BeecrasyConfig {

	public static void register() {
		//ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
		//ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
		ModLoadingContext.get().getActiveContainer().registerConfig(Type.SERVER, SERVER_CONFIG);
	}

	public static class Client {

		Client(ModConfigSpec.Builder builder) {
		}
	}

	public static class Common {

		Common(ModConfigSpec.Builder builder) {

		}
	}

	public static class Server {
		public final IntValue LIFESPAN;
		public final IntValue INTERVAL;
		public final IntValue RADIUS;
		Server(ModConfigSpec.Builder builder) {
			builder.push("beehive");
			LIFESPAN=builder.defineInRange("averageLifespan", 12000, 20, Integer.MAX_VALUE);
			INTERVAL=builder.defineInRange("hiveInterval", 100, 20, Integer.MAX_VALUE);
			RADIUS=builder.defineInRange("hiveWorkingRadius", 3, 1, Integer.MAX_VALUE);
			builder.pop();
		}
	}

	public static final ModConfigSpec CLIENT_CONFIG;
	public static final ModConfigSpec COMMON_CONFIG;
	public static final ModConfigSpec SERVER_CONFIG;
	
	public static final Client CLIENT;
	public static final Common COMMON;
	public static final Server SERVER;

	static {
		// ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		// CLIENT = new Client(CLIENT_BUILDER);
		// CLIENT_CONFIG = CLIENT_BUILDER.build();
		ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
		COMMON = new Common(COMMON_BUILDER);
		COMMON_CONFIG = COMMON_BUILDER.build();
		ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
		SERVER = new Server(SERVER_BUILDER);
		SERVER_CONFIG = SERVER_BUILDER.build();
		ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
		CLIENT = new Client(CLIENT_BUILDER);
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
}
