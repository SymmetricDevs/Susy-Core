package supersymmetry.common.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * /tpdim <dimId> [x] [y] [z]
 *
 * Teleports the executing player to the given dimension.
 * Coordinates default to (0, 128, 0) if not specified.
 *
 * Register in FMLServerStartingEvent:
 *
 * @SubscribeEvent
 *                 public void onServerStarting(FMLServerStartingEvent event) {
 *                 event.registerServerCommand(new CommandTpDim());
 *                 }
 */
public class CommandTPDimSpace extends CommandBase {

    @Override
    public String getName() {
        return "tpdim";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tpdim <dimId> [x] [y] [z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) throw new WrongUsageException(getUsage(sender));

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);

        int dimId = parseInt(args[0]);
        double x = args.length > 1 ? parseDouble(args[1]) : 0.0;
        double y = args.length > 2 ? parseDouble(args[2]) : 128.0;
        double z = args.length > 3 ? parseDouble(args[3]) : 0.0;

        if (!DimensionManager.isDimensionRegistered(dimId)) {
            throw new CommandException("Dimension " + dimId + " is not registered.");
        }

        // Ensure the world is loaded
        DimensionManager.initDimension(dimId);

        // Teleport without going through Teleporter/portal logic
        FMLCommonHandler.instance().getMinecraftServerInstance()
                .getPlayerList()
                .transferPlayerToDimension(player, dimId,
                        new net.minecraft.world.Teleporter(server.getWorld(dimId)) {

                            @Override
                            public void placeInPortal(net.minecraft.entity.Entity entity, float yaw) {
                                entity.setLocationAndAngles(x, y, z, yaw, entity.rotationPitch);
                            }

                            @Override
                            public boolean placeInExistingPortal(net.minecraft.entity.Entity entity, float yaw) {
                                entity.setLocationAndAngles(x, y, z, yaw, entity.rotationPitch);
                                return true;
                            }

                            @Override
                            public boolean makePortal(net.minecraft.entity.Entity entity) {
                                return true;
                            }

                            @Override
                            public void removeStalePortalLocations(long worldTime) {}
                        });

        // Force position after transfer (changeDimension can override it)
        player.setPositionAndUpdate(x, y, z);

        notifyCommandListener(sender, this, "Teleported to dimension %d at (%.1f, %.1f, %.1f)", dimId, x, y, z);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                          String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            Integer[] ids = DimensionManager.getStaticDimensionIDs();
            String[] idStrs = new String[ids.length];
            for (int i = 0; i < ids.length; i++) idStrs[i] = String.valueOf(ids[i]);
            return getListOfStringsMatchingLastWord(args, idStrs);
        }
        return Collections.emptyList();
    }
}
