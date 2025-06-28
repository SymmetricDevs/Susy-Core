package supersymmetry.common.metatileentities.multi.rocket;

import com.google.common.collect.Lists;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityRocketAssembler extends MultiblockWithDisplayBase implements IDataInfoProvider, IControllable { //TODO: IControllable? IWorkable?

    protected IItemHandlerModifiable inputInventory;

    //TODO: How are OCs handled?
    protected IEnergyContainer energyContainer;

    private boolean workingEnabled = true;

    public MetaTileEntityRocketAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        resetTileAbilities();
    }

    @Override
    protected void updateFormedValid() {
        //TODO: find out what to do here
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("                                                               ", "                                                               ", "                                                               ", "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ", " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCIIISIIICCCCCCCCCCCCCCCCCCCCCCCCCCCC", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ", "                                                               ")
                .where(' ', any())
                .where('S', selfPredicate())
                .where('F', states(SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION)))
                .where('C', states(SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION)))
                .where('R', SuSyPredicates.rails())
                .where('P', states(SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.STRUCTURAL_FRAME)))
                .where('B', states(SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.RAILS)))
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS).or(states(SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION))))
                .build();
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
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRocketAssembler(metaTileEntityId);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    //TODO: inventory handling

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@NotNull IMultiblockPart part) {
        return true;
    }

    private void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();

    }


    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        //TODO
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        //TODO
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();

        return list;
    }


    @Override
    public boolean isActive() {
        return super.isActive(); //TODO
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);
        builder.image(4, 4, 190, 109, GuiTextures.DISPLAY);

        /*TODO: Progress bar? hmmm?
        ProgressWidget progressBar = new ProgressWidget(
                scannerLogic::getProgressPercent,
                4, 115, 190, 7,
                GuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> addBarHoverText(list, 0));
        builder.widget(progressBar);
        */



        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(new ImageCycleButtonWidget(173, 183, 18, 18, GuiTextures.BUTTON_POWER,
                    controllable::isWorkingEnabled, controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF)
                .widget(new ClickButtonWidget(68,56,54,18,new TextComponentTranslation("gregtech.machine.rocket_assembler.start_assembling").getUnformattedComponentText(),click -> assembleRocket()))
                .widget(new IndicatorImageWidget(174, 93, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
                        .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
                        .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));

        return builder;
    }

    private void assembleRocket(){
        if(!workingEnabled) return;

        //Debug code -> remove for build
        for (int slot = 0; slot < inputInventory.getSlots(); slot++) {
            ItemStack stack = inputInventory.getStackInSlot(slot);
            System.out.println(stack.getTagCompound().toString());
        }

        //TODO: check if all necessary things are present (Data cards + Materials)

        //TODO: blah blah more assembling and whatever
    }

    //TODO redstone signal handling (Redstone part multipart?)


    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        workingEnabled = isWorkingAllowed;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        //TODO
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        //TODO
        super.readFromNBT(data);
    }
}
