package net.thenextlvl.bans.command;

import io.papermc.paper.ban.BanListType;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

@Getter
public class PardonIPCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public PardonIPCommand(BanPlugin plugin) {
        super("pardon-ip", "Unban an ip address", "/pardon-ip [address]", List.of("unban-ip"));
        setPermission("bans.command.pardon-ip");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) plugin.bundle().sendMessage(sender, "command.usage.pardon-ip");
        else try {
            var address = InetAddress.getByName(args[0]);
            var message = Bukkit.getBanList(BanListType.IP).isBanned(address)
                    ? "command.pardon-ip.success" : "command.pardon-ip.failed";
            plugin.bundle().sendMessage(sender, message,
                    Placeholder.parsed("address", address.getHostAddress()));
            Bukkit.getBanList(BanListType.IP).pardon(address);
        } catch (UnknownHostException e) {
            plugin.bundle().sendMessage(sender, "command.usage.pardon-ip");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Bukkit.getIPBans().stream()
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
