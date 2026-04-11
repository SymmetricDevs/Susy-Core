package supersymmetry.common.command;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import supersymmetry.common.faction.FactionViolenceManager;

public class CommandFactionViolence extends CommandBase {

    //replace with gamerule later, default enabled
    //still a command for now
    @Override
    public String getName() {
        return "factionViolence";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/factionViolence <on|off|toggle>";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "on", "off", "toggle");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString("Usage: /factionViolence <on|off|toggle>"));
            return;
        }

        String arg = args[0].toLowerCase();

        switch (arg) {
            case "on":
                FactionViolenceManager.setEnabled(true);
                sender.sendMessage(new TextComponentString("Faction violence ENABLED"));
                break;
            case "off":
                FactionViolenceManager.setEnabled(false);
                sender.sendMessage(new TextComponentString("Faction violence DISABLED"));
                break;
            case "toggle":
                FactionViolenceManager.toggle();
                sender.sendMessage(new TextComponentString("Faction violence is now " +
                        (FactionViolenceManager.isEnabled() ? "ENABLED" : "DISABLED")));
                break;
            default:
                sender.sendMessage(new TextComponentString("Invalid argument. Use on, off, or toggle."));
                break;
        }
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
