package me.jingo.ovecmd;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class OveTabCompleter implements TabCompleter{
	
	private static final List<String> comnandList = List.of("reload");
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		if(!sender.hasPermission("overleveledenchanter.admin")) return Collections.emptyList();
		
		if(args.length > 1) return Collections.emptyList();
		
		if("".equals(args[0])) return comnandList;
		
		return comnandList.stream().filter(cmd -> cmd.length() > args[0].length() &&
		cmd.substring(0, args[0].length()).equalsIgnoreCase(args[0])).collect(Collectors.toList());
	}
	
}