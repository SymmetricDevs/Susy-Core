package supersymmetry.common.item.behavior;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import supersymmetry.api.rocketry.AssemblyStep;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.common.item.SuSyMetaItems;

public class BlueprintBehavior extends AbstractCardBehavior {

    public BlueprintBehavior(@NotNull Consumer<List<String>> lines, List<String> keys) {
        super(lines, keys);
    }

    @Override
    protected boolean showCardId(NBTTagCompound tag) {
        return tag.hasKey("stages");
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack.copy());
        if (itemStack.getMetadata() == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue) {
            for (AbstractRocketBlueprint blueprint : AbstractRocketBlueprint.getBlueprintsRegistry().values()) {
                ItemStack configured = itemStack.copy();
                configured.setTagCompound(blueprint.writeToNBT());
                subItems.add(configured);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.world.isRemote)
            return super.onItemRightClick(world, player, hand);

        if (!stack.hasTagCompound()) {
            return super.onItemRightClick(world, player, hand);
        }
        AbstractRocketBlueprint bp = AbstractRocketBlueprint.fromTag(stack.getTagCompound());
        if (bp == null) {
            return super.onItemRightClick(world, player, hand);
        }

        // The assembly sequence, not just the stages: it carries the blueprint's fixed
        // cost groups too, which are otherwise invisible until the assembler asks for
        // them.
        Object2IntOpenHashMap<String> totalItemList = new Object2IntOpenHashMap<>();
        for (AssemblyStep step : bp.getAssemblySequence()) {
            for (GTRecipeInput ingredient : step.getRecipeInputs()) {
                ItemStack[] matching = ingredient.getInputStacks();
                if (matching.length == 0)
                    continue;
                String itemType = matching[0].getDisplayName(); // this is incredibly stupid, but for
                // some reason storing itemstacks just
                // didn't work
                totalItemList.merge(itemType, ingredient.getAmount(), Integer::sum);
            }
        }

        if (totalItemList.isEmpty()) {
            return super.onItemRightClick(world, player, hand);
        }

        player.sendStatusMessage(new TextComponentTranslation("chat.susy.rocket_blueprint.item_list"), false);

        for (Map.Entry<String, Integer> entry : totalItemList.entrySet()) {
            player.sendStatusMessage(new TextComponentTranslation(
                    entry.getKey() + " x" + entry.getValue()), false);
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
