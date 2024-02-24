package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.fluids.GTFluid;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.fluids.SusyFluidStorageKeys;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityDumper extends MultiblockWithDisplayBase {
    // Multiplies fluid voiding rate, hardcoded for now
    private final int rateBonus = 1;
    // Amount of ticks between voiding
    private int voidingFrequency = 10;

    private boolean active = false;
    public MetaTileEntityDumper(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDumper(this.metaTileEntityId);
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
                        FluidStorageKey key = mat.getProperty(PropertyKey.FLUID).getPrimaryKey();
                        // Anything that is a liquid and not flammable may be dumped
                        boolean dumpeable = key.equals(FluidStorageKeys.LIQUID)
                                || key.equals(SusyFluidStorageKeys.IMPURE_SLURRY)
                                || key.equals(SusyFluidStorageKeys.SLURRY);
                        //TODO: Cache this?
                        if(dumpeable && !mat.hasFlag(MaterialFlags.FLAMMABLE)) {
                            tank.drain(this.getActualVoidingRate(), true);
                            this.active = true;
                        }
                    }
                }
            }
        }
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
                .where(' ', any())
                .where('#', air())
                .build();
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

    // In liters
    public int getActualVoidingRate() {
        return 16000 * rateBonus;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(active);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.active = buf.readBoolean();
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if(isStructureFormed()) {
            ITextComponent componentRate = TextComponentUtil.stringWithColor(TextFormatting.DARK_PURPLE,
                    String.valueOf(this.getActualVoidingRate()));

            textList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.machine.dumper.rate",
                    componentRate));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.dumper.tooltip.1", getActualVoidingRate()));
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

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.DUMPER_OVERLAY;
    }
}
