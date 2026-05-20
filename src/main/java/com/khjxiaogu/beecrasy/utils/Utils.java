package com.khjxiaogu.beecrasy.utils;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Attachments;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public final class Utils {
	public static ItemStackTemplate getRecipeOutput(List<ItemStack> stacks,CraftingRecipe recipe) {
		return switch(recipe) {
		case ShapedRecipe sr->sr.result;
		case ShapelessRecipe sr->sr.result;
		default->{
			CraftingInput ipt=CraftingInput.of(3, 3, stacks);
			ItemStack is=recipe.assemble(ipt);
			if(is!=null&&!is.isEmpty()) {
				yield new ItemStackTemplate(is.getItem(),is.getCount(),is.getComponentsPatch());
			}
			yield null;
		}
		};
	}
	public static RandomSource getSyncedRandom(Player p) {
		@Nullable Long comp=p.getData(Attachments.RANDOM_SEED);
		RandomSource rnd=RandomSource.create(comp);
		p.setData(Attachments.RANDOM_SEED.get(), rnd.nextLong());
		return rnd;
	}
	/**
	 * 将任务列表均匀拆分为若干个子集合。
	 * <p>
	 * 拆分规则：
	 * <ul>
	 *     <li>每个子集合的元素个数应尽可能接近 {@code effort}（目标大小），且不小于 {@code effort}（若总任务数不足 {@code effort}，
	 *     则返回包含整个列表的单个子集合）。</li>
	 *     <li>子集合之间元素数量尽可能相等（任意两个子集合大小之差不超过1）。</li>
	 *     <li>拆分的子集合数量不会超过系统可用的处理器核心数（{@link Runtime#availableProcessors()}），
	 *     即 {@code k = min(round(n / effort), 可用处理器数)}。</li>
	 * </ul>
	 *
	 * @param tasks  原始任务列表（不能为 null）
	 * @param effort 期望的每个子集合的目标大小（正数，例如 400.0f）。算法会尽量使每个子集合大小接近该值，
	 *               但实际大小可能略高或略低（受总数和处理器数限制）。
	 * @param <T>    任务类型
	 * @return 拆分后的子集合列表。若原始列表为 null 或空，返回空列表；
	 *         若列表长度小于 {@code effort}，返回包含整个列表的单个子集合。
	 */
    public static <T> List<List<T>> splitTasks(List<T> tasks,float effort) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        int n = tasks.size();
        if (n < effort) {
            List<List<T>> result = new ArrayList<>();
            result.add(new ArrayList<>(tasks));
            return result;
        }
        int k = Math.round(n / effort);
        k=Math.min(k, Runtime.getRuntime().availableProcessors());
        int base = n / k;
        int remainder = n % k; 

        List<List<T>> result = new ArrayList<>(k);
        int start = 0;
        for (int i = 0; i < k; i++) {
            int size = base + (i < remainder ? 1 : 0);
            int end = start + size;
            List<T> subList = new ArrayList<>(tasks.subList(start, end));
            result.add(subList);
            start = end;
        }
        return result;
    }
	public static MutableComponent translate(String format, Object... objects) {
		return translateWithFallback(format, null, objects);
	}

	public static MutableComponent translate(String format) {
		return translate(format, new Object[0]);
	}

	public static MutableComponent translateWithFallback(String format, String fallback, Object... objects) {
		return MutableComponent.create(new TranslatableContents(format, fallback, objects));
	}

	public static MutableComponent translateWithFallback(String format, String fallback) {
		return translate(format, fallback, new Object[0]);
	}

	public static MutableComponent string(String content) {
		return MutableComponent.create(PlainTextContents.create(content));
	}

}
