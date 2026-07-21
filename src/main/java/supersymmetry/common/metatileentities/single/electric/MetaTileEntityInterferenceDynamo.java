package supersymmetry.common.metatileentities.single.electric;

import java.util.List;
import java.util.Set;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import java.lang.reflect.Field;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

public class MetaTileEntityInterferenceDynamo extends TieredMetaTileEntity {

    private int currentRadius = 0;
    private static final Field PROGRESS_TIME_FIELD;

    //make problem cauers immune
    private static final Set<Class<? extends MetaTileEntity>> WHITELIST = Set.of(
            MetaTileEntityDustAgitator.class,
            MetaTileEntityDefoliator.class,
            MetaTileEntityFederationDropBeacon.class,
            MetaTileEntityHydrocarbonSaturator.class,
            MetaTileEntityInterferenceDynamo.class,
            MetaTileEntityToxicSpewer.class
    );

    public MetaTileEntityInterferenceDynamo(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityInterferenceDynamo(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("harmRadius", currentRadius);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        currentRadius = data.getInteger("harmRadius");
    }

    @Override
    public void update() {
        super.update();

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);

        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity()) return;
        if (getWorld().isRemote) return;

        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());

        drainInRadius(getWorld(), getPos(), currentRadius);

        if (currentRadius < 32) currentRadius++;
    }


    static {
        try {
            PROGRESS_TIME_FIELD = AbstractRecipeLogic.class.getDeclaredField("progressTime");
            PROGRESS_TIME_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find progressTime field in AbstractRecipeLogic", e);
        }
    }

    private void drainInRadius(World world, BlockPos center, int radius) {
        if (radius == 0) return;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos target = center.add(dx, dy, dz);
                    if (!world.isValid(target)) continue;
                    if (target.equals(center)) continue;

                    TileEntity te = world.getTileEntity(target);
                    if (!(te instanceof IGregTechTileEntity)) continue;

                    MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
                    if (mte == null) continue;

                    if (WHITELIST.contains(mte.getClass())) continue;

                    AbstractRecipeLogic logic = mte.getCapability(GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC, null);
                    if (
                            logic == null ||
                                    logic.getProgress() <= 0 ||
                                    !logic.consumesEnergy() || //exclude energy generators, no infinite power for you. Also handles things like radiator, hx, etc...
                                    // exclude steam and primitive
                                    logic instanceof PrimitiveRecipeLogic ||
                                    logic instanceof SteamMultiblockRecipeLogic ||
                                    logic instanceof RecipeLogicSteam ||
                                    logic instanceof BoilerRecipeLogic
                    ) continue;

                    try {
                        PROGRESS_TIME_FIELD.setInt(logic, 1);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Could not reset progressTime", e);
                    }
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.interference_dynamo.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.interference_dynamo.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.interference_dynamo.tooltip.description1"));
        tooltip.add(I18n.format("susy.machine.generic.tooltip.radius_warning"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
