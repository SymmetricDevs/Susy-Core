package supersymmetry.common.command;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

public class CommandHordeBase extends CommandTreeBase {

    @NotNull
    @Override
    public String getName() {
        return "mobHorde";
    }

    @NotNull
    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("horde", "invasion");
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "susy.command.horde.usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender,
                        String[] args) throws CommandException {
        super.execute(server, sender, args);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
