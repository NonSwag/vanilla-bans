package net.thenextlvl.bans.command;

import lombok.Getter;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import java.util.Collections;
import java.util.List;

@Getter
public class PardonIPCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public PardonIPCommand(BanPlugin plugin) {
        super("pardon-ip", "Unban an ip address", "/pardon-ip [player] (reason)", List.of("unban-ip"));
        setPermission("bans.command.pardon-ip");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Bukkit.getIPBans().stream()
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
