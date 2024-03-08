package net.thenextlvl.bans.listener;

import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_BANNED;

public record ConnectionListener(BanPlugin plugin) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        var entry = Bukkit.getBanList(BanListType.PROFILE).getBanEntry(event.getPlayerProfile());
        if (entry == null) return;
        event.disallow(KICK_BANNED, plugin.bundle().component(Locale.US, "disconnect.banned.format",
                Placeholder.parsed("source", entry.getSource()),
                Placeholder.parsed("reason", entry.getReason() != null ? entry.getReason() : "-/-"),
                Placeholder.parsed("time", entry.getExpiration() != null ? format(entry.getExpiration()) : "∞")
        ));
    }

    private static final ChronoUnit[] units = {
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.WEEKS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
    };

    public static String format(Date expires) {
        var now = LocalDateTime.now();
        var expireDateTime = LocalDateTime.ofInstant(expires.toInstant(), ZoneId.systemDefault());
        var builder = new StringBuilder();
        for (var unit : units) {
            var difference = unit.between(now, expireDateTime);
            if (difference <= 0) continue;
            builder.append(difference).append(" ").append(name(unit));
            builder.append(difference > 1 ? "s, " : ", ");
            now = now.plus(difference, unit);
        }
        if (builder.length() >= 2) builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

    private static @Nullable String name(ChronoUnit unit) {
        return switch (unit) {
            case SECONDS -> "second";
            case MINUTES -> "minute";
            case HOURS -> "hour";
            case DAYS -> "day";
            case WEEKS -> "week";
            case MONTHS -> "month";
            case YEARS -> "year";
            case FOREVER -> "forever";
            default -> null;
        };
    }
}
