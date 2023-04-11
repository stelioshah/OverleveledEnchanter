package me.jingo.enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.jingo.ove.Anvil;
import me.jingo.ove.OverleveledEnchanter;

public class LoreCorrector implements Listener {
	
	private final OverleveledEnchanter pluginInstance;
	
	public LoreCorrector(OverleveledEnchanter pluginInstance) {
		
		this.pluginInstance = pluginInstance;
	}
	
	private static final String expCostString = ChatColor.AQUA + "This will cost:"+ ChatColor.GREEN + " %d " + ChatColor.AQUA + "levels";
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onTakingResult(InventoryClickEvent e) {
		
		if(!InventoryType.ANVIL.equals(e.getInventory().getType())) return;
		
		if(!isPlayerClickCorrect(e)) return;
		
		removeExpLore(e.getCurrentItem());
	}
	
	private boolean isPlayerClickCorrect(InventoryClickEvent e) {
		
		if(!(e.getWhoClicked() instanceof Player)) return false;
		
		Player thePlayer = (Player)e.getWhoClicked();
		
		//Resulting slot
		if(e.getRawSlot() != 2) return false;	
		
		if(!Anvil.hasItem(e.getCurrentItem())) return false;
		
		if(!isItemLoreModified(e.getCurrentItem())) return false;
		
		if(GameMode.CREATIVE.equals(thePlayer.getGameMode())) return true;
		
		if(ClickType.MIDDLE.equals(e.getClick())) return false;
		
		if(((AnvilInventory)e.getInventory()).getRepairCost() > thePlayer.getLevel()) return false;
		
		return true;
	}
	
	private boolean isItemLoreModified(ItemStack theItem) {
		
		if(!theItem.getItemMeta().getPersistentDataContainer().has(pluginInstance.expLoreKey.getKey(), pluginInstance.expLoreKey.getValue())) return false;
		
		if(!theItem.getItemMeta().hasLore()) return false;
		
		return true;
	}
	
	private void removeExpLore(ItemStack currentItem) {
		
		ItemMeta theMeta = currentItem.getItemMeta();
		
		int loreLine = currentItem.getItemMeta().getPersistentDataContainer().get(this.pluginInstance.expLoreKey.getKey(), this.pluginInstance.expLoreKey.getValue());
	
		List<String> theLore = new ArrayList<>();
		
		if(theMeta.hasLore())
			theLore.addAll(theMeta.getLore());
		
		theLore.remove(loreLine);
		
		theMeta.setLore(theLore);
		
		theMeta.getPersistentDataContainer().remove(this.pluginInstance.expLoreKey.getKey());
		
		currentItem.setItemMeta(theMeta);
	}
	
	public static void addExpLore(ItemMeta newMeta, int expCost, Map.Entry<NamespacedKey, PersistentDataType<Integer,Integer>> loreKey) {
		
		if(expCost < 40) return;
		
		List<String> theLore =  new ArrayList<>();
		
		if(newMeta.hasLore())
			theLore.addAll(newMeta.getLore());
		
		newMeta.getPersistentDataContainer().set(loreKey.getKey(), loreKey.getValue(), theLore.size());
		
		theLore.add(String.format(expCostString, expCost));
		
		newMeta.setLore(theLore);
		
	}
}
