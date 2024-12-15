package supersymmetry.mixins.yungs;

import com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces.MineshaftPiece;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces.ZombieVillagerRoom;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ZombieVillagerRoom.class)
public abstract class MixinZombieVillagerRoom extends MineshaftPiece {

    @Redirect(method = "addComponentParts", at = @At(value = "INVOKE", target = "Lcom/yungnickyoung/minecraft/bettermineshafts/world/generator/pieces/ZombieVillagerRoom;setBlockState(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;IIILnet/minecraft/world/gen/structure/StructureBoundingBox;)V"))
    private void susycore$removeFurnace(ZombieVillagerRoom instance, World world, IBlockState iBlockState, int i1, int i2, int i3, StructureBoundingBox structureBoundingBox) {
        if (iBlockState.getBlock() instanceof BlockFurnace) {
            setBlockState(world, AIR, i1, i2, i3, structureBoundingBox);
        }
    }

}
