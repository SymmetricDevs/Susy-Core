package supersymmetry.common.command;

import java.util.*;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import gregtech.api.unification.material.Material;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.LocalizationUtils;
import gregtech.core.unification.material.internal.MaterialRegistryManager;

public class CommandUntranslatedKeys extends CommandBase {

    // false positives and items from other mods
    static final String[] BLACKLIST = {
            "tile.tardis.tardis_top_wardrobe.name",
            "gcym.machine.tiered_hatch.opv.name",
            "item.industrialrenewal.pointer_long.name",
            "item.industrialrenewal.disc_r.name",
            "tile.techguns.bunkerdoor.name",
            "robot.name",
            "item.industrialrenewal.cart_linkable.name",
            "tile.tardis.toyota_light_off.name",
            "tile.engineersdecor.sign_caution.name",
            "tile.tardis.multiblock.name",
            "item.industrialrenewal.indicator_on.name",
            "item.industrialrenewal.indicator_off.name",
            "tile.tardis.gallifreyan_grass_snow.name",
            "tile.tardis.tardis_top_wood_door.name",
            "tile.serenetweaks.BlockBranches.name",
            "tile.tardis.toyota_light_on.name",
            "item.industrialrenewal.fire.name",
            "item.industrialrenewal.push_button.name",
            "item.industrialrenewal.limiter.name",
            "tile.tardis.multiblock_master.name",
            "tile.appliedenergistics2.paint.name",
            "item.industrialrenewal.pointer.name",
            "item.industrialrenewal.fluid_loader_arm.name",
            "tile.ltinput.name",
            "tile.ltoutput.name",
            "tile.tardis.tardis_top_cc.name",
            "item.industrialrenewal.switch_on.name",
            "tile.ltsinglecable.name",
            "item.industrialrenewal.lathecutter.name",
            "item.immersiverailroading:item_rolling_stock.name",
            "item.industrialrenewal.switch_off.name",
            "tile.tardis.console_02.name",
            "tile.network_cable.name",
            "item.industrialrenewal.bar_level.name",
            "tile.tardis.console_01.name",
            "tile.techguns.door3x3.name",
            "tile.tardis.tardis_top_clock.name",
            "item.jump_rod.name",
            "tile.catwalks.catwalk.unknown.name",
            "item.nae2.virtual_pattern.name",
            "tile.mcjtylib_ng.multipart.name",
            "tile.srparasites.evolutionlure_ten.name",
            "tile.transparent.name",
            "tile.tardis.tardis_top_tt.name",
            "tile.tardis.tardis_top_01.name",
            "tile.tardis.tardis_top_02.name",
            "item.fake_sword.name",
            "item.industrialrenewal.rotary_drum.name",
            "tile.tardis.tardis_top_03.name",
            "tile.tardis.tardis_top_04.name",
            "tile.srparasites.evolutionlure_nine.name",
            "item.industrialrenewal.label_5.name",
            "tile.tardis.console_05.name",
            "tile.tardis.console_04.name",
            "tile.tardis.gallifreyan_sand.name",
            "item.multi_tool.name",
            "tile.tardis.console_03.name",
            "tile.spawn_command.name",
            "tile.null.name",
            "item.null.name"
    };

    @Override
    public String getName() {
        return "untranslatedkeys";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "susy.command.untranslatedkeys.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Set<String> untranslated = new HashSet<>();
        checkMaterials(untranslated);
        checkFluids(untranslated);
        checkItems(untranslated);

        List.of(BLACKLIST).forEach(untranslated::remove);

        StringBuilder builder = new StringBuilder();
        for (String str : untranslated) {
            builder.append(str);
            builder.append("=\n");
        }

        untranslated.clear();

        sender.sendMessage(new TextComponentString(builder.toString()));
        ClipboardUtil.copyToClipboard(builder.toString());
    }

    public void checkMaterials(Set<String> untranslated) {
        for (Material mat : MaterialRegistryManager.getInstance().getRegisteredMaterials()) {
            if (!LocalizationUtils.hasKey(mat.getUnlocalizedName())) {
                untranslated.add(mat.getUnlocalizedName());
            }
        }
    }

    public void checkFluids(Set<String> untranslated) {
        Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
        for (Fluid fluid : fluids.values()) {
            String localized = fluid.getLocalizedName(new FluidStack(fluid, 1));
            if (localized.isBlank() || localized.equals(fluid.getUnlocalizedName())) {
                untranslated.add(fluid.getUnlocalizedName());
            }
        }
    }

    public void checkItems(Set<String> untranslated) {
        Collection<Item> items = ForgeRegistries.ITEMS.getValuesCollection();
        for (Item item : items) {
            if (item.getHasSubtypes()) {
                NonNullList<ItemStack> subItems = NonNullList.create();
                for (CreativeTabs tab : item.getCreativeTabs()) {
                    if (tab != null) {
                        item.getSubItems(tab, subItems);
                    }
                }
                for (ItemStack stack : subItems) {
                    String key = stack.getTranslationKey() + ".name";
                    if (stack.getItem().getItemStackDisplayName(stack).equals(key)) {
                        untranslated.add(key);
                    }
                }
            } else {
                String key = item.getTranslationKey(new ItemStack(item, 1)) + ".name";
                if (item.getItemStackDisplayName(new ItemStack(item, 1)).equals(key)) {
                    untranslated.add(key);
                }
            }
        }
    }
}
