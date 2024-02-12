package supersymmetry.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.event.MobHordeEvent;

import java.util.List;

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
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (args.length > 0) {
                String name = args[0];
                MobHordeEvent event = MobHordeEvent.EVENTS.get(name);

                if(event == null) {
                    throw new CommandException("susy.command.horde.start.no_such_horde", name);
                }

                if(!event.canRun(player)) {
                    throw new CommandException("susy.command.horde.start.unable_to_run");
                }

                if(!event.run(player)) {
                    throw new CommandException("susy.command.horde.start.error_executing_horde");
                }

            }
        }
    }
}
