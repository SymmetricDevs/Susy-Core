package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.mui.widget.HorizontalScrollableListWidget;
import supersymmetry.common.mui.widget.RocketSimulatorComponentContainerWidget;
import supersymmetry.common.mui.widget.SlotWidgetAdvanced;

public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase {
  public DataStorageLoader master_blueprint = null;
  public Map<String, List<DataStorageLoader>> components = new HashMap<>();
  private ItemStack lastUsedBlueprint;

  public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
    super(metaTileEntityId);
    master_blueprint =
        new DataStorageLoader(
            this,
            item -> {
              int meta = SuSyMetaItems.isMetaItem(item);
              return meta == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue;
            });
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

  public enum guiState {
    rocket_blueprint_assebler,
    rocket_stats,
    not_formed // not even used yet :<
  }

  private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
    int width = 300;
    int height = 200;
    ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
    builder.label(width / 2, height / 2 - 20, "susy.machine.rocket_simulator.master_blueprint");
    builder.widget(
        new IndicatorImageWidget(width, height, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
            .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
            .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));
    builder.widget(
        new ClickButtonWidget(
            width - 40,
            height - 30,
            40,
            30,
            new TextComponentTranslation("debug").getUnformattedComponentText(),
            this::SetDefaultBlueprint));
    builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
    builder.bindPlayerInventory(entityPlayer.inventory, height);
    RocketSimulatorComponentContainerWidget mainWindow =
        new RocketSimulatorComponentContainerWidget(new Position(9, 9), new Size(200, 200));

    builder.widget(mainWindow);
    builder.widget(
        new SlotWidgetAdvanced(
                master_blueprint,
                0,
                width / 2,
                height / 2,
                () -> {
                  drawComponentTree(10, 25, master_blueprint.getStackInSlot(0), mainWindow);
                  master_blueprint.setLocked(
                      components.values().stream()
                          .flatMap(List::stream)
                          .anyMatch(ds -> !ds.isEmpty())); // lock the main blueprint if
                  // component data cards are inserted so that they dont get voided

                })
            .setBackgroundTexture(GuiTextures.SLOT_DARK));

    SusyLog.logger.info("gui drawn");

    return builder;
  }

  private void drawComponentTree(
      int startX,
      int startY,
      ItemStack blueprintStack,
      RocketSimulatorComponentContainerWidget container) {

    // meant to do like
    // comp name                #slot# #slot# #slot#
    //                          ----------#slider#---------------------
    // comp name but longer     |  #slot# #slot# #slot# #slot# #slot# | #slot# #slot#
    //                          =============visible part==============

    container.RemoveSlotLists();
    if (generateComponentTree(blueprintStack)) {
      for (Map.Entry<String, List<DataStorageLoader>> entry : this.components.entrySet()) {
        String text = entry.getKey();
        var list = new HorizontalScrollableListWidget(startX, startY, 18 * 5 + 5, 20);
        list.setSliderActive(entry.getValue().size() > 5);
        for (DataStorageLoader slot : entry.getValue()) {
          // position will get changed in the .addSlotList thing later anyways
          list.addWidget(new SlotWidget(slot, 0, 1, 1).setBackgroundTexture(GuiTextures.SLOT_DARK));
        }
        container.addSlotList(
            new LabelWidget(0, 0, "susy.machine.rocket_simulator.component." + text, 0xFFFFFF),
            list);
      }
    }
  }

  private boolean generateComponentTree(ItemStack stack) {
    if (stack == lastUsedBlueprint)
      return true; // i wasnt sure if it would lag too much if i regenerated the thing on every
    // frame so here its meant to do it only when the item changes i guess
    lastUsedBlueprint = stack;
    this.components.clear();
    if (stack.hasTagCompound()) {
      NBTTagCompound tag = stack.getTagCompound();
      if (tag.hasKey("components", Constants.NBT.TAG_LIST)) {
        NBTTagList components = tag.getTagList("components", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < components.tagCount(); i++) {
          NBTTagCompound comp = components.getCompoundTagAt(i);
          if (comp.hasKey("allowedCounts", Constants.NBT.TAG_INT_ARRAY)) {
            if (comp.hasKey("type", Constants.NBT.TAG_STRING)) {
              String type = comp.getString("type");
              int[] counts = comp.getIntArray("allowedCounts");
              int max = Arrays.stream(counts).max().orElse(0);
              List<DataStorageLoader> slots = new ArrayList<>();
              for (int j = 0; j < max; j++) {
                slots.add(
                    new DataStorageLoader(
                        this,
                        x -> {
                          if (x.hasTagCompound()) {
                            if (x.getTagCompound().hasKey("type")) {
                              return x.getTagCompound().getString("type") == type;
                            }
                          }
                          return false;
                        }));
                this.components.put(type, slots);
              }
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  private boolean verifyDatacardArrangementAndMergeToMaster() {
    // assumes that it has all of the nbt's since i dont wanna do that entire thing again

    NBTTagList componentsList =
        master_blueprint
            .getStackInSlot(0)
            .getTagCompound()
            .getTagList("components", Constants.NBT.TAG_LIST);
    for (int i = 0; i < componentsList.tagCount(); i++) {
      NBTTagCompound comp = componentsList.getCompoundTagAt(i);
      List<DataStorageLoader> cardSlots = this.components.get(comp.getString("type"));
      int count = (int) cardSlots.stream().map(x -> !x.isEmpty()).count();
      if (!Arrays.asList(comp.getIntArray("allowedCounts")).contains(count)) {
        return false;
      } // not sure how to make the warnings shut up, likely just did it incorrectly
    }
    for (Map.Entry<String,List<DataStorageLoader>> entry : this.components.entrySet()) {

        }

    return false;
  }

  // TODO: remove the button, if i forget to do this you can call me an idiot for the rest of the
  // year
  public void SetDefaultBlueprint(Widget.ClickData data) {
    this.markDirty();
    master_blueprint.clearNBT();
    master_blueprint.mutateItem("rocket", "soyuz");
    NBTTagList components = new NBTTagList();
    NBTTagCompound fuel_tank = new NBTTagCompound();
    fuel_tank.setString("type", "tank");
    fuel_tank.setIntArray("allowedCounts", new int[] {2, 3, 4, 5});
    components.appendTag(fuel_tank);
    NBTTagCompound nozzle = new NBTTagCompound();
    nozzle.setString("type", "nozzle");
    nozzle.setIntArray("allowedCounts", new int[] {5, 10, 20});
    components.appendTag(nozzle);
    master_blueprint.addToCompound(
        x -> {
          x.setTag("components", components);
          return x;
        });
  }

  @Override
  public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
    return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
  }
}
