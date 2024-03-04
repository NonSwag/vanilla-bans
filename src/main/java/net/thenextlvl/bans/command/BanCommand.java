package net.thenextlvl.bans.command;

import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class BanCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public BanCommand(BanPlugin plugin) {
        super("ban", "Ban a player", "/ban [player] (reason)", Collections.emptyList());
        setPermission("bans.command.ban");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) plugin.bundle().sendMessage(sender, "command.usage.ban");
        else Bukkit.getAsyncScheduler().runNow(plugin, task -> ban(sender, args));
        return true;
    }

    private void ban(CommandSender sender, String[] args) {
        var player = Bukkit.getOfflinePlayer(args[0]);
        var reason = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        var ban = player.ban(reason, (Date) null, sender instanceof Player ? sender.getName() : "Server");

        if (ban != null) ban.save();

        var message = (ban != null
                ? (ban.getExpiration() != null
                ? (reason != null
                ? "command.ban.success.expiring.reason"
                : "command.ban.success.expiring")
                : (reason != null
                ? "command.ban.success.indefinite.reason"
                : "command.ban.success.indefinite"))
                : "command.ban.failure");

        var expiration = ban != null && ban.getExpiration() != null
                ? ban.getExpiration().toString()
                : "-/-";
        plugin.bundle().sendMessage(sender, message,
                Placeholder.parsed("player", player.getName() != null ? player.getName() : args[0]),
                Placeholder.parsed("reason", reason != null ? reason : "-/-"),
                Placeholder.parsed("date", expiration)
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
