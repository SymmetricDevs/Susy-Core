package supersymmetry.common.metatileentities.multi.rocket;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class MetaTileEntityLandingPad extends MultiblockWithDisplayBase {

    private Queue<LandingData> incoming;
    private LandingData current;

    public MetaTileEntityLandingPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLandingPad(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (this.incoming.peek().scheduledTotalWorldTime > this.getWorld().getTotalWorldTime()) {

        }
        if (this.getWorld().isRemote) {

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
        predicate.or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1));
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
                .aisle("     CCCCC     ", "      CCC      ")
                .aisle("   CCPPPPPCC   ", "     AAAAA     ")
                .aisle("  CPPPPPPPPPC  ", "   AAAAAAAAA   ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle("CPPPPPPPPPPPPPC", " AAAAAAAAAAAAA ")
                .aisle("CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", " AAAAAAAAAAAAA ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle("  CPPPPPPPPPC  ", "   AAAAAAAAA   ")
                .aisle("   CCPPPPPCC   ", "     AAAAA     ")
                .aisle("     CCSCC     ", "      CCC      ")
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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        int i = 0;
        for (LandingData satellite : incoming) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("scheduledTotalWorldTime", satellite.scheduledTotalWorldTime);
            int j = 0;
            for (ItemStack stack : satellite.recipe) {
                tag.setInteger("item" + j, stack.getCount());
                j++;
            }
            data.setTag("incoming" + i, tag);
        }
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        incoming = new ArrayDeque<>();
        int i = 0;
        while (data.hasKey("incoming" + i, 10)) {
            NBTTagCompound tag = data.getCompoundTag("incoming" + i);
            LandingData landingData = new LandingData();
            landingData.scheduledTotalWorldTime = tag.getInteger("scheduledTotalWorldTime");
            int j = 0;
            for (ItemStack stack : landingData.recipe) {
                stack.setCount(tag.getInteger("item" + j));
                j++;
            }
            incoming.add(landingData);
            i++;
        }
    }

    private static class LandingData {
        private List<ItemStack> recipe;
        private int scheduledTotalWorldTime;
    }
}
