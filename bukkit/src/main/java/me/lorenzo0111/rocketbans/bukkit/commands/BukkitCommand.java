package me.lorenzo0111.rocketbans.bukkit.commands;

import me.lorenzo0111.rocketbans.bukkit.commands.subcommands.HistoryCommand;
import me.lorenzo0111.rocketbans.bukkit.commands.subcommands.WarnsCommand;
import me.lorenzo0111.rocketbans.bukkit.platform.entity.BukkitAdapter;
import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BukkitCommand extends RocketBansCommand implements TabExecutor {

    public BukkitCommand(RocketBansPlugin plugin) {
        super(plugin);

        register(new WarnsCommand(this));
        register(new HistoryCommand(this));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        handleCommand(BukkitAdapter.sender(sender), label, args);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return handleTab(BukkitAdapter.sender(sender), label, args);
    }
}
