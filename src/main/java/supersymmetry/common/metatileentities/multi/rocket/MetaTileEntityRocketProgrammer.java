package supersymmetry.common.metatileentities.multi.rocket;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import cam72cam.mod.entity.ModdedEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.common.entities.EntityTransporterErector;

public class MetaTileEntityRocketProgrammer extends MultiblockWithDisplayBase {

    protected IItemHandlerModifiable circuitHolder = new ItemStackHandler(1);
    protected AxisAlignedBB structureAABB;

    public MetaTileEntityRocketProgrammer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRocketProgrammer(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.setStructureAABB();
    }

    @Override
    protected void updateFormedValid() {
        if (this.getOffsetTimer() % 10 == 0 && this.getConfig() != null) {
            EntityTransporterErector rocket = searchRocket();
            if (rocket != null) {
                rocket.getRocketNBT().setTag("config", this.getConfig());
            }
        }
    }

    private EntityTransporterErector searchRocket() {
        List<ModdedEntity> trains = getWorld().getEntitiesWithinAABB(ModdedEntity.class, this.structureAABB);

        if (!trains.isEmpty()) {
            for (ModdedEntity forgeTrainEntity : trains) {
                if (forgeTrainEntity.getSelf() instanceof EntityTransporterErector rollingStock &&
                        rollingStock.isRocketLoaded()) {
                    return rollingStock;
                }
            }
        }
        return null;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.DOWN, RelativeDirection.FRONT)
                .aisle("     CCECC     ",
                        "   CCC   CCC   ",
                        "  CC       CC  ",
                        " CC         CC ",
                        " C           C ",
                        "CC           CC",
                        "C             C",
                        "S             E",
                        "C             C",
                        "CCCCCCCCCCCCCCC")
                .where(' ', any())
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('S', selfPredicate())
                .where('E',
                        states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                                .or(abilities(MultiblockAbility.INPUT_ENERGY)))
                .build();
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        itemBuffer.add(circuitHolder.getStackInSlot(0));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    public void setStructureAABB() {
        BlockPos left = this.getPos().offset(getRelativeFacing(RelativeDirection.RIGHT));
        BlockPos right = this.getPos().offset(getRelativeFacing(RelativeDirection.RIGHT), 13).offset(EnumFacing.UP);

        this.structureAABB = new AxisAlignedBB(left, right);
    }

    protected EnumFacing getRelativeFacing(RelativeDirection dir) {
        return dir.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    public NBTTagCompound getConfig() {
        if (!circuitHolder.getStackInSlot(0).isEmpty()) {
            return circuitHolder.getStackInSlot(0).getTagCompound();
        }
        return null;
    }

    public void writeConfigItemToNBT(NBTTagCompound tag) {
        if (!circuitHolder.getStackInSlot(0).isEmpty()) {
            NBTTagCompound item = new NBTTagCompound();
            circuitHolder.getStackInSlot(0).writeToNBT(item);
            tag.setTag("config", item);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        writeConfigItemToNBT(data);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("config")) {
            this.circuitHolder.setStackInSlot(0, new ItemStack(data.getCompoundTag("config")));
        }
        reinitializeStructurePattern();
    }
}
