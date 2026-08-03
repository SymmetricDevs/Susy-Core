package supersymmetry.common.rocketry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.item.SuSyMetaItems;

/**
 * A single slot holding a rocket configurer, shared by everything that can stamp a mission list onto a rocket: the
 * rocket programmer, and the optional configurer slot on the launch pads.
 */
public class RocketConfigurerHandler extends GTItemStackHandler {

    public RocketConfigurerHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity, 1);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return SuSyMetaItems.ROCKET_CONFIGURER.getStackForm().isItemEqual(stack);
    }

    /**
     * A configurer that was never written to carries no missions, so it is treated as an empty slot.
     */
    public boolean isEmpty() {
        return getStackInSlot(0).isEmpty() || getStackInSlot(0).getTagCompound() == null;
    }

    /**
     * Writes the held mission list onto a rocket, pruned to what it can reach from the dimension it stands in.
     *
     * @return false if missions had to be dropped to fit the budget
     */
    public boolean program(EntityAbstractRocket rocket) {
        return program(rocket.getEntityData(), rocket.world.provider.getDimension());
    }

    /**
     * As {@link #program(EntityAbstractRocket)}, but onto a bare rocket NBT compound — the transporter erector
     * carries one of those around until the launch pad copies it into the rocket it spawns.
     *
     * @return false if missions had to be dropped to fit the budget
     */
    public boolean program(NBTTagCompound rocketNBT, int startingDimension) {
        if (isEmpty()) return true;
        RocketConfiguration config = new RocketConfiguration(getStackInSlot(0).getTagCompound());
        boolean withinBudget = config.setBudget(startingDimension, RocketConfiguration.DEFAULT_BUDGET);
        rocketNBT.setTag(EntityAbstractRocket.ROCKET_CONFIG_KEY, config.serialize());
        return withinBudget;
    }
}
