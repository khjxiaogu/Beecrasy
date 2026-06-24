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

package com.khjxiaogu.beecrasy.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public abstract class FileBinaryBakery<T> implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final PackOutput.PathProvider pathProvider;
    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final String modid;
    protected final String directory;
    protected final StreamCodec<? super ByteBuf,T> codec;
    private final List<TaskResult<T>> results=new ArrayList<>();

    public FileBinaryBakery(PackOutput output, PackOutput.Target target, String directory, StreamCodec<? super ByteBuf,T> codec, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        // Track generated data so other dataproviders can validate if needed.
        this.pathProvider = output.createPathProvider(target, directory);
        this.modid = modId;
        this.directory = directory;
        this.codec = codec;
        this.lookupProvider = lookupProvider;
    }


    protected OutputStream wrapStream(OutputStream in) {
    	return in;
    }
    protected String getExtension() {
    	return "bin";
    }
    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

        gather();

        return lookupProvider.thenCompose(_ -> {
           
            this.results.forEach((task) -> {
                final Path path = this.pathProvider.file(task.name(),getExtension());
                
                futuresBuilder.add(CompletableFuture.supplyAsync(() -> {
                	ByteBuf rfbb=Unpooled.buffer(4096);
                	this.codec.encode(rfbb, task.file());
                    return rfbb;
                }).thenComposeAsync(encoded ->
                	CompletableFuture.runAsync(() -> {
	                	try {
		                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		                    try(@SuppressWarnings("deprecation")
							HashingOutputStream hashedBytes = new HashingOutputStream(Hashing.sha1(), bytes);
		                    	
		                    	){
		                    	OutputStream os=wrapStream(hashedBytes);
		                    	os.write(ByteBufUtil.getBytes(encoded));
		                    	os.flush();
		                    	if(os!=hashedBytes)
			                    	os.close();
			                    cache.writeIfNeeded(path, bytes.toByteArray(), hashedBytes.hash());
			                    
		                    }
		                } catch (IOException var10) {
		                    LOGGER.error("Failed to save file to {}", path, var10);
		                }
                	},Util.backgroundExecutor().forName("saveStable"))
                ));
                
            });

            return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
        });
    }

    protected abstract void gather();


    protected void addTask(Identifier name, T file) {
    	results.add(new TaskResult<>(name,file));
    }
    private record TaskResult<T>(Identifier name, T file) {
    }
}
