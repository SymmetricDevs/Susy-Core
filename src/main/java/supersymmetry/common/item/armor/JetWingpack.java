package supersymmetry.common.item.armor;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
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
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.metaitem.stats.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GradientUtil;
import gregtech.api.util.input.KeyBind;
import supersymmetry.api.capability.IElytraFlyingProvider;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.util.ElytraFlyingUtils;
import supersymmetry.client.audio.MovingSoundJetEngine;
import supersymmetry.client.renderer.handler.JetWingpackModel;

public class JetWingpack extends ArmorLogicSuite implements IItemHUDProvider {

    protected static final int TANK_CAPACITY = 32000;

    protected static final int MAX_SPEED = 2;
    protected static final double MIN_SPEED = 0.02;
    protected static final double THRUST = 0.05D;
    protected static final double REVERSE_THRUST = 0.05D;
    protected static final double FALLING = 0.005D;

    protected static final ISpecialArmor.ArmorProperties DEFAULT_PROPERTIES = new ISpecialArmor.ArmorProperties(
            Integer.MIN_VALUE, -2, Integer.MAX_VALUE);

    protected static final Function<FluidStack, Integer> FUEL_BURN_TIME = fluidStack -> {
        Recipe recipe = SuSyRecipeMaps.JET_WINGPACK_FUELS.findRecipe(
                GTValues.V[GTValues.MV],
                Collections.emptyList(),
                Collections.singletonList(fluidStack));
        return recipe != null ? recipe.getDuration() : 0;
    };

    @SideOnly(Side.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    @SideOnly(Side.CLIENT)
    protected MovingSoundJetEngine jetEngineSound;

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

                String formated = String.format("%.1f",
                        (float) prop[0].getContents().amount * 100.0F / (float) prop[0].getCapacity());
                this.HUD.newString(I18n.format("metaarmor.hud.fuel_lvl", formated + "%"));
                NBTTagCompound data = item.getTagCompound();
                if (data != null && data.hasKey("engineActive")) {
                    String status = data.getBoolean("engineActive") ? I18n.format("metaarmor.hud.status.enabled") :
                            I18n.format("metaarmor.hud.status.disabled");
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
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jet_wingpack.engine_active"),
                            true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jet_wingpack.engine_inactive"),
                            true);
            }
        }

        if (world.isRemote) {
            handleSounds(player, engineActive);
        }

        if (engineActive && player.isElytraFlying() && drainFuel(itemStack, getEnergyPerUse(), true)) {
            Vec3d lookVec = player.getLookVec();
            if (KeyBind.VANILLA_SNEAK.isKeyDown(player)) {
                // handles braking
                player.motionX -= REVERSE_THRUST * (player.motionX - MIN_SPEED * lookVec.x); // v(t+1) = v(t) - THRUST *
                                                                                             // (v(t) - MIN_SPEED)
                player.motionY -= REVERSE_THRUST * player.motionY + FALLING;                 // so that you won't fly
                                                                                             // upwards when braking
                player.motionZ -= REVERSE_THRUST * (player.motionZ - MIN_SPEED * lookVec.z);
            } else {
                // handles acceleration
                player.motionX += THRUST * (MAX_SPEED * lookVec.x - player.motionX); // v(t+1) = v(t) + THRUST *
                                                                                     // (MAX_SPEED - v(t))
                player.motionY += THRUST * (MAX_SPEED * lookVec.y - player.motionY); // or dv/dt = THRUST * (MAX_SPEED -
                                                                                     // v)
                player.motionZ += THRUST * (MAX_SPEED * lookVec.z - player.motionZ);
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

    // I hate this...
    @SideOnly(Side.CLIENT)
    public void handleSounds(EntityPlayer player, boolean engineActive) {
        if (this.jetEngineSound == null) {
            this.jetEngineSound = new MovingSoundJetEngine(player);
            Minecraft.getMinecraft().getSoundHandler().playSound(jetEngineSound);
        }
        if (engineActive && !jetEngineSound.isPlaying()) {
            jetEngineSound.startPlaying();
        } else if (!engineActive && jetEngineSound.isPlaying()) {
            jetEngineSound.stopPlaying();
        }
        if (jetEngineSound.isThrottled() != KeyBind.VANILLA_SNEAK.isKeyDown(player)) {
            jetEngineSound.setThrottled(KeyBind.VANILLA_SNEAK.isKeyDown(player));
        }
    }

    @Override
    public void addToolComponents(ArmorMetaItem.ArmorMetaValueItem mvi) {
        mvi.addComponents(new ElytraFlyingProvider());
        mvi.addComponents(new JetWingpackBehaviour(TANK_CAPACITY));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
                                    ModelBiped defaultModel) {
        return JetWingpackModel.INSTANCE;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "susy:textures/armor/jet_wingpack.png";
    }

    @Override
    public boolean handleUnblockableDamage(EntityLivingBase entity, @NotNull ItemStack armor, DamageSource source,
                                           double damage, EntityEquipmentSlot equipmentSlot) {
        return (source == DamageSource.FLY_INTO_WALL && entity.isElytraFlying());
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase entity, @NotNull ItemStack armor,
                                                       DamageSource source, double damage, EntityEquipmentSlot slot) {
        // triple the amount of kinetic damages :trollface:
        return source == DamageSource.FLY_INTO_WALL ? DEFAULT_PROPERTIES :
                super.getProperties(entity, armor, source, damage, slot);
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

        int burnTime = FUEL_BURN_TIME.apply(fuelStack);
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

    private static class ElytraFlyingProvider implements IItemComponent, IElytraFlyingProvider, ICapabilityProvider,
                                              IItemCapabilityProvider {

        @Override
        public boolean isElytraFlying(@NotNull EntityLivingBase entity, @NotNull ItemStack itemStack,
                                      boolean shouldStop) {
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
            return capability == SuSyCapabilities.ELYTRA_FLYING_PROVIDER ?
                    SuSyCapabilities.ELYTRA_FLYING_PROVIDER.cast(this) : null;
        }

        @Override
        public ICapabilityProvider createProvider(ItemStack itemStack) {
            return this;
        }
    }

    public class JetWingpackBehaviour implements IItemDurabilityManager, IItemCapabilityProvider, IItemBehaviour,
                                      ISubItemHandler {

        private static final IFilter<FluidStack> JET_WINGPACK_FUEL_FILTER = new IFilter<>() {

            @Override
            public boolean test(@NotNull FluidStack fluidStack) {
                return FUEL_BURN_TIME.apply(fluidStack) > 0;
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
                    .setFilter(JET_WINGPACK_FUEL_FILTER);
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
                Optional<Recipe> firstRecipe = SuSyRecipeMaps.JET_WINGPACK_FUELS.getRecipeList().stream().findFirst();
                firstRecipe.ifPresent(recipe -> {
                    Optional<FluidStack> inputFluidStack = recipe.getFluidInputs().stream()
                            .map(GTRecipeInput::getInputFluidStack)
                            .filter(Objects::nonNull)
                            .findFirst();
                    inputFluidStack.ifPresent(stack -> {
                        Fluid fluid = stack.getFluid();
                        if (fluid != null && stack.amount > 0) {
                            fluidHandlerItem.fill(new FluidStack(fluid, maxCapacity), true);
                            subItems.add(copy);
                        }
                    });
                });
            }
            subItems.add(itemStack);
        }
    }
}
