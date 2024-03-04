package net.thenextlvl.bans.command;

import lombok.Getter;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@Getter
public class BanIPCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public BanIPCommand(BanPlugin plugin) {
        super("ban-ip", "Ban an ip address", "/ban-ip [address] (reason)", Collections.emptyList());
        setPermission("bans.command.ban-ip");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
