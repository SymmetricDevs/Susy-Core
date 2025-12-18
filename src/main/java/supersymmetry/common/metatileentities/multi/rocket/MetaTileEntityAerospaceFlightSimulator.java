package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.world.DummyWorld;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.mui.widget.RocketRenderWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;

public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase
                                                    implements IWorkable {

    public static Entity createEntityByResource(ResourceLocation rl, World world) {
        // 1. Get the entry from the registry
        var entry = ForgeRegistries.ENTITIES.getValue(rl);
        if (entry == null) {
            throw new IllegalArgumentException("No entity registered under " + rl);
        }

        // 2. Create an instance
        // The constructor used here is the standard (World) one.
        try {
            return entry.newInstance(world);
        } catch (Exception e) {
            // The constructor threw something – wrap it nicely
            throw new RuntimeException(
                    "Failed to instantiate entity " + rl + " with constructor (World)", e);
        }
    }

    private IEnergyContainer energyContainer;
    private boolean isActive = false;
    private boolean isWorkingEnabled = true;
    protected boolean hasNotEnoughEnergy;

    private int progress = 0;

    public DataStorageLoader rocketBlueprintSlot = new DataStorageLoader(
            this,
            item -> SuSyMetaItems.isMetaItem(item) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue &&
                    item.getTagCompound() != null && item.getTagCompound().getBoolean("buildstat"));

    public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public int getMaxProgress() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.progress = 0;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(
                MetalCasingType.STEEL_SOLID); // replace with real values later pls
    }

    public IBlockState getComputerState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
    }

    protected int getEnergyToConsume() {
        return 1000;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.progress = 0;
    }

    @Override
    protected void updateFormedValid() {
        int energyToConsume = this.getEnergyToConsume();
        boolean hasMaintenance = ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics();
        if (hasMaintenance) {
            energyToConsume += getNumMaintenanceProblems() * energyToConsume / 10;
        }

        if (this.hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyToConsume) {
            this.hasNotEnoughEnergy = false;
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!hasNotEnoughEnergy) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == -energyToConsume) {
                    setActive(true);
                } else {
                    this.hasNotEnoughEnergy = true;
                    setActive(false);
                }
            }
        } else {
            this.hasNotEnoughEnergy = true;
            setActive(false);
        }
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCSMC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .where('M', maintenancePredicate())
                .where('S', selfPredicate())
                .where('A', air())
                .where('C', states(getCasingState()))
                .where('P', states(getComputerState()))
                .build();
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        int width = 198;
        int height = 208;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.image(4, 4, 190, 117, GuiTextures.DISPLAY);
        builder.widget(
                new IndicatorImageWidget(174, 101, 17, 17, getLogo())
                        .setWarningStatus(getWarningLogo(), this::addWarningText)
                        .setErrorStatus(getErrorLogo(), this::addErrorText));

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(
                new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                        .setMaxWidthLimit(181)
                        .setClickHandler(this::handleDisplayClick));

        // Power Button
        // todo in the future, refactor so that this class is instanceof IControllable.
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(
                    new ImageCycleButtonWidget(
                            173,
                            183,
                            18,
                            18,
                            GuiTextures.BUTTON_POWER,
                            controllable::isWorkingEnabled,
                            controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }
        builder.widget(getFlexButton(173, 125, 18, 18));

        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        var bpw = new SlotWidgetMentallyStable(this.rocketBlueprintSlot, 0, height / 2, width / 2);
        bpw.setBackgroundTexture(GuiTextures.SLOT_DARK);
        builder.widget(bpw);

        if (!this.rocketBlueprintSlot.isEmpty() && this.rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
            var tag = this.rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
            var bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
            if (bp != null && bp.readFromNBT(tag) && bp.isFullBlueprint()) {

                ResourceLocation entity_res = bp.relatedEntity;
                DummyWorld world = new DummyWorld();
                // var world = this.getWorld();
                Entity rocketentity = MetaTileEntityAerospaceFlightSimulator.createEntityByResource(entity_res, world);
                rocketentity.setPosition(0, 0, 0);

                builder.widget(new RocketRenderWidget(new Size(200, 150), new Position(0, 0), rocketentity));
            }
        }

        return builder;
    }
}
