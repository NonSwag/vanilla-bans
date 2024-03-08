package net.thenextlvl.bans.command;

import io.papermc.paper.ban.BanListType;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class BanlistCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public BanlistCommand(BanPlugin plugin) {
        super("banlist", "List all bans", "/banlist ips | players", Collections.emptyList());
        setPermission("bans.command.banlist");
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean execute(CommandSender sender, String label, String[] args) {
        var players = args.length == 0 || args[0].equals("players");
        var ips = args.length == 0 || args[0].equals("ips");

        if (args.length > 1 || (!players && !ips)) {
            plugin.bundle().sendMessage(sender, "command.usage.banlist");
            return true;
        }

        var entries = new LinkedHashSet<BanEntry<?>>();
        if (players) entries.addAll(Bukkit.getBanList(BanListType.PROFILE).getEntries());
        if (ips) entries.addAll(Bukkit.getBanList(BanListType.IP).getEntries());

        var message = entries.isEmpty() ? "command.banlist.empty"
                : entries.size() == 1 ? "command.banlist.single"
                : "command.banlist.count";

        plugin.bundle().sendMessage(sender, message, Placeholder.parsed("amount", String.valueOf(entries.size())));

        entries.forEach(entry -> plugin.bundle().sendMessage(sender, "command.banlist.info",
                Placeholder.parsed("source", entry.getSource()),
                Placeholder.parsed("target", entry.getTarget()),
                Placeholder.parsed("reason", entry.getReason() != null ? entry.getReason() : "-/-")
        ));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return args.length <= 1 ? Stream.of("ips", "players")
                .filter(s -> s.contains(args[args.length - 1]))
                .toList() : Collections.emptyList();
    }
}
