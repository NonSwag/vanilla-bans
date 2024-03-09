package net.thenextlvl.bans.command;

import io.papermc.paper.ban.BanListType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static net.thenextlvl.bans.command.BanCommand.parseDuration;

@Getter
public class BanIPCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public BanIPCommand(BanPlugin plugin) {
        super("ban-ip", "Ban an ip address", "/ban-ip [address] (time) (reason)", Collections.emptyList());
        setPermission("bans.command.ban-ip");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) plugin.bundle().sendMessage(sender, "command.usage.ban-ip");
        else Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            try {
                var player = Bukkit.getPlayer(args[0]);
                var address = player != null && player.getAddress() != null
                        ? player.getAddress().getAddress()
                        : InetAddress.getByName(args[0]);
                banIp(sender, address, args);
            } catch (UnknownHostException e) {
                plugin.bundle().sendMessage(sender, "command.usage.ban-ip");
            }
        });
        return true;
    }

    private void banIp(CommandSender sender, InetAddress address, String[] args) {
        var expires = args.length >= 2 ? parseDuration(args[1]) : Long.valueOf(-1L);

        if (expires == null) {
            plugin.bundle().sendMessage(sender, "command.usage.ban-ip");
            return;
        }

        var source = sender instanceof Player ? sender.getName() : "Server";
        var time = expires > 0 ? new Date(System.currentTimeMillis() + expires) : null;
        var reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;

        var ban = Bukkit.getBanList(BanListType.IP).addBan(address, reason, time, source);
        if (ban != null) ban.save();

        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getAddress() != null
                        && player.getAddress().getAddress().equals(address))
                .forEach(player -> player.getScheduler().run(plugin, task ->
                        player.kick(Component.empty(), PlayerKickEvent.Cause.IP_BANNED), null));

        var message = (ban != null
                ? (ban.getExpiration() != null
                ? (reason != null
                ? "command.ban-ip.success.expiring.reason"
                : "command.ban-ip.success.expiring")
                : (reason != null
                ? "command.ban-ip.success.indefinite.reason"
                : "command.ban-ip.success.indefinite"))
                : "command.ban-ip.failure");

        var expiration = ban != null && ban.getExpiration() != null
                ? ban.getExpiration().toString()
                : "-/-";
        plugin.bundle().sendMessage(sender, message,
                Placeholder.parsed("address", address.getHostAddress()),
                Placeholder.parsed("reason", reason != null ? reason : "-/-"),
                Placeholder.parsed("date", expiration)
        );

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
