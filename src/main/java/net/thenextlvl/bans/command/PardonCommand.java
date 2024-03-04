package net.thenextlvl.bans.command;

import lombok.Getter;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class PardonCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public PardonCommand(BanPlugin plugin) {
        super("pardon", "Unban a player", "/pardon [player] (reason)", List.of("unban"));
        setPermission("bans.command.pardon");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Bukkit.getBannedPlayers().stream()
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
