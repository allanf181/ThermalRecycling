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

package org.blockartistry.mod.ThermalRecycling.support;

import java.util.ArrayList;
import java.util.List;

import org.blockartistry.mod.ThermalRecycling.BlockManager;
import org.blockartistry.mod.ThermalRecycling.ItemManager;
import org.blockartistry.mod.ThermalRecycling.ModLog;
import org.blockartistry.mod.ThermalRecycling.ModOptions;
import org.blockartistry.mod.ThermalRecycling.blocks.PileOfRubble;
import org.blockartistry.mod.ThermalRecycling.data.ExtractionData;
import org.blockartistry.mod.ThermalRecycling.data.ItemData;
import org.blockartistry.mod.ThermalRecycling.data.RecipeData;
import org.blockartistry.mod.ThermalRecycling.data.ScrapHandler;
import org.blockartistry.mod.ThermalRecycling.data.ScrapValue;
import org.blockartistry.mod.ThermalRecycling.data.ScrappingTables;
import org.blockartistry.mod.ThermalRecycling.items.Material;
import org.blockartistry.mod.ThermalRecycling.items.RecyclingScrap;
import org.blockartistry.mod.ThermalRecycling.support.handlers.ThermalRecyclingScrapHandler;
import org.blockartistry.mod.ThermalRecycling.support.recipe.RecipeDecomposition;
import org.blockartistry.mod.ThermalRecycling.util.ItemStackHelper;
import org.blockartistry.mod.ThermalRecycling.util.ItemStackWeightTable.ItemStackItem;

import cpw.mods.fml.common.registry.GameRegistry;

