package me.jingo.ove;

import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class EnchantingTable implements Listener{
	
	private final OverleveledEnchanter pluginInstance;
	
	public EnchantingTable(OverleveledEnchanter pluginInstance) {
		
		this.pluginInstance = pluginInstance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onEnchanting(EnchantItemEvent theTable){
		
		for(Map.Entry<Enchantment, Integer> ench : theTable.getEnchantsToAdd().entrySet()) {
			
			int maxLevel = pluginInstance.getMaxLevel(ench.getKey());
			
			if(pluginInstance.isLevelInvalid(maxLevel)) {
				maxLevel = ench.getKey().getMaxLevel();
				String.format(OverleveledEnchanter.noMaxLevelFoundWarn, ench.getKey().getKey().getKey(), ench.getKey().getMaxLevel());
			}
			
			if(ench.getValue() > maxLevel && !pluginInstance.hasNoMaxLevel(maxLevel))
				theTable.getEnchantsToAdd().replace(ench.getKey(), maxLevel);
				
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onEnchantingTable(PrepareItemEnchantEvent theTable) {
		
		for(EnchantmentOffer theOffer : theTable.getOffers()) {
			
			if(theOffer == null) continue;
			
				int maxLevel = pluginInstance.getMaxLevel(theOffer.getEnchantment());
				
				if(pluginInstance.isLevelInvalid(maxLevel)) {
					maxLevel = theOffer.getEnchantment().getMaxLevel();
					String.format(OverleveledEnchanter.noMaxLevelFoundWarn, theOffer.getEnchantment().getKey().getKey(), theOffer.getEnchantment().getMaxLevel());
				}
				
				if(theOffer.getEnchantmentLevel() > maxLevel && !pluginInstance.hasNoMaxLevel(maxLevel))
					theOffer.setEnchantmentLevel(maxLevel);
			
		}
		
	}
	
}
