/* This file is part of ThermalRecycling, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.ThermalRecycling.tweaker;

import minetweaker.MineTweakerAPI;

import org.blockartistry.mod.ThermalRecycling.ModLog;
import cpw.mods.fml.common.Loader;

public final class MineTweakerSupport {

	private static final String MINETWEAKER = "MineTweaker3";

	private MineTweakerSupport() {

	}

	public static void initialize() {
		if (Loader.isModLoaded(MINETWEAKER)) {
			ModLog.info("Registering hooks with MineTweaker");

			MineTweakerAPI.registerClass(ItemDataRegistry.class);
			MineTweakerAPI.registerClass(RecipeDataRegistry.class);
			MineTweakerAPI.registerClass(ExtractionDataRegistry.class);
			MineTweakerAPI.registerClass(VendingProfessionRegistry.class);
			MineTweakerAPI.registerClass(RubblePileDataRegistry.class);
			MineTweakerAPI.registerClass(BreedingItemRegistry.class);

			// Register the constants
			Constants.register();
		}
	}
}
