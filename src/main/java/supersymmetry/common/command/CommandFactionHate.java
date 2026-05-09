package supersymmetry.common.command;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import supersymmetry.common.faction.FactionHateManager;
import supersymmetry.common.util.FactionHelper;

public class CommandFactionHate extends CommandBase {

    @Override
    public String getName() {
        return "factionHate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return new TextComponentTranslation(
                "susy.command.faction.generic.usage").getUnformattedText();
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
            ITextComponent textComponent = new TextComponentTranslation(
                    "susy.command.faction.generic.usage");
            sender.sendMessage(textComponent);
            return;
        }

        String arg = args[0].toLowerCase();
        String faction = args[1];

        switch (arg) {
            case "get": {
                int hate = FactionHateManager.getHate((EntityPlayer) sender, faction);
                ITextComponent text = new TextComponentTranslation(
                        "susy.command.faction.get",
                        faction,
                        hate);

                sender.sendMessage(text);
                break;
            }
            case "add": {
                int value = Integer.parseInt(args[2]);
                FactionHateManager.addHate((EntityPlayer) sender, faction, value);
                ITextComponent text = new TextComponentTranslation(
                        "susy.command.faction.add",
                        value,
                        faction);
                sender.sendMessage(text);
                break;
            }
            case "set": {
                int value = Integer.parseInt(args[2]);
                FactionHateManager.setHate((EntityPlayer) sender, faction, value);
                ITextComponent text = new TextComponentTranslation(
                        "susy.command.faction.set",
                        value,
                        faction);
                sender.sendMessage(text);
                break;
            }
            default: {
                ITextComponent text = new TextComponentTranslation(
                        "susy.command.faction.invalid");
                sender.sendMessage(text);
                break;
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
