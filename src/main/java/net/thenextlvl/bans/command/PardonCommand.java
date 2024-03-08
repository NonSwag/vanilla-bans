package net.thenextlvl.bans.command;

import io.papermc.paper.ban.BanListType;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        super("pardon", "Unban a player", "/pardon [player]", List.of("unban"));
        setPermission("bans.command.pardon");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) plugin.bundle().sendMessage(sender, "command.usage.pardon");
        else Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            var player = Bukkit.getOfflinePlayer(args[0]).getPlayerProfile();
            var message = Bukkit.getBanList(BanListType.PROFILE).isBanned(player)
                    ? "command.pardon.success" : "command.pardon.failed";
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("player", player.getName() != null ? player.getName() : args[0]));
            Bukkit.getBanList(BanListType.PROFILE).pardon(player);
        });
        return true;
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
