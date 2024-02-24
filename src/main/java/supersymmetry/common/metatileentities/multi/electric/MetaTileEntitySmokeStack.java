package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.fluids.GTFluid;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMufflerHatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySmokeStack extends MultiblockWithDisplayBase {

    // Storing this, just in case it is ever needed
    private int height = 5;
    // Multiplies fluid voiding rate, based on structure height
    private int rateBonus = 1;
    // Amount of ticks between voiding
    private int voidingFrequency = 10;

    private boolean active = false;
    public MetaTileEntitySmokeStack(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySmokeStack(this.metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if(this.getWorld().isRemote) return;
        if(getOffsetTimer() % voidingFrequency == 0) {
            this.active = false;
            for (IFluidTank tank:
                    getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
                FluidStack fs = tank.getFluid();
                if(fs != null) {
                    Fluid fluid = fs.getFluid();
                    if(fluid instanceof GTFluid.GTMaterialFluid gtFluid) {
                        Material mat = gtFluid.getMaterial();
                        // Anything that is gaseous (ignoring density) and not flammable may be smoke stacked
                        //TODO: Cache this?
                        if(gtFluid.isGaseous() && !mat.hasFlag(MaterialFlags.FLAMMABLE)) {
                            tank.drain(this.getActualVoidingRate(), true);
                            this.active = true;
                        }
                    }
                }
            }
        }

    }

    protected BlockPattern createStructurePattern() {
        // May want to force the input to be underneath the pipe casings
        return FactoryBlockPattern.start(FRONT, RIGHT, UP)
                .aisle("S")
                .aisle("P").setRepeatable(3,7)
                .aisle("F")
                .where('S', this.selfPredicate())
                .where('P', states(this.getPipeCasingState())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1)))
                .where('F', abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1))
                .build();
    }

    // Updates the height and rate of the multiblock
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        World world = getWorld();
        // Minimum height
        int height = 5;
        // One block below the minimum height
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(getPos().up(height - 2));
        for ( ; height < 10 ; height++ ) {
            if(isBlockMuffler(world, pos.move(EnumFacing.UP))) break;
        }

        this.height = height;
        // Arbitrary base, minimum structure height is five blocks
        this.rateBonus = (int) Math.pow(2, height - 5);

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.height);
            buf.writeInt(this.rateBonus);
        });
    }

    // For determining the multiblocks height
    public boolean isBlockMuffler(@NotNull World world, @NotNull BlockPos pos) {
        if (world.getTileEntity(pos) instanceof IGregTechTileEntity gtTe) {
            MetaTileEntity mte = gtTe.getMetaTileEntity();
            return mte instanceof MetaTileEntityMufflerHatch;
        }
        return false;
    }

    // In liters
    public int getActualVoidingRate() {
        return 1000 * rateBonus;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isActive(), true);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if(dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.height = buf.readInt();
            this.rateBonus = buf.readInt();
        }

        if(dataId == GregtechDataCodes.IS_WORKING) {
            this.active = this.lastActive;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("height", this.height);
        data.setInteger("rateBonus", this.rateBonus);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.height = data.hasKey("height") ? data.getInteger("height") : this.height;
        this.rateBonus = data.hasKey("rateBonus") ? data.getInteger("rateBonus") : this.rateBonus;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(height);
        buf.writeInt(rateBonus);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.height = buf.readInt();
        this.rateBonus = buf.readInt();
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if(isStructureFormed()) {
            ITextComponent componentHeight = TextComponentUtil.stringWithColor(TextFormatting.DARK_BLUE,
                    String.valueOf(this.height));
            ITextComponent componentRateBonus = TextComponentUtil.stringWithColor(TextFormatting.DARK_PURPLE,
                    String.valueOf(this.rateBonus));

            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.machine.smoke_stack.height_and_rate",
                    componentHeight,
                    componentRateBonus));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.smoke_stack.tooltip.1"));
    }


    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }
    protected IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.SMOKE_STACK_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
