package supersymmetry.common.tileentities;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;

import static supersymmetry.api.util.SuSyUtility.susyId;

public class SuSyTileEntities {

    public static void register() {
        registerTileEntity(TileEntityEccentricRoll.class, "eccentric_roll");
    }

    private static void registerTileEntity(@NotNull Class<? extends TileEntity> teClazz, @NotNull String name) {
        GameRegistry.registerTileEntity(teClazz, susyId(name));
    }
}
