package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
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

    return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
  }

  public enum guiState {
    rocket_blueprint_assebler,
    rocket_stats,
    not_formed
  }

  private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
    int width = 300;
    int height = 200;
    ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
    builder.slot(master_blueprint, 0, width / 2, height / 2, GuiTextures.SLOT);
    if (master_blueprint.getStackInSlot(0).getCount() != 1) {
      builder.label(
          width / 2, height / 2 - 20, "susy.machine.rocket_simulator.master_blueprint.missing");
    } else {
      builder.label(width / 2, height / 2 - 20, "susy.machine.rocket_simulator.master_blueprint");
    }
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
    drawComponentTree(0, 0, builder, master_blueprint.getStackInSlot(0));

    SusyLog.logger.info("gui drawn");

    return builder;
  }

  private void drawComponentTree(
      int startX, int startY, ModularUI.Builder builder, ItemStack blueprintStack) {
    int spacing = 160;
    master_blueprint.setLocked(
        components.values().stream()
            .flatMap(List::stream)
            .anyMatch(ds -> !ds.isEmpty())); // lock the main blueprint if
    // component data cards are inserted so that they dont get voided

    if (generateComponentTree(blueprintStack)) {
      int i = 0;
      for (Map.Entry<String, List<DataStorageLoader>> entry : this.components.entrySet()) {
        builder.label(
            startX, startY + i * 20, "susy.machine.rocket_simulator.components." + entry.getKey());
        int y = 0;
        for (DataStorageLoader slot : entry.getValue()) {
          builder.slot(slot, 0, (startX + spacing) - y * 20, startY + i * 20, GuiTextures.SLOT);
          y++;
        }
        i++;
      }
    }

    // if (tag.hasKey("components", Constants.NBT.TAG_LIST)) {
    //   NBTTagList components = tag.getTagList("components", Constants.NBT.TAG_COMPOUND);
    //   for (int i = 0; i < components.tagCount(); i++) {
    //     NBTTagCompound comp = components.getCompoundTagAt(i);
    //     if (comp.hasKey("allowedCounts", Constants.NBT.TAG_INT_ARRAY)) {
    //       if (comp.hasKey("type", Constants.NBT.TAG_STRING)) {
    //         String type = comp.getString("type");
    //         builder.label(
    //             startX, startY + i * 20, "susy.machine.rocket_simulator.components." + type);
    //         int[] counts = comp.getIntArray("allowedCounts");
    //
    //         Arrays.stream(counts).max().orElse(0);
    //         List<DataStorageLoader> slots = new ArrayList<>();
    //         for (int j = 0; j < counts.length; j++) {
    //           slots.add(
    //               new DataStorageLoader(
    //                   this,
    //                   x -> {
    //                     if (x.hasTagCompound()) {
    //                       if (x.getTagCompound().hasKey("type")) {
    //                         return x.getTagCompound().getString("type") == type;
    //                       }
    //                     }
    //                     return false;
    //                   }));
    //         }
    //
    //         //  this.components.put(type,);
    //       }
    //     }
    //   }
    // }
  }

  private boolean generateComponentTree(ItemStack stack) {
    if (stack == lastUsedBlueprint)
      return true; // i wasnt sure if it would lag too much if i regenerated the thing on every
    // frame so here its meant to do it only when thhe item changes i guess
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
