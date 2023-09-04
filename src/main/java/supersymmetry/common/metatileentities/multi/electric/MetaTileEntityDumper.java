package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;

public class MetaTileEntityDumper extends RecipeMapMultiblockController {
    public MetaTileEntityDumper(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.DUMPING);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDumper(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("A  A", "BBBB", "A  A")
                .aisle("BBBB", "C##A", "BBBB")
                .aisle("A  A", "BSBB", "A  A")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('B', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1)))
                .where('C', abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1))
                .where('D', abilities(MultiblockAbility.EXPORT_FLUIDS).setExactLimit(1))
                .where(' ', any())
                .where('#', air())
                .build();
    }
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public void update() {
        super.update();

        if (this.isActive()) {
            if (getWorld().isRemote) {
                dumpingParticles();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void dumpingParticles() {
        BlockPos pos = this.getPos();
        EnumFacing facing = this.getFrontFacing().getOpposite();
        float xPos = pos.getX() + (1.5F * facing.getXOffset()) + (3F * -facing.getZOffset());
        float yPos = pos.getY();
        float zPos = pos.getZ() + (3F * facing.getXOffset()) + (1.5F * facing.getZOffset());

        float ySpd = 0F;
        float xSpd = facing.getZOffset() * 1F;
        float zSpd = -facing.getXOffset() * 1F;

        getWorld().spawnParticle(EnumParticleTypes.WATER_DROP, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.DUMPER_OVERLAY;
    }
}
