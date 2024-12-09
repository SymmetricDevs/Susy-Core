package supersymmetry.common.item.armor;

import gregtech.api.GTValues;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.metaitem.stats.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.capability.IElytraFlyingProvider;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.api.util.ElytraFlyingUtils;
import supersymmetry.client.renderer.handler.JetWingpackModel;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public class JetWingpack extends ArmorLogicSuite implements IItemHUDProvider {

    public static final int TANK_CAPACITY = 32000;
    public static final Function<FluidStack, Integer> COMBUSTION_FUEL_BURN_TIME = fluidStack -> {
        Recipe recipe = RecipeMaps.GAS_TURBINE_FUELS.findRecipe(
                Integer.MAX_VALUE,
                Collections.emptyList(),
                Collections.singletonList(fluidStack));
        return recipe != null ? recipe.getDuration() : 0;
    };

    @SideOnly(Side.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    protected JetWingpack() {
        super(1, TANK_CAPACITY, GTValues.EV, EntityEquipmentSlot.CHEST);
        if (ArmorUtils.SIDE.isClient()) {
            this.HUD = new ArmorUtils.ModularHUD();
        }
    }

    @SideOnly(Side.CLIENT)
    public void drawHUD(@NotNull ItemStack item) {
        IFluidHandlerItem tank = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (tank != null) {
            IFluidTankProperties[] prop = tank.getTankProperties();
            if (prop[0] != null && prop[0].getContents() != null) {
                if (prop[0].getContents().amount == 0) {
                    return;
                }

                String formated = String.format("%.1f", (float) prop[0].getContents().amount * 100.0F / (float) prop[0].getCapacity());
                this.HUD.newString(I18n.format("metaarmor.hud.fuel_lvl", formated + "%"));
                NBTTagCompound data = item.getTagCompound();
                if (data != null && data.hasKey("engineActive")) {
                    String status = data.getBoolean("engineActive") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled");
                    String result = I18n.format("metaarmor.hud.engine_status", status);
                    this.HUD.newString(result);
                }
            }
        }

        this.HUD.draw();
        this.HUD.reset();
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);

        byte toggleTimer = 0;
        boolean pressed = false;
        boolean wingActive = false;
        boolean engineActive = false;

        if (data.hasKey("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
        if (data.hasKey("pressed")) pressed = data.getBoolean("pressed");
        if (data.hasKey("wingActive")) wingActive = data.getBoolean("wingActive");
        if (data.hasKey("engineActive")) engineActive = data.getBoolean("engineActive");

        if (toggleTimer == 0 && KeyBind.ARMOR_MODE_SWITCH.isKeyDown(player)) {
            engineActive = !engineActive;
            toggleTimer = 5;
            if (!world.isRemote) {
                if (engineActive)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jet_wingpack.engine_active"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jet_wingpack.engine_inactive"), true);
            }
        }

        if (engineActive && player.isElytraFlying() && drainFuel(itemStack, getEnergyPerUse(), true)) {
            Vec3d vec3d = player.getLookVec();
            if (KeyBind.VANILLA_SNEAK.isKeyDown(player)) {
                player.motionX += vec3d.x * 0.001D - 0.05D * player.motionX;
                player.motionY += -0.005D - 0.05D * player.motionY;
                player.motionZ += vec3d.z * 0.001D - 0.05D * player.motionZ;
            } else {
                player.motionX += 0.05D * (2 * vec3d.x - player.motionX);
                player.motionY += 0.05D * (2 * vec3d.y - player.motionY);
                player.motionZ += 0.05D * (2 * vec3d.z - player.motionZ);
                world.spawnParticle(EnumParticleTypes.CLOUD, player.posX, player.posY, player.posZ, 0.0, 0.0, 0.0);
            }
            drainFuel(itemStack, getEnergyPerUse(), false);
        }

        if (!pressed && KeyBind.VANILLA_JUMP.isKeyDown(player)) {
            pressed = true;
            if (!world.isRemote) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!wingActive) {
                    if (ElytraFlyingUtils.canTakeOff(playerMP, engineActive)) {
                        playerMP.setElytraFlying();
                        wingActive = true;
                    }
                } else {
                    playerMP.clearElytraFlying();
                    wingActive = false;
                }
            }
        }

        if (pressed && !KeyBind.VANILLA_JUMP.isKeyDown(player)) pressed = false;
        if (toggleTimer > 0) toggleTimer--;

        data.setByte("toggleTimer", toggleTimer);
        data.setBoolean("pressed", pressed);
        data.setBoolean("wingActive", wingActive);
        data.setBoolean("engineActive", engineActive);

        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public void addToolComponents(ArmorMetaItem.ArmorMetaValueItem mvi) {
        mvi.addComponents(new TestElytraFlyingProvider());
        mvi.addComponents(new JetWingpackBehaviour(TANK_CAPACITY));
    }

    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
                                    ModelBiped defaultModel) {
        return JetWingpackModel.INSTANCE;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "susy:textures/armor/jet_wingpack.png"; // actually useless
    }

    protected boolean drainFuel(@NotNull ItemStack stack, int amount, boolean simulate) {
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(stack);
        short burnTimer = 0;
        if (data.hasKey("burnTimer")) burnTimer = data.getShort("burnTimer");
        if (burnTimer > 0) {
            if (!simulate) {
                data.setShort("burnTimer", (short) (burnTimer - 1));
            }
            return true;
        }

        IFluidHandlerItem fluidHandler = getFluidHandler(stack);
        if (fluidHandler == null) return false;
        FluidStack fuelStack = fluidHandler.drain(amount, false);
        if (fuelStack == null) return false;

        int burnTime = COMBUSTION_FUEL_BURN_TIME.apply(fuelStack);
        if (burnTime <= 0) return false;

        if (!simulate) {
            data.setShort("burnTimer", (short) (burnTime));
            fluidHandler.drain(amount, true);
        }
        return true;
    }

    private IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    private static class TestElytraFlyingProvider implements IItemComponent, IElytraFlyingProvider, ICapabilityProvider, IItemCapabilityProvider {

        @Override
        public boolean isElytraFlying(@NotNull EntityLivingBase entity, @NotNull ItemStack itemStack, boolean shouldStop) {
            if (entity instanceof EntityPlayer) {
                NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
                if (shouldStop) {
                    data.setBoolean("wingActive", false);
                }
                return data.hasKey("wingActive") && data.getBoolean("wingActive");
            }
            return false;
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == SuSyCapabilities.ELYTRA_FLYING_PROVIDER;
        }

        @Nullable
        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == SuSyCapabilities.ELYTRA_FLYING_PROVIDER ? SuSyCapabilities.ELYTRA_FLYING_PROVIDER.cast(this) : null;
        }

        @Override
        public ICapabilityProvider createProvider(ItemStack itemStack) {
            return this;
        }
    }

    // Yeah, this is a bit bloat...
    public class JetWingpackBehaviour implements IItemDurabilityManager, IItemCapabilityProvider, IItemBehaviour, ISubItemHandler {

        private static final IFilter<FluidStack> JETPACK_FUEL_FILTER = new IFilter<>() {

            @Override
            public boolean test(@NotNull FluidStack fluidStack) {
                return RecipeMaps.COMBUSTION_GENERATOR_FUELS.find(Collections.emptyList(),
                        Collections.singletonList(fluidStack), (Objects::nonNull)) != null;
            }

            @Override
            public int getPriority() {
                return IFilter.whitelistLikePriority();
            }
        };
        public final int maxCapacity;
        private final Pair<Color, Color> durabilityBarColors;

        public JetWingpackBehaviour(int internalCapacity) {
            this.maxCapacity = internalCapacity;
            this.durabilityBarColors = GradientUtil.getGradient(0xB7AF08, 10);
        }

        @Override
        public double getDurabilityForDisplay(@NotNull ItemStack itemStack) {
            IFluidHandlerItem fluidHandlerItem = itemStack
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null) return 0;
            IFluidTankProperties fluidTankProperties = fluidHandlerItem.getTankProperties()[0];
            FluidStack fluidStack = fluidTankProperties.getContents();
            return fluidStack == null ? 0 : (double) fluidStack.amount / (double) fluidTankProperties.getCapacity();
        }

        @Nullable
        @Override
        public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
            return durabilityBarColors;
        }

        @Override
        public ICapabilityProvider createProvider(ItemStack itemStack) {
            return new GTFluidHandlerItemStack(itemStack, maxCapacity)
                    .setFilter(JETPACK_FUEL_FILTER);
        }

        @Override
        public void addInformation(ItemStack itemStack, List<String> lines) {
            IItemBehaviour.super.addInformation(itemStack, lines);
            NBTTagCompound data = GTUtility.getOrCreateNbtCompound(itemStack);
            String status = I18n.format("metaarmor.hud.status.disabled");
            if (data.hasKey("engineActive")) {
                if (data.getBoolean("engineActive"))
                    status = I18n.format("metaarmor.hud.status.enabled");
            }
            lines.add(I18n.format("metaarmor.hud.engine_status", status));
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
            return onRightClick(world, player, hand);
        }

        @Override
        public String getItemSubType(ItemStack itemStack) {
            return "";
        }

        @Override
        public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
            ItemStack copy = itemStack.copy();
            IFluidHandlerItem fluidHandlerItem = copy
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem != null) {
                fluidHandlerItem.fill(Materials.Diesel.getFluid(TANK_CAPACITY), true);
                subItems.add(copy);
            } else {
                subItems.add(itemStack);
            }
        }
    }
}
