package net.thenextlvl.bans.command;

import lombok.Getter;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class BanlistCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public BanlistCommand(BanPlugin plugin) {
        super("banlist", "List all bans", "/banlist (ips/players)", Collections.emptyList());
        setPermission("bans.command.banlist");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return false;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Stream.of("ips", "players")
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
