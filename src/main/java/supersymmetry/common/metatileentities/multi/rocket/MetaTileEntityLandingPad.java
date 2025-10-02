package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.entities.EntityLander;

public class MetaTileEntityLandingPad extends MultiblockWithDisplayBase {

    private AxisAlignedBB landingAreaBB;
    protected IItemHandlerModifiable outputInventory;

    public MetaTileEntityLandingPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLandingPad(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
        setStructureAABB();
    }

    protected void initializeAbilities() {
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
    }

    @Override
    protected void updateFormedValid() {
        EntityLander lander = getLander();
        if (lander != null && !lander.isEmpty()) {
            GTTransferUtils.moveInventoryItems(lander.getInventory(), this.outputInventory);
            if (this.isBlockRedstonePowered()) {
                lander.setHasTakenOff(true);
            }
        }
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    protected static IBlockState getPadState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.HEAVY_DUTY_PAD);
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public TraceabilityPredicate getAbilityPredicate() {
        TraceabilityPredicate predicate = super.autoAbilities(true, false);
        predicate.or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)
                .setPreviewCount(1));
        predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
        predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isActive(), true);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("     CCCCC     ", "      CCC      ", "      CCC      ")
                .aisle("   CCPPPPPCC   ", "     PPPPP     ", "     AAAAA     ")
                .aisle("  CPPPPPPPPPC  ", "   PPPPPPPPP   ", "   AAAAAAAAA   ")
                .aisle(" CPPPPPPPPPPPC ", "  PPPPPPPPPPP  ", "  AAAAAAAAAAA  ")
                .aisle(" CPPPPPPPPPPPC ", "  PPPPPPPPPPP  ", "  AAAAAAAAAAA  ")
                .aisle("CPPPPPPPPPPPPPC", " PPPPPPPPPPPPP ", " AAAAAAAAAAAAA ")
                .aisle("CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", " PPPPPPPPPPPPP ", " AAAAAAAAAAAAA ")
                .aisle(" CPPPPPPPPPPPC ", "  PPPPPPPPPPP  ", "  AAAAAAAAAAA  ")
                .aisle(" CPPPPPPPPPPPC ", "  PPPPPPPPPPP  ", "  AAAAAAAAAAA  ")
                .aisle("  CPPPPPPPPPC  ", "   PPPPPPPPP   ", "   AAAAAAAAA   ")
                .aisle("   CCPPPPPCC   ", "     PPPPP     ", "     AAAAA     ")
                .aisle("     CCSCC     ", "      CCC      ", "      CCC      ")
                .where(' ', any())
                .where('A', air())
                .where('S', selfPredicate())
                .where('C', states(getCasingState()).setMinGlobalLimited(6).or(getAbilityPredicate()))
                .where('P', states(getPadState()))
                .build();
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    public EntityLander getLander() {
        for (EntityLander entity : this.getWorld().getEntitiesWithinAABB(EntityLander.class,
                this.landingAreaBB)) {
            if (entity.onGround) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    public void setStructureAABB() {
        EnumFacing facing = this.getFrontFacing();
        BlockPos controllerPos = this.getPos();

        BlockPos padCenter = controllerPos.offset(facing.getOpposite(), 7);

        EnumFacing right = facing.rotateY();
        EnumFacing left = facing.rotateYCCW();

        // Only accept the Y layer directly on top of the landing pad
        BlockPos corner1 = padCenter.offset(left, 7).offset(facing, 6).offset(EnumFacing.UP, 1);
        BlockPos corner2 = padCenter.offset(right, 7).offset(facing.getOpposite(), 6).offset(EnumFacing.UP, 2);

        // Create the bounding box
        this.landingAreaBB = new AxisAlignedBB(corner1, corner2);
    }
}
