/*
 * This file is part of ThermalRecycling, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.ThermalRecycling.data;

import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.blockartistry.mod.ThermalRecycling.util.ItemStackHelper;
import org.blockartistry.mod.ThermalRecycling.util.MyComparators;
import org.blockartistry.mod.ThermalRecycling.util.MyUtils;

import com.google.common.base.Preconditions;

import cofh.lib.util.helpers.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * An instance of this class is used to track recipes for the Thermal Recycler.
 *
 */
public final class RecipeData {

	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;
	public static final int DUPLICATE = 2;

	static final Map<ItemStack, RecipeData> recipes = new TreeMap<ItemStack, RecipeData>(
			MyComparators.itemStackAscending);

	final ItemStack inputStack;
	final List<ItemStack> outputStacks;

	public RecipeData(final ItemStack input, final List<ItemStack> output) {
		Preconditions.checkNotNull(input);
		Preconditions.checkNotNull(output);

		this.inputStack = input;
		this.outputStacks = output;
	}

	public ItemStack getInput() {
		return this.inputStack;
	}

	public int getMinimumInputStacksRequired() {
		return this.inputStack.stackSize;
	}

	public List<ItemStack> getOutput() {
		return Collections.unmodifiableList(this.outputStacks);
	}

	public static RecipeData get(final ItemStack input) {

		RecipeData match = recipes.get(input);

		if (match == null
				&& input.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			final ItemStack t = input.copy();
			t.setItemDamage(OreDictionary.WILDCARD_VALUE);
			match = recipes.get(t);
		}

		return match;
	}

	public static List<ItemStack> getRecipe(final ItemStack input) {
		List<ItemStack> result = null;
		final RecipeData data = get(input);
		if (data != null) {
			result = data.getOutput();
			if (result != null)
				result = ItemStackHelper.clone(result);
		}
		return result;
	}

	/**
	 * Adds the given recipe to the tracking tables.  It assumes control
	 * over output.
	 */
	public static int put(final ItemStack input, List<ItemStack> output) {
		Preconditions.checkNotNull(input);
		Preconditions.checkNotNull(output);

		int retCode = DUPLICATE;

		// See if we have an existing mapping
		final RecipeData result = get(input);

		// If we don't, or the mapping that exists is a wild card and the
		// incoming
		// recipe is specific, we want to add to the dictionary. The dictionary
		// will prefer specific recipes over wild cards if possible.
		if (result == null
				|| result.getInput().getItemDamage() == OreDictionary.WILDCARD_VALUE && input
						.getItemDamage() != OreDictionary.WILDCARD_VALUE) {

			final ItemStack stack = input.copy();

			// Traverse the list replacing WILDCARD stacks with concrete
			// ones.  The logic prefers Thermal Foundation equivalents
			// if found.
			for (int i = 0; i < output.size(); i++) {

				ItemStack working = output.get(i);

				if (working.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
					final String oreName = ItemHelper.getOreName(working);

					if (oreName != null) {
						working = ItemStackHelper.getItemStack(oreName,
								working.stackSize);
						if (working != null)
							output.set(i, working);
					}
				}
			}

			output = MyUtils.compress(ItemStackHelper.coelece(output));

			recipes.put(stack, new RecipeData(stack, output));

			retCode = SUCCESS;
		}

		return retCode;
	}

	public static int getMinimumQuantityToRecycle(final ItemStack item) {

		final RecipeData recipe = get(item);
		return recipe == null ? 1 : recipe.getMinimumInputStacksRequired();
	}

	@Override
	public String toString() {

		final StringBuilder builder = new StringBuilder();
		builder.append(String.format("[%dx %s] => [", inputStack.stackSize,
				ItemStackHelper.resolveName(inputStack)));

		if (outputStacks == null || outputStacks.isEmpty()) {
			builder.append("none");
		} else {
			boolean sawOne = false;

			for (final ItemStack stack : outputStacks) {
				if (sawOne)
					builder.append(", ");
				else
					sawOne = true;
				builder.append(String.format("%dx %s", stack.stackSize,
						ItemStackHelper.resolveName(stack)));
			}
		}
		builder.append("]");

		return builder.toString();
	}

	public static void writeDiagnostic(final Writer writer) throws Exception {

		writer.write("\nKnown Thermal Recycler Recipes:\n");
		writer.write("=================================================================\n");
		for (final RecipeData d : recipes.values())
			writer.write(String.format("%s\n", d.toString()));
		writer.write("=================================================================\n");
	}

}
