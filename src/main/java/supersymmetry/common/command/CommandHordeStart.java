package supersymmetry.common.command;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import supersymmetry.api.event.MobHordeEvent;
import supersymmetry.common.event.MobHordePlayerData;
import supersymmetry.common.event.MobHordeWorldData;

public class CommandHordeStart extends CommandBase {

    @NotNull
    @Override
    public String getName() {
        return "start";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "susy.command.horde.start.usage";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        return MobHordeEvent.EVENTS.values().stream().map(event -> event.KEY).collect(Collectors.toList());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (args.length > 0) {
                String name = args[0];
                MobHordeEvent event = MobHordeEvent.EVENTS.get(name);

                MobHordePlayerData playerData = MobHordeWorldData.get(player.world)
                        .getPlayerData(player.getPersistentID());

                if (event == null) {
                    throw new CommandException("susy.command.horde.start.no_such_horde", name);
                }

                if (!event.canRun(player)) {
                    throw new CommandException("susy.command.horde.start.unable_to_run", name);
                }

                if (playerData.hasActiveInvasion) {
                    // true => overwrite existing invasion
                    if (args.length > 1 && args[1].equals("true")) {
                        playerData.stopInvasion(player);
                    } else {
                        ITextComponent textComponent = new TextComponentTranslation(
                                "susy.command.horde.start.has_active_invasion", playerData.currentInvasion);
                        sender.sendMessage(textComponent);
                        return;
                    }
                }

                if (!event.run(player, playerData::addEntity)) {
                    throw new CommandException("susy.command.horde.start.error_executing_horde");
                }

                playerData.setCurrentInvasion(event);
                ITextComponent textComponent = new TextComponentTranslation("susy.command.horde.start.started",
                        event.KEY);
                sender.sendMessage(textComponent);
            } else {
                throw new CommandException("susy.command.horde.start.argument_required");
            }
        }
    }
}
