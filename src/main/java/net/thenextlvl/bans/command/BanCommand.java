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
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Getter
public class BanCommand extends Command implements PluginIdentifiableCommand {
    private final BanPlugin plugin;

    public BanCommand(BanPlugin plugin) {
        super("ban", "Ban a player", "/ban [player] (time) (reason)", Collections.emptyList());
        setPermission("bans.command.ban");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) plugin.bundle().sendMessage(sender, "command.usage.ban");
        else Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            var player = Bukkit.getOfflinePlayer(args[0]);
            if (player.getPlayer() != null) player.getPlayer().getScheduler()
                    .run(plugin, task1 -> ban(sender, player, args), null);
            else ban(sender, player, args);
        });
        return true;
    }

    private void ban(CommandSender sender, OfflinePlayer player, String[] args) {
        var expires = args.length >= 2 ? parseDuration(args[1]) : Long.valueOf(-1L);

        if (expires == null) {
            plugin.bundle().sendMessage(sender, "command.usage.ban");
            return;
        }

        var source = sender instanceof Player ? sender.getName() : "Server";
        var time = expires > 0 ? new Date(System.currentTimeMillis() + expires) : null;
        var reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;

        var ban = player.ban(reason, time, source);
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

    static @Nullable Long parseDuration(String expiration) {
        if (expiration.equals("forever")) return -1L;
        if (!expiration.matches("^[1-9]\\d{0,2}(m|h|d|w|mo|y)$")) return null;
        var unit = parseUnit(expiration);
        if (unit == null) return null;
        var time = parseTime(expiration);
        if (time == null) return null;

        var now = ZonedDateTime.now();
        var then = now;

        then = switch (unit) {
            case MINUTES -> then.plusMinutes(time);
            case HOURS -> then.plusHours(time);
            case DAYS -> then.plusDays(time);
            case WEEKS -> then.plusWeeks(time);
            case MONTHS -> then.plusMonths(time);
            case YEARS -> then.plusYears(time);
            default -> then;
        };

        return ChronoUnit.MILLIS.between(now, then);
    }

    static @Nullable Long parseTime(String expiration) {
        try {
            return Long.parseLong(expiration.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static @Nullable ChronoUnit parseUnit(String input) {
        if (input.endsWith("m")) return ChronoUnit.MINUTES;
        if (input.endsWith("h")) return ChronoUnit.HOURS;
        if (input.endsWith("d")) return ChronoUnit.DAYS;
        if (input.endsWith("w")) return ChronoUnit.WEEKS;
        if (input.endsWith("mo")) return ChronoUnit.MONTHS;
        if (input.endsWith("y")) return ChronoUnit.YEARS;
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return (args.length <= 1 ? Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(s -> s.contains(args[args.length - 1]))
                : args.length == 2
                ? times(args[1])
                : Stream.<String>empty())
                .filter(s -> s.contains(args[args.length - 1]))
                .toList();
    }

    private Stream<String> times(String input) {
        if (input.isEmpty()) return Stream.of("forever", "10m", "1h", "12h", "1d", "7d", "1mo", "6mo", "1y");
        if ("forever".startsWith(input)) return Stream.of("forever");
        var units = Stream.of("m", "h", "d", "w", "mo", "y")
                .map(unit -> input + unit);
        if (input.matches("^[1-9]\\d{0,2}$")) return units;
        return Stream.empty();
    }
}
