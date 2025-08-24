package me.confuser.banmanager.bukkit.admingui;

import me.confuser.banmanager.bukkit.admingui.commands.Admin;
import me.confuser.banmanager.bukkit.admingui.commands.AdminChat;
import me.confuser.banmanager.bukkit.admingui.commands.CommandSpy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command handler that intercepts AdminGUI commands and delegates to the actual AdminGUI implementations
 */
public class AdminGuiCommandHandler implements CommandExecutor {
    
    private final Admin adminCommand;
    private final AdminChat adminChatCommand;
    private final CommandSpy commandSpyCommand;
    
    public AdminGuiCommandHandler() {
        this.adminCommand = new Admin();
        this.adminChatCommand = new AdminChat();
        this.commandSpyCommand = new CommandSpy();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "admin":
                return adminCommand.onCommand(sender, command, label, args);
            case "adminchat":
                return adminChatCommand.onCommand(sender, command, label, args);
            case "admincommandspy":
                return commandSpyCommand.onCommand(sender, command, label, args);
            default:
                return false;
        }
    }
} 