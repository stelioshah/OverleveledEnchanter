package me.jingo.ove;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jingo.enchantment.EnchantmentCorrector;
import me.jingo.enchantment.LoreCorrector;

public class Anvil implements Listener{
	
	private final OverleveledEnchanter pluginInstance;
	
	protected final Map<UUID, StoredInventory> activeAnvilPlayers = new HashMap<>();
	
	public Anvil(OverleveledEnchanter pluginInstance) {
		
		this.pluginInstance = pluginInstance;
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onUsingAnvil(PrepareAnvilEvent e) {
		
		if(!(e.getView().getPlayer() instanceof Player)) return;
		
		Player thePlayer = (Player)e.getView().getPlayer();
		
		if(!thePlayer.hasPermission("overleveledenchanter.overleveledanvil")) return;
		
		//This needs to be right here, otherwise some times there will be no result
		e.getInventory().setMaximumRepairCost(Integer.MAX_VALUE);
		
		if(hasPlayerAnviled(thePlayer))
			if(!shouldResetInventory(thePlayer, e)) return;
		
		if(!hasItem(e.getInventory().getContents()[0])) return;
		
		if(!hasItem(e.getResult())) return;
		
		//The anvil cost needs to be updated regardless if it is a name change repair or item forge
		updateAnvilCost(e);
		
		if(isItemForge(e.getInventory().getContents()[0], e.getInventory().getContents()[1]))
			updateTheAnvil(e, true);
		
		//For name changes repairs etc, we need to notify that it is not an item forge
		else
			updateTheAnvil(e, false);
		
		
		saveTheInventory(thePlayer, e);
	}
	
	private boolean shouldResetInventory(Player thePlayer, PrepareAnvilEvent e) {
		
			StoredInventory theInv = this.activeAnvilPlayers.get(thePlayer.getUniqueId());
			
			//If the player has a saved inventory and the contents have changed, remove the old inventory and continue
			if(theInv.hasInventoryChanged(e)) {
				this.activeAnvilPlayers.remove(thePlayer.getUniqueId());
				return true;
			}
			//If the contents have not changed, update the current result
			else {
				theInv.updateInventory(e);
				return false;
		}
	}
	
	private boolean hasPlayerAnviled(Player thePlayer) {return this.activeAnvilPlayers.containsKey(thePlayer.getUniqueId());}
	
	private boolean isItemForge(ItemStack slot1, ItemStack slot2) {
		
		if(!hasItem(slot2)) return false;
		
		if(isRepair(slot1, slot2)) return false;
		
		return true;
	}
	
	private boolean isRepair(ItemStack slot1, ItemStack slot2) {
		
		if(Material.ENCHANTED_BOOK.equals(slot2.getType())) return false;
		
		if(slot1.getType().equals(slot2.getType()) && slot2.getItemMeta().hasEnchants()) return false;
		
		return true;
		
	}
	
	public static boolean hasItem(ItemStack theItem) {return theItem != null && !Material.AIR.equals(theItem.getType());}
	
	private void updateTheAnvil(PrepareAnvilEvent e, boolean isForge) {
		
		updateResultingItem(e, isForge);
		
		addExpLore(e.getResult(), e.getInventory().getRepairCost());
	}
	
	private void updateAnvilCost(PrepareAnvilEvent e) {
		
		int maxRepairCost = getMaxExpCost();
		
		if(e.getInventory().getRepairCost() > maxRepairCost || (pluginInstance.isAlwaysMaxExpCost() && e.getInventory().getRepairCost() >= 40))
			e.getInventory().setRepairCost(maxRepairCost);
		
	}
	
	private int getMaxExpCost() {
		
		if(this.pluginInstance.getConfig().contains("Max-Level-Cost"))
			return this.pluginInstance.getConfig().getInt("Max-Level-Cost");
		else {
			this.pluginInstance.getLogger().severe(String.format(OverleveledEnchanter.noMaxExpLevelFoundWarn));
			return 39;
		}
	}
	
	private void updateResultingItem(PrepareAnvilEvent e, boolean isForge) {
		
		e.setResult(getNewResult(e, isForge));
	}
	
	private ItemStack getNewResult(PrepareAnvilEvent e, boolean isForge) {
		
		final ItemStack newResult = e.getResult().clone();
		
		final ItemMeta newMeta = newResult.getItemMeta();
		
		new EnchantmentCorrector(this.pluginInstance, e, isForge, newMeta).updateEnchantments();
		
		newResult.setItemMeta(newMeta);
		
		return newResult;
	}
	
	private void addExpLore(ItemStack theResult, int expCost) {
		
		final ItemMeta newMeta = theResult.getItemMeta();
		
		LoreCorrector.addExpLore(newMeta, expCost, pluginInstance.expLoreKey);
		
		theResult.setItemMeta(newMeta);
	}
	
	private void saveTheInventory(Player thePlayer, PrepareAnvilEvent e) {
		
		ItemStack[] contents = e.getInventory().getContents();
		
		this.activeAnvilPlayers.put(thePlayer.getUniqueId(), new StoredInventory(contents[0], contents[1], e.getResult(), e.getInventory().getRepairCost()));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onClosingInventory(InventoryCloseEvent e) {
		
		if(!(e.getInventory() instanceof AnvilInventory)) return;
		
		if(this.activeAnvilPlayers.containsKey(e.getPlayer().getUniqueId())) this.activeAnvilPlayers.remove(e.getPlayer().getUniqueId());
	}
}
