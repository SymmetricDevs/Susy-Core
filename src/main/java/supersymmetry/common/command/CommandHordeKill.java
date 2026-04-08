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

public class CommandHordeKill extends CommandBase {

    @NotNull
    @Override
    public String getName() {
        return "kill";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "susy.command.horde.kill.usage";
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

            playerData.killInvasion(player);

            ITextComponent textComponent = new TextComponentTranslation("susy.command.horde.stop.killed", invasion);
            sender.sendMessage(textComponent);
        }
    }
}
