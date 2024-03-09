package net.thenextlvl.bans.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.bans.BanPlugin;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public record ConnectionListener(BanPlugin plugin) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        if (switch (event.getCause()) {
            case BANNED, IP_BANNED -> false;
            default -> true;
        }) return;
        var address = event.getPlayer().getAddress();
        var message = address == null ? message(event.getPlayer().getPlayerProfile(), event.getPlayer().locale())
                : message(event.getPlayer().getPlayerProfile(), event.getPlayer().locale(), address.getAddress());
        if (message != null) event.reason(message);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        if (switch (event.getResult()) {
            case ALLOWED, KICK_BANNED -> false;
            default -> true;
        }) return;
        var message = message(event.getPlayer().getPlayerProfile(), Locale.US, event.getAddress());
        if (message != null) event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
    }

    private @Nullable Component message(PlayerProfile profile, Locale locale, InetAddress address) {
        var entry = Bukkit.getBanList(BanListType.IP)
                .<BanEntry<InetAddress>>getEntries().stream()
                .filter(ban -> ban.getBanTarget().equals(address))
                .findAny()
                .orElse(null);
        if (entry == null) return message(profile, locale);
        return plugin.bundle().component(locale, "disconnect.banned-ip.format",
                Placeholder.parsed("source", entry.getSource()),
                Placeholder.parsed("reason", entry.getReason() != null ? entry.getReason() : "-/-"),
                Placeholder.parsed("time", entry.getExpiration() != null ? format(entry.getExpiration()) : "∞")
        );
    }

    private @Nullable Component message(PlayerProfile profile, Locale locale) {
        var entry = Bukkit.getBanList(BanListType.PROFILE).getBanEntry(profile);
        if (entry == null) return null;
        return plugin.bundle().component(locale, "disconnect.banned.format",
                Placeholder.parsed("source", entry.getSource()),
                Placeholder.parsed("reason", entry.getReason() != null ? entry.getReason() : "-/-"),
                Placeholder.parsed("time", entry.getExpiration() != null ? format(entry.getExpiration()) : "∞")
        );
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
