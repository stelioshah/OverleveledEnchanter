package me.jingo.ove;
import java.util.Map;
import java.util.Optional;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import me.jingo.enchantment.LoreCorrector;
import me.jingo.ovecmd.OveCmd;
import me.jingo.ovecmd.OveTabCompleter;

public class OverleveledEnchanter extends JavaPlugin{
	
	public static final String noMaxLevelFoundWarn =  "Warning! No max level found for enchantment: "+ "'%s'" 
			+ "!...Using default max level (" + "%d" + ")!" + " Modify the config to remove this warning...";
	
	public static final String noCombLevelFoundWarn =  "Warning! No ombination level found for enchantment: " + "'%s'" 
			+ "!...Using default combination level (" + "%d" + ")!" + " Modify the config to remove this warning...";
	
	public static final String noMaxExpLevelFoundWarn = "Warning! No max experience level found in the config! Using 39 levels by default!";
	
	public static final String noBoosterFound = "Warning! No %s booster found!";
	
	public final Map.Entry<NamespacedKey, PersistentDataType<Integer,Integer>> expLoreKey = Map.entry(new NamespacedKey(this, "exploreline"), PersistentDataType.INTEGER);
	
	@Override
	public void onEnable() {
		
		getConfig().options().copyDefaults();
		
		saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(new Anvil(this), this);
		
		if(this.getConfig().getBoolean("Enchanting-Table-Level-Fixer"))
			getServer().getPluginManager().registerEvents(new EnchantingTable(this), this);
		
		getServer().getPluginManager().registerEvents(new LoreCorrector(this), this);
		
		getCommand("ove").setExecutor(new OveCmd(this));
		
		getCommand("ove").setTabCompleter(new OveTabCompleter());
	}
	
	public boolean isUncapped() {return this.getConfig().getBoolean("Uncapped-Merge-Combination");}
	
	public boolean isBoosterEnabled() {return this.getConfig().getBoolean("Rank-Level-Booster");}
	
	public boolean isAlwaysMaxExpCost() {return this.getConfig().getBoolean("Too-Expensive-Always-Max-Level-Cost");}
	
	public int getMaxLevelBooster(Player thePlayer) {
		
		return getBooster(thePlayer, ".maxLevel");
	}
	
	public int getCombLevelBooster(Player thePlayer) {
		
		return getBooster(thePlayer, ".combining-level");
	}
	
	private int getBooster(Player thePlayer, String type) {
		
		//If the booster is disabled as an option, there should be no value for it. The booster is being added on the max level. Max level + 0 = max level.
		if(!this.isBoosterEnabled()) return 0;
		
		if(thePlayer.hasPermission("overleveledenchanter.admin")) {
			
			if(!this.getConfig().contains("Admin-Level-Booster" + type)) {
				this.getLogger().severe(String.format(noBoosterFound, type));
				return 0;
			}
			else 
				return this.getConfig().getInt("Admin-Level-Booster" + type);
		}
		
		Optional<String> booster = this.getConfig().getConfigurationSection("Level-Booster").getKeys(false).stream().filter(tier -> 
		thePlayer.hasPermission("overleveledenchanter.booster." + tier)).findFirst();
		
		if(booster.isEmpty()) return 0;
		
		return this.getConfig().getInt("Level-Booster." + booster.get() + type);
	}
	
	public int getMaxLevel(Enchantment ench) {
		
		return getLevel(ench, ".maxLevel", OverleveledEnchanter.noMaxLevelFoundWarn);
	}
	
	public int getCombLevel(Enchantment ench) {
		
		return getLevel(ench, ".combining-level", OverleveledEnchanter.noCombLevelFoundWarn);
	}
	
	private int getLevel(Enchantment ench, String path, String warning) {
		
		if(this.getConfig().contains(getEnchKey(ench) + path))
			return this.getConfig().getInt(getEnchKey(ench) + path);
		
		else {
			this.getLogger().severe(String.format(warning, ench.getKey().getKey(), ench.getMaxLevel()));
			return ench.getMaxLevel();
		}
	}
	
	public boolean isLevelInvalid(int theLevel) {
		
		return theLevel < -1;
	}
	
	public boolean hasNoMaxLevel(int theLevel) {
		
		return theLevel == -1;	
	}
	
	private String getEnchKey(Enchantment ench) {
		
		return "Enchantments." + ench.getKey().getKey();
	}
	
}
