package de.howaner.FramePicture.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.howaner.FramePicture.FrameManager;
import de.howaner.FramePicture.FramePicturePlugin;
import de.howaner.FramePicture.util.Cache;
import de.howaner.FramePicture.util.Config;
import de.howaner.FramePicture.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class FramePictureCommand implements CommandExecutor {
	private FrameManager manager;
	
	public FramePictureCommand(FrameManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 0)
			return sendHelp(sender);
		String aufgabe = args[0];
		///SET
		if (aufgabe.equalsIgnoreCase("set")) {
			//Is a Player?
			if (!(sender instanceof Player)) {
				sender.sendMessage(Lang.PREFIX.getText() + Lang.NO_PLAYER.getText());
				return true;
			}
			final Player player = (Player)sender;
			//Permission
			if (!player.hasPermission("FramePicture.set")) {
				player.sendMessage(Lang.PREFIX.getText() + Lang.NO_PERMISSION.getText());
				return true;
			}
			//Bestellvorgang abbrechen, wenn args = 1
			if (args.length == 1 && Cache.hasCacheCreating(player)) {
				Cache.removeCacheCreating(player);
				player.sendMessage(Lang.PREFIX.getText() + Lang.CREATING_CANCELLED.getText());
				return true;
			}
			//Args prüfen
			if (args.length < 2)
				return sendHelp(sender);
			//Hat er bereits einen Erstellvorgang?
			if (Cache.hasCacheCreating(player)) {
				player.sendMessage(Lang.PREFIX.getText() + Lang.ALREADY_SELECTION.getText());
				return true;
			}
			//Money
			if (Config.MONEY_ENABLED) {
				if (!FramePicturePlugin.getEconomy().has(player.getName(), Config.CREATE_PRICE)) {
					player.sendMessage(Lang.NOT_ENOUGH_MONEY.getText());
					return true;
				}
			}
			
			//Erstellung
			StringBuilder pathBuilder = new StringBuilder();
			for (int i = 1; i < args.length; i++) {
				if (i != 1) pathBuilder.append(" ");
				pathBuilder.append(args[i]);
			}
			final String path = pathBuilder.toString();
			
			//player.sendMessage(Lang.PREFIX.getText() + Lang.PLEASE_WAIT.getText());
			Cache.setCacheCreating(player, path);
			player.sendMessage(Lang.PREFIX.getText() + Lang.CLICK_FRAME.getText());
			return true;
		}
		///MULTISET
		else if (aufgabe.equalsIgnoreCase("multiset")) {
			//Is a Player?
			if (!(sender instanceof Player)) {
				sender.sendMessage(Lang.PREFIX.getText() + Lang.NO_PLAYER.getText());
				return true;
			}
			final Player player = (Player)sender;
			//Permission
			if (!player.hasPermission("FramePicture.multiset")) {
				player.sendMessage(Lang.PREFIX.getText() + Lang.NO_PERMISSION.getText());
				return true;
			}
			//Bestellvorgang abbrechen, wenn args = 1
			if (args.length == 1 && Cache.hasCacheMultiCreating(player)) {
				Cache.removeCacheMultiCreating(player);
				player.sendMessage(Lang.PREFIX.getText() + Lang.CREATING_CANCELLED.getText());
				return true;
			}
			//Args prüfen
			if (args.length < 2)
				return sendHelp(sender);
			//Hat er bereits einen Erstellvorgang?
			if (Cache.hasCacheMultiCreating(player)) {
				player.sendMessage(Lang.PREFIX.getText() + Lang.ALREADY_SELECTION.getText());
				return true;
			}
			//Money
			if (Config.MONEY_ENABLED) {
				if (!FramePicturePlugin.getEconomy().has(player.getName(), Config.CREATE_PRICE)) {
					player.sendMessage(Lang.NOT_ENOUGH_MONEY.getText());
					return true;
				}
			}
			
			//Erstellung
			StringBuilder pathBuilder = new StringBuilder();
			for (int i = 1; i < args.length; i++) {
				if (i != 1) pathBuilder.append(" ");
				pathBuilder.append(args[i]);
			}
			final String path = pathBuilder.toString();
			
			Cache.setCacheMultiCreating(player, path);
			player.sendMessage(Lang.PREFIX.getText() + Lang.CLICK_MULTIFRAME.getText());
			return true;
		}
		///GET
		else if (aufgabe.equalsIgnoreCase("get")) {
			//Args
			if (args.length != 1)
				return sendHelp(sender);
			//Ist er ein Spieler?
			if (!(sender instanceof Player)) {
				sender.sendMessage(Lang.PREFIX.getText() + Lang.NO_PLAYER.getText());
				return true;
			}
			Player player = (Player)sender;
			//Permission
			if (!player.hasPermission("FramePicture.get")) {
				player.sendMessage(Lang.PREFIX.getText() + Lang.NO_PERMISSION.getText());
				return true;
			}
			//Cache setzen
			if (Cache.hasCacheGetting(player)) {
				player.sendMessage(Lang.PREFIX.getText() + Lang.GETTING_MODE_DISABLED.getText());
				Cache.removeCacheGetting(player);
			} else {
				player.sendMessage(Lang.PREFIX.getText() + Lang.GETTING_MODE_ENABLED.getText());
				Cache.addCacheGetting(player);
			}
			return true;
		}
		///RELOAD
		else if (aufgabe.equalsIgnoreCase("reload")) {
			//Args
			if (args.length != 1)
				return sendHelp(sender);
			//Ist er ein Spieler?
			if (sender instanceof Player) {
				Player player = (Player)sender;
				if (!player.hasPermission("FramePicture.reload")) {
					player.sendMessage(Lang.PREFIX.getText() + Lang.NO_PERMISSION.getText());
					return true;
				}
			}
			//Alles neuladen
			if (!Config.configFile.exists()) Config.save();
			Config.load();
			Config.save();
			Lang.load();
			
			manager.loadFrames();
			manager.saveFrames();
			
			// Money
			if (Config.MONEY_ENABLED) {
				FramePicturePlugin.getPlugin().setupEconomy();
				if (FramePicturePlugin.getEconomy() == null) {
					FramePicturePlugin.log.info("Vault not found! Money Support disabled!");
					Config.MONEY_ENABLED = false;
					Config.save();
				}
			}
			
			this.manager.getFrameLoader().reload();
			
			manager.getLogger().info("Plugin reloaded!");
			sender.sendMessage(Lang.PREFIX.getText() + Lang.PLUGIN_RELOAD.getText());
			return true;
		} else
			return sendHelp(sender);
	}
	
	public boolean sendHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Help from /FramePicture or /fp:");
		sender.sendMessage("/FramePicture set <URL>  " + ChatColor.GOLD + "--" + ChatColor.WHITE + "  Set a Picture in a Frame.");
		sender.sendMessage("/FramePicture multiset <URL>  " + ChatColor.GOLD + "--" + ChatColor.WHITE + "  Create a Picture wall.");
		sender.sendMessage("/FramePicture get  " + ChatColor.GOLD + "--" + ChatColor.WHITE + "  Get the Url from a Picture");
		sender.sendMessage("/FramePicture reload  " + ChatColor.GOLD + "--" + ChatColor.WHITE + "  Reload the Config.");
		return true;
	}

}
