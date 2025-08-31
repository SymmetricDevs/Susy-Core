package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.capability.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.widgets.ButtonWidget;

import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.logic.RocketAssemblerLogic;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityRocketAssembler extends RecipeMapMultiblockController {
  public MetaTileEntityRocketAssembler(ResourceLocation metaTileEntityId) {
    super(metaTileEntityId, SuSyRecipeMaps.ROCKET_ASSEMBLER);
    this.recipeMapWorkable = new RocketAssemblerLogic(this);
  }

  public DataStorageLoader blueprintSlot =
      new DataStorageLoader(
          this,
          x -> {
            if (x.hasTagCompound()) {
              NBTTagCompound tag = x.getTagCompound();
              var bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
              return bp.readFromNBT(tag) && bp.isFullBlueprint();
            }
            return false;
          });

  public AbstractRocketBlueprint getCurrentBlueprint() {
    if (blueprintSlot.isEmpty()) return null;
    NBTTagCompound tag = blueprintSlot.getStackInSlot(0).getTagCompound();
    AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
    if (bp.readFromNBT(tag)) {
      return bp;
    } else {
      return null;
      // hopefully never happens since its checked when the item is inserted
    }
  }

  public void abortAssembly() {
    this.componentIndex = 0;
    this.componentList.clear();
    this.recipeMapWorkable.invalidate();
  }

  public void finishAssembly() {
    // TODO: actually spawn the rocket entity?
    abortAssembly();
  }

  public void startAssembly(AbstractRocketBlueprint bp) {
    this.componentList =
        bp.getStages().stream()
            .flatMap(x -> x.getComponents().values().stream())
            .flatMap(List::stream)
            .collect(Collectors.toList());
    this.isWorking = true;
  }

  // list of every component that has to be constructed.
  public List<AbstractComponent<?>> componentList = new ArrayList<>();
  public int componentIndex = 0;
  public boolean isWorking = false;

  public AbstractComponent<?> getCurrentCraftTarget() {
    if (isWorking && componentList.size() - 1 > componentIndex) {
      return this.componentList.get(this.componentIndex++);
    } else {
      this.isWorking = false;
    }
    return null;
  }

  @Override
  protected boolean shouldShowVoidingModeButton() {
    return false;
  }

  @Override
  protected @NotNull BlockPattern createStructurePattern() {
    return FactoryBlockPattern.start()
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            " P           P   P           P   P           P   P           P ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
            " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
        .aisle(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCSMCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ",
            "                                                               ")
        .where(' ', any())
        .where('M', maintenancePredicate())
        .where('S', selfPredicate())
        .where(
            'F',
            states(
                SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                    BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION)))
        .where(
            'C',
            states(
                    SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                        BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION))
                .or(autoAbilities(false, false, true, false, true, false, false))
                .or(
                    abilities(MultiblockAbility.INPUT_ENERGY) // nukler reactor please
                        .setMinGlobalLimited(8)
                        .setMaxGlobalLimited(8)
                        .setPreviewCount(8)))
        .where('R', SuSyPredicates.rails())
        .where(
            'P',
            states(
                SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                    BlockRocketAssemblerCasing.RocketAssemblerCasingType.STRUCTURAL_FRAME)))
        .where(
            'B',
            states(
                SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                    BlockRocketAssemblerCasing.RocketAssemblerCasingType.RAILS)))
        .build();
  }

  @Override
  public void receiveCustomData(int dataId, PacketBuffer buf) {
    super.receiveCustomData(dataId, buf);
    if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
      this.blueprintSlot.setLocked(buf.readBoolean());
    }
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
  protected ModularUI createUI(EntityPlayer entityPlayer) {
    return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
  }

  protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
    ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);

    // Display
    if (this instanceof IProgressBarMultiblock progressMulti && progressMulti.showProgressBar()) {
      builder.image(4, 4, 190, 109, GuiTextures.DISPLAY);

      if (progressMulti.getNumProgressBars() == 3) {
        // triple bar
        ProgressWidget progressBar =
            new ProgressWidget(
                    () -> progressMulti.getFillPercentage(0),
                    4,
                    115,
                    62,
                    7,
                    progressMulti.getProgressBarTexture(0),
                    ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
        builder.widget(progressBar);

        progressBar =
            new ProgressWidget(
                    () -> progressMulti.getFillPercentage(1),
                    68,
                    115,
                    62,
                    7,
                    progressMulti.getProgressBarTexture(1),
                    ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 1));
        builder.widget(progressBar);

        progressBar =
            new ProgressWidget(
                    () -> progressMulti.getFillPercentage(2),
                    132,
                    115,
                    62,
                    7,
                    progressMulti.getProgressBarTexture(2),
                    ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 2));
        builder.widget(progressBar);
      } else if (progressMulti.getNumProgressBars() == 2) {
        // double bar
        ProgressWidget progressBar =
            new ProgressWidget(
                    () -> progressMulti.getFillPercentage(0),
                    4,
                    115,
                    94,
                    7,
                    progressMulti.getProgressBarTexture(0),
                    ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
        builder.widget(progressBar);

        progressBar =
            new ProgressWidget(
                    () -> progressMulti.getFillPercentage(1),
                    100,
                    115,
                    94,
                    7,
                    progressMulti.getProgressBarTexture(1),
                    ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 1));
        builder.widget(progressBar);
      } else {
        // single bar
        ProgressWidget progressBar =
            new ProgressWidget(
                    () -> progressMulti.getFillPercentage(0),
                    4,
                    115,
                    190,
                    7,
                    progressMulti.getProgressBarTexture(0),
                    ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> progressMulti.addBarHoverText(list, 0));
        builder.widget(progressBar);
      }
      builder.widget(
          new IndicatorImageWidget(174, 93, 17, 17, getLogo())
              .setWarningStatus(getWarningLogo(), this::addWarningText)
              .setErrorStatus(getErrorLogo(), this::addErrorText));
    } else {
      builder.image(4, 4, 190, 117, GuiTextures.DISPLAY);
      builder.widget(
          new IndicatorImageWidget(174, 101, 17, 17, getLogo())
              .setWarningStatus(getWarningLogo(), this::addWarningText)
              .setErrorStatus(getErrorLogo(), this::addErrorText));
    }

    builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
    builder.widget(
        new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
            .setMaxWidthLimit(181)
            .setClickHandler(this::handleDisplayClick));

    // Power Button
    IControllable controllable =
        getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
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

    // Voiding Mode Button
    if (shouldShowVoidingModeButton()) {
      builder.widget(
          new ImageCycleButtonWidget(
                  173,
                  161,
                  18,
                  18,
                  GuiTextures.BUTTON_VOID_MULTIBLOCK,
                  4,
                  this::getVoidingMode,
                  this::setVoidingMode)
              .setTooltipHoverString(MultiblockWithDisplayBase::getVoidingModeTooltip));
    } else {
      builder.widget(
          new ImageWidget(173, 161, 18, 18, GuiTextures.BUTTON_VOID_NONE)
              .setTooltip("gregtech.gui.multiblock_voiding_not_supported"));
    }

    // // Distinct Buses Button
    // if (this instanceof IDistinctBusController distinct && distinct.canBeDistinct()) {
    //     builder.widget(new ImageCycleButtonWidget(173, 143, 18, 18,
    // GuiTextures.BUTTON_DISTINCT_BUSES,
    //             distinct::isDistinct, distinct::setDistinct)
    //                     .setTooltipHoverString(i -> "gregtech.multiblock.universal.distinct_" +
    //                             (i == 0 ? "disabled" : "enabled")));
    // } else {
    //     builder.widget(new ImageWidget(173, 143, 18, 18, GuiTextures.BUTTON_NO_DISTINCT_BUSES)
    //             .setTooltip("gregtech.multiblock.universal.distinct_not_supported"));
    // }

    // Flex Button
    // TODO: make it abort the construction process
    builder.widget(getFlexButton(173, 125, 18, 18));
    builder.label(100, 60, this.getMetaFullName() + ".blueprint_slot.name");
    builder.slot(this.blueprintSlot, 0, 100, 78, GuiTextures.SLOT_DARK);
    builder.bindPlayerInventory(entityPlayer.inventory, 125);
    return builder;
  }

  @Override
  protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {

    return null;
  }

  @Override
  public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
    return new MetaTileEntityRocketAssembler(metaTileEntityId);
  }
}