import org.blockartistry.mod.ThermalRecycling.util.OreDictionaryHelper;
import org.blockartistry.mod.ThermalRecycling.util.PreferredItemStacks;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class ModThermalRecycling extends ModPlugin {

	private static class EnergeticRedstoneRecipes {

		protected final String ore;
		protected final int input;
		protected final int output;

		public EnergeticRedstoneRecipes(final String ore, final int input, final int output) {
			this.ore = ore;
			this.input = input;
			this.output = output;
		}

		public boolean areOresAvailable() {
			return !OreDictionary.getOres(this.ore).isEmpty();
		}

		public void register() {
			if (!areOresAvailable())
				return;

			final List<Object> ingredients = new ArrayList<Object>();
			ingredients.add(this.ore);
			for (int i = 0; i < input; i++)
				ingredients.add(Items.redstone);
			final ShapelessOreRecipe recipe = new ShapelessOreRecipe(
					new ItemStack(ItemManager.energeticRedstoneDust, output), ingredients.toArray());
			GameRegistry.addRecipe(recipe);
		}
	}

	private static final EnergeticRedstoneRecipes[] energeticUraniumRecipes = new EnergeticRedstoneRecipes[] {
			new EnergeticRedstoneRecipes("dustUranium", 2, 3), new EnergeticRedstoneRecipes("crushedUranium", 2, 3),
			new EnergeticRedstoneRecipes("crushedPurifiedUranium", 4, 6) };

	public ModThermalRecycling() {
		super(SupportedMod.THERMAL_RECYCLING);
	}

	@Override
	public boolean initialize() {

		// Register special scrap handlers
		final ThermalRecyclingScrapHandler handler = new ThermalRecyclingScrapHandler();
		// Need to be able to see any special frames in real time.
		ScrapHandler.registerHandler(new ItemStack(ItemManager.processingCore, 1, OreDictionaryHelper.WILDCARD_VALUE),
				handler);

		// RTG Support - scrub fuel cells from output
		ItemData.setScrubbedFromOutput(new ItemStack(ItemManager.material, 1, Material.FUEL_CELL), true);

		// RTG Energy Cells have superior value - even if depleted
		ItemData.setValue(new ItemStack(ItemManager.energyCell, 1, OreDictionaryHelper.WILDCARD_VALUE),
				ScrapValue.SUPERIOR);
		ItemData.setValue(new ItemStack(ItemManager.material, 1, Material.RTG_DEPLETED), ScrapValue.SUPERIOR);

		ItemData.setRecipeIgnored(ItemManager.recyclingScrapBox, true);
		ItemData.setRecipeIgnored(ItemManager.debris, true);
		ItemData.setRecipeIgnored(BlockManager.scrapBlock, true);
		ItemData.setRecipeIgnored(ItemManager.material, true);
		ItemData.setRecipeIgnored(new ItemStack(ItemManager.material, 1, Material.RTG_HOUSING), false);
		ItemData.setRecipeIgnored(ItemManager.paperLogMaker, true);
		ItemData.setRecipeIgnored(ItemManager.energeticRedstoneDust, true);

		ItemData.setValue(new ItemStack(ItemManager.debris), ScrapValue.NONE);
		ItemData.setValue(new ItemStack(BlockManager.scrapBlock), ScrapValue.NONE);
		ItemData.setValue(new ItemStack(ItemManager.paperLogMaker), ScrapValue.NONE);
		ItemData.setValue(new ItemStack(ItemManager.material, 1, Material.LITTER_BAG), ScrapValue.NONE);

		ItemData.setValue(new ItemStack(ItemManager.material, 1, Material.PAPER_LOG), ScrapValue.POOR);

		ItemData.setValue(new ItemStack(ItemManager.recyclingScrap, 1, RecyclingScrap.POOR), ScrapValue.POOR);
		ItemData.setValue(new ItemStack(ItemManager.recyclingScrap, 1, RecyclingScrap.STANDARD), ScrapValue.STANDARD);
		ItemData.setValue(new ItemStack(ItemManager.recyclingScrap, 1, RecyclingScrap.SUPERIOR), ScrapValue.SUPERIOR);
		ItemData.setValue(new ItemStack(ItemManager.recyclingScrapBox, 1, RecyclingScrap.POOR), ScrapValue.POOR);
		ItemData.setValue(new ItemStack(ItemManager.recyclingScrapBox, 1, RecyclingScrap.STANDARD),
				ScrapValue.STANDARD);
		ItemData.setValue(new ItemStack(ItemManager.recyclingScrapBox, 1, RecyclingScrap.SUPERIOR),
				ScrapValue.SUPERIOR);

		if (ModOptions.getEnableForgeOreDictionaryScan()) {
			// Use the Forge dictionary to find equivalent ore to set the
			// appropriate scrap value.
			registerScrapValuesForge(ScrapValue.STANDARD, "ingotIron", "ingotGold", "ingotCopper", "ingotTin",
					"ingotSilver", "ingotLead", "ingotNickle", "ingotPlatinum", "ingotManaInfused", "ingotElectrum",
					"ingotInvar", "ingotBronze", "ingotSignalum", "ingotEnderium");

			registerScrapValuesForge(ScrapValue.STANDARD, "dustIron", "dustGold", "dustCopper", "dustTin", "dustSilver",
					"dustLead", "dustNickle", "dustPlatinum", "dustManaInfused", "dustElectrum", "dustInvar",
					"dustBronze", "dustSignalum", "dustEnderium");

			registerScrapValuesForge(ScrapValue.STANDARD, "blockIron", "blockGold", "blockCopper", "blockTin",
					"blockSilver", "blockLead", "blockNickle", "blockPlatinum", "blockManaInfused", "blockElectrum",
					"blockInvar", "blockBronze", "blockSignalum", "blockEnderium");

			registerScrapValuesForge(ScrapValue.STANDARD, "oreIron", "oreGold", "oreCopper", "oreTin", "oreSilver",
					"oreLead", "oreNickle", "orePlatinum", "oreManaInfused", "oreElectrum", "oreInvar", "oreBronze",
					"oreSignalum", "oreEnderium");

			registerScrapValuesForge(ScrapValue.POOR, "nuggetIron", "nuggetGold", "nuggetCopper", "nuggetTin",
					"nuggetSilver", "nuggetLead", "nuggetNickle", "nuggetPlatinum", "nuggetManaInfused",
					"nuggetElectrum", "nuggetInvar", "nuggetBronze", "nuggetSignalum", "nuggetEnderium");

			registerScrapValuesForge(ScrapValue.SUPERIOR, "gemDiamond", "gemEmerald", "oreDiamond", "oreEmerald",
					"blockDiamond", "blockEmerald");
			registerScrapValuesForge(ScrapValue.STANDARD, "nuggetDiamond", "nuggetEmerald");
		}

		registerRecycleToWoodDustForge(1, "logWood");
		registerRecycleToWoodDustForge(2, "plankWood");
		registerRecycleToWoodDustForge(8, "treeSapling");

		registerRecipesToIgnoreForge("logWood", "plankWood", "treeSapling");

		// Configure extraction recipes
		registerExtractionRecipe(ScrappingTables.poorScrap, new ItemStackItem(null, 120),
				new ItemStackItem(ScrappingTables.standardScrap, 60),
				new ItemStackItem(ItemStackHelper.getItemStack("minecraft:dye:15").get(), 10),
				new ItemStackItem(PreferredItemStacks.instance.dustCoal, 10),
				new ItemStackItem(PreferredItemStacks.instance.dustCharcoal, 10),
				new ItemStackItem(PreferredItemStacks.instance.sulfer, 10),
				new ItemStackItem(PreferredItemStacks.instance.dustIron, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustTin, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustCopper, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustNickel, 20));

		registerExtractionRecipe(ScrappingTables.standardScrap, new ItemStackItem(null, 78),
				new ItemStackItem(ScrappingTables.superiorScrap, 52),
				new ItemStackItem(PreferredItemStacks.instance.dustCoal, 10),
				new ItemStackItem(ItemStackHelper.getItemStack("ThermalFoundation:material:17").get(), 10),
				new ItemStackItem(PreferredItemStacks.instance.dustIron, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustTin, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustCopper, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustSilver, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustLead, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustGold, 10));

		registerExtractionRecipe(ScrappingTables.superiorScrap,
				new ItemStackItem(PreferredItemStacks.instance.dustGold, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustPlatinum, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustElectrum, 20),
				new ItemStackItem(PreferredItemStacks.instance.dustSignalum, 10),
				new ItemStackItem(PreferredItemStacks.instance.dustLumium, 10),
				new ItemStackItem(PreferredItemStacks.instance.dustEnderium, 10));

		registerExtractionRecipe(new ItemStack(ItemManager.recyclingScrapBox, 1, OreDictionaryHelper.WILDCARD_VALUE),
				new ItemStackItem(null, 1));

		// Soylent Red and Yellow
		registerExtractionRecipe(new ItemStack(Blocks.pumpkin, 6),
				new ItemStackItem(new ItemStack(ItemManager.soylentYellow), 1));
		registerExtractionRecipe(new ItemStack(Items.carrot, 12),
				new ItemStackItem(new ItemStack(ItemManager.soylentYellow), 1));
		registerExtractionRecipe(new ItemStack(Items.potato, 16),
				new ItemStackItem(new ItemStack(ItemManager.soylentYellow), 1));
		registerExtractionRecipe(new ItemStack(Items.apple, 12),
				new ItemStackItem(new ItemStack(ItemManager.soylentYellow), 1));

		registerExtractionRecipe(new ItemStack(Items.beef, 6),
				new ItemStackItem(new ItemStack(ItemManager.soylentRed), 1));
		registerExtractionRecipe(new ItemStack(Items.porkchop, 8),
				new ItemStackItem(new ItemStack(ItemManager.soylentRed), 1));
		registerExtractionRecipe(new ItemStack(Items.fish, 12),
				new ItemStackItem(new ItemStack(ItemManager.soylentRed), 1));
		registerExtractionRecipe(new ItemStack(Items.chicken, 8),
				new ItemStackItem(new ItemStack(ItemManager.soylentRed), 1));

		registerExtractionRecipe(new ItemStack(Items.rotten_flesh, 16),
				new ItemStackItem(new ItemStack(ItemManager.soylentGreen), 1));

		ItemData.setBlockedFromExtraction(ScrappingTables.poorScrapBox, false);
		ItemData.setBlockedFromExtraction(ScrappingTables.standardScrapBox, false);
		ItemData.setBlockedFromExtraction(ScrappingTables.superiorScrapBox, false);

		// RTG - Extract an RTG Energy Cell to a Housing - loses anything
		// energy, etc.
		registerExtractionRecipe(new ItemStack(ItemManager.energyCell, 1, OreDictionaryHelper.WILDCARD_VALUE),
				new ItemStackItem(new ItemStack(ItemManager.material, 1, Material.RTG_HOUSING), 1));

		// RTG - Extract a Depleted RTG Energy Cell to a Housing
		registerExtractionRecipe(new ItemStack(ItemManager.material, 1, Material.RTG_DEPLETED),
				new ItemStackItem(new ItemStack(ItemManager.material, 1, Material.RTG_HOUSING), 1));

		// ////////////////////
		//
		// Add recipe blacklist items first
		// before processing!
		//
		// ////////////////////

		// Apply the blacklist from the configuration. We need to fix up
		// each entry with a ^ so the underlying routine just does what it
		// needs to do.
		for (final String s : ModOptions.getRecyclerBlacklist()) {
			registerItemBlockedFromScrapping(true, "^" + s);
		}

		// Register scrap items for Pile of Rubble
		PileOfRubble.addRubbleDrop(ScrappingTables.poorScrap, 1, 2, 5);
		PileOfRubble.addRubbleDrop(ScrappingTables.poorScrapBox, 1, 1, 2);
		PileOfRubble.addRubbleDrop(ScrappingTables.standardScrap, 1, 2, 4);
		PileOfRubble.addRubbleDrop(ScrappingTables.standardScrapBox, 1, 1, 1);

		PileOfRubble.addRubbleDrop(new ItemStack(ItemManager.material, 1, Material.LITTER_BAG), 1, 2, 4);

		PileOfRubble.addRubbleDrop(new ItemStack(ItemManager.soylentGreen), 1, 1, 1);
		PileOfRubble.addRubbleDrop(new ItemStack(ItemManager.soylentYellow), 1, 1, 2);
		PileOfRubble.addRubbleDrop(new ItemStack(ItemManager.soylentRed), 1, 1, 2);

		// If there is uranium dust in the ore dictionary create a crafting
		// recipe for Energetic Redstone Dust.
		if (ModOptions.getEnergeticRedstoneUraniumCrafting()) {
			for (final EnergeticRedstoneRecipes r : energeticUraniumRecipes)
				r.register();
		}

		return true;
	}

	private void processRecipeList(final List<Object> recipes, final boolean vanillaOnly) {

		// Process all registered recipes
		for (final Object o : recipes) {

			final IRecipe recipe = (IRecipe) o;
			final ItemStack stack = recipe.getRecipeOutput();

			try {

				// Check to see if this item should have a recipe in
				// the list. This does not mean that something later
				// on can't add one - just means by default it will
				// not be included.
				if (stack != null && (!vanillaOnly || ItemStackHelper.isVanilla(stack))) {
					if (!ItemData.isRecipeIgnored(stack)) {

						// If the name is prefixed with any of the mods
						// we know about then we can create the recipe.
						final String name = Item.itemRegistry.getNameForObject(stack.getItem());

						if (SupportedMod.isModWhitelisted(name)) {
							final List<ItemStack> output = RecipeDecomposition.decompose(recipe);
							if (output != null && !output.isEmpty()) {
								if (vanillaOnly && !ItemStackHelper.isVanilla(output))
									continue;
								recycler.useRecipe(recipe).save();
							}
						}
					}
				}
			} catch (Throwable t) {
				ModLog.warn("processRecipeList: Unable to register recipe for [%s]",
						ItemStackHelper.resolveName(stack));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean postInit() {

		// ////////////////////
		//
		// Process the recipes
		//
		// ////////////////////
		final List<Object> recipes = CraftingManager.getInstance().getRecipeList();
		processRecipeList(recipes, true);
		processRecipeList(recipes, false);

		// Lock our tables
		ItemData.freeze();
		RecipeData.freeze();
		ScrapHandler.freeze();
		ExtractionData.freeze();

		return true;
	}
}
