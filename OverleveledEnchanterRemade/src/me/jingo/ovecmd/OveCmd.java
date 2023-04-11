package me.jingo.ovecmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.jingo.ove.OverleveledEnchanter;

public class OveCmd implements CommandExecutor{
	
	private static final String help = ChatColor.YELLOW + "-----<{" + ChatColor.GOLD +  "Overleveled Enchanter Command List"+ ChatColor.YELLOW + "}>-----"
			+ ChatColor.DARK_AQUA + "\n/ove:" + ChatColor.GRAY + " Plugin Help"
			+ ChatColor.DARK_AQUA + "\n/ove reload:" + ChatColor.GRAY + " Reload the config of the plugin";
	
	private static final String noPermission = ChatColor.RED + "You do NOT have permission to use this command!";
	
	private static final String uknownCmd = ChatColor.RED + "Uknown Command!" + 
			ChatColor.AQUA + "\nUse:" + ChatColor.GREEN + " /ove to see the available commands";
	
	private static final String successfulReload = ChatColor.GOLD + "Config succesfully reloaded!";
	
	private final OverleveledEnchanter pluginInstance;
	
	public OveCmd(OverleveledEnchanter pluginInstance) {
		
		this.pluginInstance = pluginInstance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!sender.hasPermission("overleveledenchanter.ove")) {
			sender.sendMessage(noPermission);
			return true;
		}
		
		if(args.length == 0) {sender.sendMessage(help);}
		
		else if(args[0].equalsIgnoreCase("reload")) {
			
			if(!sender.hasPermission("overleveledenchanter.admin")) {
				
				sender.sendMessage(noPermission);
				return true;
			}
			
			pluginInstance.reloadConfig();
			sender.sendMessage(successfulReload);
		}
		
		else sender.sendMessage(uknownCmd);
		
		return true;
		
	}
}
