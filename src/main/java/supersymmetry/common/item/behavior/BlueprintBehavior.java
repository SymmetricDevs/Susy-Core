package supersymmetry.common.item.behavior;

import java.util.*;
import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.util.GTUtility;
import supersymmetry.Supersymmetry;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.common.item.SuSyMetaItems;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class BlueprintBehavior implements IItemBehaviour, ISubItemHandler {

    private final Consumer<List<String>> lines;
    private final List<String> keys;

    public BlueprintBehavior(@NotNull Consumer<List<String>> lines, List<String> keys) {
        this.lines = lines;
        this.keys = keys;
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        var tag = GTUtility.getOrCreateNbtCompound(itemStack);
        return tag.getString("name");
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack.copy());
        if (itemStack.getMetadata() == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue) {
            for (AbstractRocketBlueprint blueprint : AbstractRocketBlueprint.getBlueprintsRegistry().values()) {
                ItemStack configured = itemStack.copy();
                NBTTagCompound tag = blueprint.writeToNBT();
                configured.setTagCompound(tag);
                subItems.add(configured);
            }
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        this.lines.accept(lines);
        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag == null)
            return;

        for (String key : this.keys) {
            if (tag.hasKey(key, Constants.NBT.TAG_STRING)) {
                if (tag.hasKey("stages")) {
                    lines.add(I18n.format(itemStack.getTranslationKey() + ".tag." + tag.getString(key)) + " ID: " +
                            getID(tag));
                } else {
                    lines.add(I18n.format(itemStack.getTranslationKey() + ".tag." + tag.getString(key)));
                }
            }
        }

        if (tag.hasKey("name", Constants.NBT.TAG_STRING)) {
            String targetName = tag.getString("name");
            for (AbstractComponent<?> component : AbstractComponent.getRegistry()) {
                if (component.getName().equals(targetName)) {
                    lines.addAll(component.getTooltipLines(tag));
                    break;
                }
            }
        }
    }

    private String getID(NBTTagCompound key) {
        // Left pad
        String fullID = String.format("%08x", key.hashCode());
        return fullID.toUpperCase();
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty())
            return;

        if (player.world.isRemote)
            return;

        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
            if (!bp.readFromNBT(tag)) {
                bp = null;
            }
            List<AbstractComponent<?>> componentList;

            componentList = bp.getStages().stream().flatMap(x -> x.getComponents().values().stream())
                    .flatMap(List::stream).toList();
            HashMap<String, Integer> totalItemList = new HashMap<>();
            for (AbstractComponent<?> currentComponent : componentList) {
                List<MaterialCost> ingredientList = currentComponent.getMaterials();
                for (MaterialCost materialCost : ingredientList) {
                    String itemType = materialCost.getStack().getDisplayName(); // this is incredibly stupid, but for
                                                                                // some reason storing itemstacks just
                                                                                // didn't work
                    if (totalItemList.containsKey(itemType)) {
                        totalItemList.replace(itemType, totalItemList.get(itemType) + materialCost.getCount());
                    } else {
                        totalItemList.put(itemType, materialCost.getCount());
                    }
                }
            }

            if (totalItemList.isEmpty()) {
                event.setCanceled(true);
                return;
            }

            player.sendStatusMessage(new TextComponentTranslation("chat.susy.rocket_blueprint.item_list"), false);

            for (Map.Entry<String, Integer> entry : totalItemList.entrySet()) {
                player.sendStatusMessage(new TextComponentTranslation(
                        entry.getKey() + " x" + entry.getValue()), false);
            }

            event.setCanceled(true);

            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }
        event.setCanceled(true);
    }
}
