package net.thenextlvl.bans;

import core.i18n.file.ComponentBundle;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.bans.command.*;
import net.thenextlvl.bans.listener.ConnectionListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;

@Getter
@Accessors(fluent = true)
public class BanPlugin extends JavaPlugin {
    private final Metrics metrics = new Metrics(this, 21221);

    private final File translations = new File(getDataFolder(), "translations");
    private final ComponentBundle bundle = new ComponentBundle(translations, audience ->
            audience instanceof Player player ? player.locale() : Locale.US)
            .register("vanilla_bans", Locale.US)
            .register("vanilla_bans_german", Locale.GERMANY);

    @Override
    public void onLoad() {
        bundle().miniMessage(MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolvers(TagResolver.standard())
                        .resolver(Placeholder.component("prefix", bundle.component(Locale.US, "prefix")))
                        .build())
                .build());
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);

        Bukkit.getCommandMap().register(getName(), new BanCommand(this));
        Bukkit.getCommandMap().register(getName(), new BanIPCommand(this));
        Bukkit.getCommandMap().register(getName(), new BanlistCommand(this));
        Bukkit.getCommandMap().register(getName(), new PardonCommand(this));
        Bukkit.getCommandMap().register(getName(), new PardonIPCommand(this));
    }

    @Override
    public void onDisable() {
        metrics.shutdown();
    }
}