package me.jingo.ove;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StoredInventory {
	
	@Nonnull 
	private final ItemStack slot1;
	
	private final ItemStack slot2;
	
	@Nonnull
	private final ItemStack result;
	
	@Nonnull
	private final int expCost;
	
	public StoredInventory(ItemStack slot1, ItemStack slot2, ItemStack result, int expCost) {
		
		this.slot1 = slot1;
		
		this.slot2 = slot2;
		
		this.result = result;
	
		this.expCost = expCost;
	}
	
	public boolean hasInventoryChanged(PrepareAnvilEvent e) {
		
		final ItemStack[] contents = e.getInventory().getContents();
		
		final ItemStack slot1 = contents[0];
		
		final ItemStack slot2 = contents[1];
		
		if(!this.slot1.isSimilar(slot1)) return true;
		
		//The PrepareAnvilEvent fires due to clicks, this sometimes result to slot2 showing as null, even if it isn't.
		if(this.slot2 == null) {
			if(slot2 != null) return true;
		}
		
		else if(!this.slot2.isSimilar(slot2)) return true;
		
		return false;
	}
	
	public ItemStack getResult() {return this.result;}
	
	public void updateInventory(PrepareAnvilEvent e) {
		
		//the result is the first slot seen as "null", when removing (clicking) something from the inventory. the clicked slot goes next.
		if(e.getResult() == null || Material.AIR.equals(e.getResult().getType())) return;
		
		if(hasChangedName(e.getResult()))
			updateName(e.getResult());
			
		//The result needs to always be updated
		e.setResult(this.result);
		
		//The cost also needs to always be updated
		e.getInventory().setRepairCost(expCost);
		
		if(e.getView().getPlayer() instanceof Player)
			((Player)e.getView().getPlayer()).updateInventory();
	}
	
	private boolean hasChangedName(ItemStack theResult) {return !this.result.getItemMeta().getDisplayName().equals(theResult.getItemMeta().getDisplayName());}
	
	private void updateName(ItemStack theResult) {
		
		final String newName = theResult.getItemMeta().getDisplayName();
		
		final ItemMeta savedMeta = this.result.getItemMeta();
		
		savedMeta.setDisplayName(newName);
		
		this.result.setItemMeta(savedMeta);
	}
}
