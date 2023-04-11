package me.jingo.enchantment;

import java.util.Map;
import java.util.function.Function;

import org.bukkit.enchantments.Enchantment;

import me.jingo.ove.OverleveledEnchanter;

public class EnchantmentLevelCorrector {
	
	private final OverleveledEnchanter pluginInstance;
	
	private final Map.Entry<Enchantment, Integer> newEnch;
	
	private final int maxLevelBooster;
	
	private final int combLevelBooster;
	
	private final boolean isUncapped;
	
	private final int newLevel;
	
	public EnchantmentLevelCorrector(OverleveledEnchanter pluginInstance, Map.Entry<Enchantment, Integer> newEnchantment, int otherLevel, boolean isUncapped, int maxLevelBooster, int combLevelBooster) {
		
		this.pluginInstance = pluginInstance;
		
		this.newEnch = newEnchantment;
		
		this.maxLevelBooster = maxLevelBooster;
		
		this.combLevelBooster = combLevelBooster;
		
		this.isUncapped = isUncapped;
		
		this.newLevel = getNewLevel(otherLevel, maxLevelBooster, combLevelBooster);
	}
	
	public int getNewLevel() {return this.newLevel;}
	
	private int getNewLevel(int otherLevel, int maxLevelBooster, int combLevelBooster) {
		
		final Enchantment ench = this.newEnch.getKey();
		
		final int maxLevel = getCorrectMaxLevel(pluginInstance::getMaxLevel, OverleveledEnchanter.noMaxLevelFoundWarn, this.maxLevelBooster);
		
		final int combLevel = getCorrectMaxLevel(pluginInstance::getCombLevel, OverleveledEnchanter.noCombLevelFoundWarn, this.combLevelBooster);
		
		int newLevel = this.newEnch.getValue();
		
		if(otherLevel > newLevel) newLevel = otherLevel;
		
		//if the enchantments have the same level value and the level is lower than the maximum comb level (or if there is no comb level)
		else if (otherLevel == newLevel && (newLevel < combLevel || pluginInstance.hasNoMaxLevel(combLevel))) {
			//then if the level is lower than the enchantment's max level, or if the uncapped anvil is enabled and the player has the appropriate permission
			 if (newLevel <= ench.getMaxLevel()-1 || this.isUncapped) 
				 newLevel += 1;
		}
		
		if(isOverMaxLevel(newLevel, maxLevel)) newLevel = maxLevel;
		
		return newLevel;
	}
	
	private int getCorrectMaxLevel(Function<Enchantment, Integer> getLevel, String warning, int booster) {
		
		Enchantment ench = this.newEnch.getKey();
		
		int level = getLevel.apply(ench);
		
		if(pluginInstance.isLevelInvalid(level)) {
			//Normally enchantments are combined up until a level prior to the enchantment's max level.
			level = ench.getMaxLevel();
			this.pluginInstance.getLogger().severe(String.format(warning, ench.getKey().getKey(), ench.getMaxLevel()));
		}
		
		//If the enchantment has a max level, add the max level booster for that player
		else if(!pluginInstance.hasNoMaxLevel(level))
			level += booster;
		
		return level;
	
	}
	
	private boolean isOverMaxLevel(int level, int maxLevel) {
		
		return !pluginInstance.hasNoMaxLevel(maxLevel) && level > maxLevel;
	}
}
