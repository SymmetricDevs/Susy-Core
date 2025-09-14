package supersymmetry.common.metatileentities.multi.electric;


import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityReverberatoryFurnace extends RecipeMapMultiblockController {

    public MetaTileEntityReverberatoryFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.REVERBERATORY_FURNACE);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityReverberatoryFurnace(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX", "YYY")
                .aisle("XXX", "X#X", "X#X", "Y#Y")
                .aisle("XXX", "X#X", "X#X", "YYY")
                .aisle("XXX", "X#X", "X#X", "YYY")
                .aisle("XXX", "X#X", "X#X", "YYY")
                .aisle("XXX", "X#X", "X#X", "YYY")
                .aisle("XXX", "XSX", "XXX", "YYY")
                .where('Y', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS)))
                .where('X', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                        .or(autoAbilities(false, false, true, true, true, true, false))
                        .or(abilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS).setPreviewCount(0))
                        .or(abilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS).setPreviewCount(0)))
                .where('#', air())
                .where('S', selfPredicate())
                .build();
    }

    @Override
    protected void initializeAbilities() {
        List<IItemHandlerModifiable> imports = new ArrayList<>();
        imports.addAll(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS));
        imports.addAll(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputInventory = new ItemHandlerList(imports); // Please remember to use inputInventory for all things to do with recipe logic.

        List<IItemHandlerModifiable> exports = new ArrayList<>();
        exports.addAll(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS));
        exports.addAll(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputInventory = new ItemHandlerList(exports);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PRIMITIVE_BRICKS;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public void update() {
        super.update();

        if (this.isActive()) {
            if (getWorld().isRemote) {
                BlockPos pos = this.getPos();
                EnumFacing facing = this.getFrontFacing().getOpposite();
                float xPos = facing.getXOffset() * 5F + pos.getX() + 0.5F;
                float yPos = facing.getYOffset() * 0.76F + pos.getY() + 0.25F;
                float zPos = facing.getZOffset() * 5F + pos.getZ() + 0.5F;

                float ySpd = facing.getYOffset() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
                runMufflerEffect(xPos, yPos, zPos, 0, ySpd, 0);
            }
        }
    }

    private void pollutionParticles() {

    }

    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            final BlockPos pos = getPos();
            float x = pos.getX() + 0.5F;
            float z = pos.getZ() + 0.5F;

            final EnumFacing facing = getFrontFacing();
            final float horizontalOffset = GTValues.RNG.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + GTValues.RNG.nextFloat() * 0.375F + 0.3F;

            if (facing.getAxis() == EnumFacing.Axis.X) {
                if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == EnumFacing.Axis.Z) {
                if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                getWorld().playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
            getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0, 0, 0);
            getWorld().spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }
}
