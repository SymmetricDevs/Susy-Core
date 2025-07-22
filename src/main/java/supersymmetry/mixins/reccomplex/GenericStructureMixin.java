package supersymmetry.mixins.reccomplex;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.storage.loot.LootGenerationHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.common.metatileentities.storage.MetaTileEntityLockedCrate;



@Mixin(targets = "ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure", remap = false)
public class GenericStructureMixin {

    @Inject(method = "generateTileEntityContents", at = @At("HEAD"), cancellable = true)
    private static void onGenerateTileEntityContents(StructureSpawnContext context, TileEntity tileEntity, CallbackInfo ci) {
        if (!context.generateAsSource && tileEntity instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (mte instanceof MetaTileEntityLockedCrate) {
                IItemHandlerModifiable realInventory = ((MetaTileEntityLockedCrate) mte).getInventoryForLootGen();
                LootGenerationHandler.generateAllTags(context, realInventory);
                ci.cancel(); // prevent RecComplex from running its normal (getCapability-based) logic
            }
        }
    }
}
