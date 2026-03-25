package supersymmetry.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import org.jetbrains.annotations.NotNull;

import supersymmetry.common.event.MobHordePlayerData;
import supersymmetry.common.event.MobHordeWorldData;

public class CommandHordeStop extends CommandBase {

    @NotNull
    @Override
    public String getName() {
        return "stop";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "susy.command.horde.stop.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;

            MobHordePlayerData playerData = MobHordeWorldData.get(player.world)
                    .getPlayerData(player.getPersistentID());

            if (!playerData.hasActiveInvasion) {
                ITextComponent textComponent = new TextComponentTranslation(
                        "susy.command.horde.stop.has_active_no_invasion");
                sender.sendMessage(textComponent);
                return;
            }

            String invasion = playerData.currentInvasion;

            playerData.stopInvasion(player);

            ITextComponent textComponent = new TextComponentTranslation("susy.command.horde.stop.stopped", invasion);
            sender.sendMessage(textComponent);
        }
    }
}
