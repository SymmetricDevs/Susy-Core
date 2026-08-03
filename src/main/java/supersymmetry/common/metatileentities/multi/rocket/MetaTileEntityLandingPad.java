package supersymmetry.common.metatileentities.multi.rocket;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.multiblock.IRedstoneControllable;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityLander;

public class MetaTileEntityLandingPad extends MultiblockWithDisplayBase implements IRedstoneControllable {

    private AxisAlignedBB landingAreaBB;
    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IEnergyContainer energyContainer;
    private List<Runnable> signalActions = new ArrayList<>();
    protected boolean extractItems;

    public MetaTileEntityLandingPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        signalActions.add(this::toggleExtractItems);
        signalActions.add(this::launchLander);
        signalActions.add(this::destroyLander);
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
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputInventory = null;
        this.outputInventory = null;
        this.energyContainer = null;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addEnergyUsageExactLine(VA[LV])
                .addLowPowerLine(energyContainer.getEnergyStored() < VA[LV]);

        textList.add(new TextComponentTranslation("susy.landing_pad." + (extractItems ? "extracting" : "inserting")));
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return new ImageCycleButtonWidget(x, y, width, height, SusyGuiTextures.BUTTON_INSERT_EXTRACT, 2,
                () -> this.extractItems ? 0 : 1,
                this::setExtractItems)
                        .setTooltipHoverString(mode -> mode == 0 ? "susy.landing_pad.extracting" :
                                "susy.landing_pad.inserting");
    }

    @Override
    protected void updateFormedValid() {
        EntityLander lander = getLander();
        if (lander != null && energyContainer.changeEnergy(-VA[LV]) == -VA[LV]) {
            if (extractItems) {
                if (!lander.isEmpty()) {
                    GTTransferUtils.moveInventoryItems(lander.getInventory(), this.outputInventory);
                    lander.markDirty();
                }
            } else {
                GTTransferUtils.moveInventoryItems(this.inputInventory, lander.getInventory());
                lander.getInventory();
            }
            if (this.isBlockRedstonePowered() && !lander.isCountdownStarted()) {
                lander.startCountdown(20);
            }
        }
    }

    protected void launchLander() {
        EntityLander lander = getLander();
        if (lander != null && !getLander().isCountdownStarted()) {
            lander.startCountdown(20);
        }
    }

    protected void destroyLander() {
        EntityLander lander = getLander();
        if (lander != null && !getLander().isEmpty()) {
            lander.setDead();
        }
    }

    protected void toggleExtractItems() {
        setExtractItems(this.extractItems ? 1 : 0); // flip
    }

    protected void setExtractItems(int mode) {
        this.extractItems = mode == 0;
        // Send update to client with writeCustomData
        this.writeCustomData(SuSyDataCodes.UPDATE_INSERT_EXTRACT, (buffer) -> {
            buffer.writeBoolean(this.extractItems);
        });
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SuSyDataCodes.UPDATE_INSERT_EXTRACT) {
            this.extractItems = buf.readBoolean();
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
                .where('C',
                        states(getCasingState()).setMinGlobalLimited(6)
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS,
                                        MultiblockAbility.INPUT_ENERGY))
                                .or(autoAbilities()))
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
        BlockPos corner2 = padCenter.offset(right, 7).offset(facing.getOpposite(), 6).offset(EnumFacing.UP, 4);

        // Create the bounding box
        this.landingAreaBB = new AxisAlignedBB(corner1, corner2);
    }

    @Override
    public int getSignalCeiling() {
        return 3;
    }

    @Override
    public void pulse(int sig) {
        this.signalActions.get(sig).run();
    }
}
