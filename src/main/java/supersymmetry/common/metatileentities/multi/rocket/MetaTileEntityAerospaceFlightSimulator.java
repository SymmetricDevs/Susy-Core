package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.mui.widget.RocketStageDisplayWidget;
import supersymmetry.common.mui.widget.SlotWidgetBlueprintContainer;
import supersymmetry.common.rocketry.SusyRocketComponents;

public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase {
  // live widemann reaction
  // https://discord.com/channels/881234100504109166/881234101103890454/1402784628603097270
  public Map<String, Map<String, List<DataStorageLoader>>> slots = new HashMap<>();
  public DataStorageLoader rocketBlueprintSlot =
      new DataStorageLoader(
          this,
          item -> {
            return SuSyMetaItems.isMetaItem(item)
                == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue;
          });

  public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
    super(metaTileEntityId);
  }

  @Override
  protected void updateFormedValid() {}

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
  protected ModularUI createUI(EntityPlayer entityPlayer) {
    return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
  }

  @Override
  public ModularUI getModularUI(EntityPlayer entityPlayer) {

    return null; // createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
  }

  private boolean slotsEmpty() {
    return slots.values().stream()
        .flatMap(m -> m.values().stream())
        .flatMap(List::stream)
        .allMatch(DataStorageLoader::isEmpty);
  }

  private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
    int width = 300;
    int height = 280;

    ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
    // black display thing in the background
    builder.image(4, 4, width - 8, height - 8, GuiTextures.DISPLAY);

    builder.dynamicLabel(
        width / 2,
        height / 2 - 16,
        () -> {
          return rocketBlueprintSlot.isEmpty()
              ? I18n.format(this.getMetaName() + ".blueprint_request")
              : "";
        },
        0x404040);
    builder.widget(
        new IndicatorImageWidget(width - 23, height - 23, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
            .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
            .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));

    builder.widget(
        new ClickButtonWidget(
            width - 40,
            height - 130,
            40,
            30,
            new TextComponentTranslation("debug").getUnformattedComponentText(),
            this::SetDefaultBlueprint));

    builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
    builder.bindPlayerInventory(entityPlayer.inventory, height - 80);
    // this is the thing that displays slots for components
    RocketStageDisplayWidget mainWindow =
        new RocketStageDisplayWidget(
            new Position(9, 20),
            new Size(width - 20, 28 * 4),
            (stage, name) -> {
              if (!slots.containsKey(stage.getName())) {
                throw new IllegalStateException("missing the key value for a stage");
              }
              if (!slots.get(stage.getName()).containsKey(name)) {
                throw new IllegalStateException("missing the key value for a component");
              }
              return this.slots.get(stage.getName()).get(name);
            });
    mainWindow.insertionAction =
        (window) -> {
          this.onBlueprintInserted(window);
        };
    mainWindow.removalAction =
        (window) -> {
          this.onBlueprintRemoved(window);
        };
    mainWindow.setVisible(false);
    mainWindow.setActive(false);
    SlotWidgetBlueprintContainer blueprintContainer =
        new SlotWidgetBlueprintContainer(
            rocketBlueprintSlot,
            0,
            width / 2,
            height / 2,
            // this is called on SlotChanged
            () -> {},
            // moved it a bit further down because it wont let me do stupid things unless
            // i edit it after the constructor is done
            //
            // this is called on detectAndSendChanges
            () -> {
              rocketBlueprintSlot.setLocked(!slotsEmpty());
              // lock the main blueprint if component data cards are
              // inserted so that they dont get
              // voided (hopefully)
            });

    ClickButtonWidget buildButton =
        new ClickButtonWidget(
            0,
            height - 130,
            40,
            30,
            new TextComponentTranslation(this.getMetaName() + ".build_button_label")
                .getFormattedText(),
            (c) -> {
              this.buildBlueprint(c, mainWindow);
            });
    builder.dynamicLabel(
        width / 5,
        height / 5,
        () -> {
          return mainWindow.getStatusText();
        },
        0x505050);
    // this ChangeListener thing is a mess, it gets called mostly correctly server side (although it
    // still gets called when you just click on the slot with nothing happenning), but on the client
    // it gets called on initialization, and every single time the server syncs anything in the ui
    // to the client
    // for that reason i've decided to stop trying to make this random number generator work
    // correctly on both sides and just used updateInfo :goog:?
    blueprintContainer.setChangeListener(
        () -> {
          blueprintContainer.setSelfPosition(
              rocketBlueprintSlot.isEmpty()
                  ? new Position(width / 2, height / 2)
                  : new Position(width - 40, height - 40));

          // the part that actually matters should be server side
          if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            SusyLog.logger.info("blueprint item: {}", blueprintContainer.getHandle().getStack());
            if (this.rocketBlueprintSlot.isEmpty()) {
              // this.onBlueprintRemoved(mainWindow);
              mainWindow.onBlueprintRemoved();

            } else {
              if (this.rocketBlueprintSlot.getStackInSlot(0).getMetadata()
                  == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue) {
                // this.onBlueprintInserted(mainWindow);
                mainWindow.onBlueprintInserted();
              }
            }
          }
        });
    // initialize the thing in case the blueprint was already in the machine
    if (!this.rocketBlueprintSlot.isEmpty()) {

      NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
      AbstractRocketBlueprint bp =
          AbstractRocketBlueprint.getBlueprintsRegistry().get(tag.getString("name"));
      if (bp.readFromNBT(tag)) {
        mainWindow.generateFromBlueprint(bp);
        mainWindow.setActive(true);
        mainWindow.setVisible(true);
      } else {
        SusyLog.logger.error("invalid blueprint");
      }
    }

    builder.dynamicLabel(width / 2, (int) (height * 0.66), mainWindow::getStatusText, 0x505050);

    builder.widget(mainWindow);
    builder.widget(blueprintContainer.setBackgroundTexture(GuiTextures.SLOT_DARK));
    builder.widget(buildButton);

    return builder;
  }

  public void onBlueprintRemoved(RocketStageDisplayWidget mainwindow) {
    // if (this.slots.isEmpty() )
    this.slots.clear();

    mainwindow.selectedStageIndex = 0;
    mainwindow.error = RocketStage.ComponentValidationResult.UNKNOWN;
    mainwindow.setVisible(false);
    mainwindow.setActive(false);
  }

  // this should only be ran when a new blueprint item is inserted, as doing this will void items
  public void onBlueprintInserted(RocketStageDisplayWidget mainwindow) {
    mainwindow.setActive(true);
    mainwindow.setVisible(true);
    if (!rocketBlueprintSlot.isEmpty() && rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
      NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
      AbstractRocketBlueprint bp =
          AbstractRocketBlueprint.getBlueprintsRegistry().get(tag.getString("name"));
      if (bp.readFromNBT(tag)) {
        this.slots = mainwindow.generateSlotsFromBlueprint(bp, this);
        mainwindow.generateFromBlueprint(bp);
      } else {
        SusyLog.logger.error("invalid blueprint");
      }
    }
  }

  public void buildBlueprint(ClickData clickdata, RocketStageDisplayWidget mainwindow) {
    if (!rocketBlueprintSlot.isEmpty() && rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
      NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
      AbstractRocketBlueprint bp =
          AbstractRocketBlueprint.getBlueprintsRegistry().get(tag.getString("name"));
      if (mainwindow.blueprintBuildAttempt(bp)) {
        SusyLog.logger.info("build success, component writeout: {}", bp.writeToNBT());
        this.rocketBlueprintSlot.addToCompound(
            x -> {
              return bp.writeToNBT();
            });
      } else {
        SusyLog.logger.info(
            "status: {}\nerror stage: {}\nerror component: {}",
            mainwindow.error.getTranslationkey(),
            mainwindow.errorStage,
            mainwindow.errorComponentType);
      }
    }
  }

  // TODO: remove ts
  public void SetDefaultBlueprint(Widget.ClickData data) {
    rocketBlueprintSlot.clearNBT();
    var tag = SusyRocketComponents.ROCKET_SOYUZ_BLUEPRINT_DEFAULT.writeToNBT();
    rocketBlueprintSlot.addToCompound(
        compound -> {
          return tag;
        });
    SusyLog.logger.info("set the nbt to {}", tag);
  }

  @Override
  public void receiveCustomData(int dataId, PacketBuffer buf) {
    super.receiveCustomData(dataId, buf);
    if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
      rocketBlueprintSlot.setLocked(buf.readBoolean());
    }
  }

  @Override
  public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
    return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
  }
}
