package me.jingo.enchantment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.jingo.ove.OverleveledEnchanter;

public class EnchantmentCorrector {
	
	private final OverleveledEnchanter pluginInstance;
	
	private final Player thePlayer;
	
	private final Map<Enchantment, Integer> slot1Enchants;
	
	private final Map<Enchantment, Integer> slot2Enchants;
	
	private final ItemMeta newMeta;
	
	public EnchantmentCorrector(OverleveledEnchanter pluginInstance, PrepareAnvilEvent e, boolean isForge, ItemMeta newMeta) {
		
		this.pluginInstance = pluginInstance;
		
		this.thePlayer = (Player)e.getView().getPlayer();
		
		ItemStack[] contents = e.getInventory().getContents();
		
		this.slot1Enchants = getEnchants(contents[0]);
		
		if(contents[1] == null || !isForge)
			this.slot2Enchants = Collections.emptyMap();
		else
			this.slot2Enchants = getEnchants(contents[1]);
		
		this.newMeta = newMeta;
	}
	
	private Map<Enchantment, Integer> getEnchants(ItemStack theItem){
		
		if(theItem.getItemMeta() instanceof EnchantmentStorageMeta)
			return new HashMap<>(((EnchantmentStorageMeta)theItem.getItemMeta()).getStoredEnchants());
		else
			return new HashMap<>(theItem.getItemMeta().getEnchants());
	}
	
	public void updateEnchantments() {
		
		if(this.newMeta instanceof EnchantmentStorageMeta)
			setEnchants(((EnchantmentStorageMeta)this.newMeta).getStoredEnchants(), (e,l) -> ((EnchantmentStorageMeta)this.newMeta).addStoredEnchant(e, l, true));
		else
			setEnchants(this.newMeta.getEnchants(), (e,l) -> this.newMeta.addEnchant(e, l, true));
		
		}
	
	private void setEnchants(Map<Enchantment, Integer> resultEnchants, BiConsumer<Enchantment, Integer> addEnchant) {
		
		final int maxLevelBooster = pluginInstance.getMaxLevelBooster(thePlayer);
		
		final int combLevelBooster = pluginInstance.getCombLevelBooster(thePlayer);
		
		final boolean isUncapped = isPlayerUncapped();
		
		//We need a loop for both lists to fix all max levels. We remove any common enchantments from the iteration of the next list
		//To avoid rechecking already checked enchantments
		for(Enchantment ench : this.slot1Enchants.keySet()) {
			
			final int otherLevel;
			
			if(this.slot2Enchants.containsKey(ench)) {
				otherLevel = this.slot2Enchants.get(ench);
				//This is the removal from the other enchantment list
				this.slot2Enchants.remove(ench);
			}
			//means no other level, the enchantment solely exists on one enchantment list
			else otherLevel = -1;
			
			if(resultEnchants.keySet().contains(ench)) {
				addEnchant.accept(ench, new EnchantmentLevelCorrector(this.pluginInstance, Map.entry(ench, this.slot1Enchants.get(ench)), otherLevel, isUncapped, maxLevelBooster, combLevelBooster).getNewLevel());
			}
		}
		
		//Now we loop for all the remaining enchants (non common enchantments)
		for(Enchantment ench : this.slot2Enchants.keySet())
			if(resultEnchants.keySet().contains(ench))
				//This is the iteration for any enchantment that exists only in slot2Enchants, that is why the slot1Enchantment level is -1 (non existent)
				addEnchant.accept(ench, new EnchantmentLevelCorrector(this.pluginInstance, Map.entry(ench, this.slot2Enchants.get(ench)), -1, isUncapped, maxLevelBooster, combLevelBooster).getNewLevel());
	}
	
	private boolean isPlayerUncapped() {return this.pluginInstance.isUncapped() && this.thePlayer.hasPermission("overleveledenchanter.uncappedanvil");}
}
