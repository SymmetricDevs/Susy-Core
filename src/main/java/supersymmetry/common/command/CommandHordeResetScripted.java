package supersymmetry.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.event.MobHordeEvent;
import supersymmetry.common.event.MobHordePlayerData;
import supersymmetry.common.event.MobHordeWorldData;

import java.util.List;
import java.util.stream.Collectors;

public class CommandHordeResetScripted extends CommandBase {

    @NotNull
    @Override
    public String getName() {
        return "resetscripted";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "susy.command.horde.resetscripted.usage";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {

        List<String> list = MobHordeEvent.EVENTS.values()
                .stream()
                .map(event -> event.KEY)
                .collect(Collectors.toList());

        list.add("all");

        return list;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            throw new CommandException("Command can only be used by a player");
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;

        MobHordePlayerData playerData = MobHordeWorldData.get(player.world)
                .getPlayerData(player.getPersistentID());

        if (args.length < 1) {
            throw new CommandException("Usage: /resetscripted <event|all>");
        }

        String name = args[0];

        if (name.equalsIgnoreCase("all")) {
            playerData.completedScriptedEvents.clear();

            sender.sendMessage(new TextComponentTranslation(
                    "susy.command.horde.resetscripted.all"
            ));
            return;
        }

        MobHordeEvent event = MobHordeEvent.EVENTS.get(name);

        if (event == null) {
            throw new CommandException("susy.command.horde.resetscripted.no_such_horde", name);
        }

        if (!playerData.hasCompleted(event.KEY)) {
            sender.sendMessage(new TextComponentTranslation(
                    "susy.command.horde.resetscripted.not_set",
                    event.KEY
            ));
            return;
        }

        playerData.completedScriptedEvents.remove(event.KEY);

        sender.sendMessage(new TextComponentTranslation(
                "susy.command.horde.resetscripted.success",
                event.KEY
        ));
    }
}
