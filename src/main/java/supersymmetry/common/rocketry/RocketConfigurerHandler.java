package supersymmetry.common.rocketry;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import supersymmetry.common.item.SuSyMetaItems;

/**
 * A single slot holding a rocket configurer, shared by everything that can
 * stamp a mission list onto a rocket: the rocket programmer, and the optional
 * configurer slot on the launch pads.
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
     * A configurer that was never written to carries no missions, so it is treated
     * as an empty slot.
     */
    public boolean isEmpty() {
        return getStackInSlot(0).isEmpty() || getStackInSlot(0).getTagCompound() == null;
    }

    public Optional<RocketConfiguration> read() {
        if (isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new RocketConfiguration(getStackInSlot(0).getTagCompound()));
    }

    public boolean program(int startingDimension, NBTTagCompound rocketData) {
        return read()
                .map(config -> {
                    boolean withinBudget = config.setBudget(startingDimension, RocketConfiguration.DEFAULT_BUDGET);
                    config.applyTo(rocketData);
                    return withinBudget;
                })
                .orElse(true);
    }
}
