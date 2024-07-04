package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.fluids.FluidState;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.multi.VoidingMultiblockBase;

import javax.annotation.Nonnull;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySmokeStack extends VoidingMultiblockBase {
    // Storing this, just in case it is ever needed
    private int height = 5;

    public MetaTileEntitySmokeStack(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySmokeStack(this.metaTileEntityId);
    }

    @Override
    public boolean canVoidState(FluidState state) {
        return state == FluidState.GAS;
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
        this.updateHeight();
    }

    // Update the height when rotating the multiblock
    @Override
    public void setUpwardsFacing(EnumFacing upwardsFacing) {
        super.setUpwardsFacing(upwardsFacing);
        this.updateHeight();
    }

    public void updateHeight() {
        World world = getWorld();
        if (world == null) { // JEI previews
            return;
        }
        // Minimum height
        int height = 5;
        // One block below the minimum height

        EnumFacing relativeUp = UP.getRelativeFacing(this.getFrontFacing(), this.getUpwardsFacing(), this.isFlipped());

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(getPos().offset(relativeUp, height - 2));
        for ( ; height < 10 ; height++ ) {
            if(isBlockMuffler(world, pos.move(relativeUp))) break;
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
    public boolean isBlockMuffler(World world, @NotNull BlockPos pos) {
        if (world == null) { // JEI previews
            return true;
        }
        if (world.getTileEntity(pos) instanceof IGregTechTileEntity gtTe) {
            MetaTileEntity mte = gtTe.getMetaTileEntity();
            return mte instanceof MetaTileEntityMufflerHatch;
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if(dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.height = buf.readInt();
            this.rateBonus = buf.readInt();
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
            ITextComponent componentHeight = TextComponentUtil.stringWithColor(TextFormatting.BLUE,
                    String.valueOf(this.height));
            ITextComponent componentRateBonus = TextComponentUtil.stringWithColor(TextFormatting.DARK_PURPLE,
                    this.rateBonus + "x");
            ITextComponent componentRateBase = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.machine.smoke_stack.rate",
                    componentRateBonus);
            ITextComponent componentRateHover = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.machine.smoke_stack.rate_hover");

            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.machine.smoke_stack.height",
                    componentHeight));
            textList.add(TextComponentUtil.setHover(componentRateBase, componentRateHover));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.smoke_stack.tooltip.1", getBaseVoidingRate()));
        tooltip.add(I18n.format("gregtech.machine.smoke_stack.tooltip.2"));
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
}
