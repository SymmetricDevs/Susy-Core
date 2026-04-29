package supersymmetry.common.command;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import supersymmetry.common.faction.FactionHateManager;
import supersymmetry.common.util.FactionHelper;

public class CommandFactionHate extends CommandBase {

    @Override
    public String getName() {
        return "factionHate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/factionHate <get|add|set>";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {

        // /factionHate <subcommand>
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "get", "add", "set");
        }

        // /factionHate <subcommand> <faction>
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, FactionHelper.FACTIONS);
        }

        // /factionHate <subcommand> <faction> <value>
        if (args.length == 3) {
            String sub = args[0].toLowerCase();

            if (sub.equals("add") || sub.equals("set")) {
                // common useful values for hate manipulation
                return getListOfStringsMatchingLastWord(args, "<number>");
            }

            // "get" doesn't need a third argument
            return java.util.Collections.emptyList();
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(
                    new TextComponentString("Usage: /factionHate <get|add|set> | /factionHate add Bandits 5"));
            return;
        }

        String arg = args[0].toLowerCase();
        String faction = args[1];

        switch (arg) {
            case "get":
                int hate = FactionHateManager.getHate((EntityPlayer) sender, faction);
                sender.sendMessage(new TextComponentString("current HATE for " + faction + ": " + hate));
                break;
            case "add":
                int hate1 = Integer.parseInt(args[2]);
                FactionHateManager.addHate((EntityPlayer) sender, faction, hate1);
                sender.sendMessage(new TextComponentString("Added " + hate1 + " HATE to faction: " + faction));
                break;
            case "set":
                int hate2 = Integer.parseInt(args[2]);
                FactionHateManager.setHate((EntityPlayer) sender, faction, hate2);
                sender.sendMessage(new TextComponentString("Set " + hate2 + " HATE for faction: " + faction));
                break;
            default:
                sender.sendMessage(new TextComponentString("Invalid argument. Use get, add, or set."));
                break;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
