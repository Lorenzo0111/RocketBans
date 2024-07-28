package me.lorenzo0111.rocketbans.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import me.lorenzo0111.rocketbans.RocketBansPlugin;
import me.lorenzo0111.rocketbans.commands.RocketBansCommand;
import me.lorenzo0111.rocketbans.velocity.commands.subcommands.HistoryCommand;
import me.lorenzo0111.rocketbans.velocity.platform.entity.VelocityAdapter;

import java.util.List;

public class VelocityCommand extends RocketBansCommand implements SimpleCommand {

    public VelocityCommand(RocketBansPlugin plugin) {
        super(plugin);

        register(new HistoryCommand(this));
    }

    @Override
    public void execute(Invocation invocation) {
        this.handleCommand(VelocityAdapter.sender(invocation.source()), invocation.alias(), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return this.handleTab(VelocityAdapter.sender(invocation.source()), invocation.alias(), invocation.arguments());
    }
}
