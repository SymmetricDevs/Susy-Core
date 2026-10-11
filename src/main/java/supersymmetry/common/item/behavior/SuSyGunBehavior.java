package supersymmetry.common.item.behavior;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static supersymmetry.common.metatileentities.multi.electric.MetaTileEntityCargoDronePad.*;

public class SuSyGunBehavior implements IItemBehaviour {

    public boolean fire(Vec3d firePos, double pitch, double yaw) {
        if (this.currentAmmo >= 1) {
            currentAmmo--;

        }
        return false;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getSubCompound(TAG_ROOT);
            if (tag.hasKey(TAG_X) && tag.hasKey(TAG_Y) && tag.hasKey(TAG_Z)) {
                lines.add(I18n.format("susy.location_card.tooltip.coords") + "(" + tag.getInteger(TAG_X) + ", " +
                        tag.getInteger(TAG_Y) + ", " + tag.getInteger(TAG_Z) + ")");
            } else {
                lines.add(I18n.format("susy.location_card.tooltip.no_coords"));
            }
        } else {
            lines.add(I18n.format("susy.location_card.tooltip.no_coords"));
        }
    }

    public final int ammoCapacity;
    public final double muzzleVelocity;
    public final List<ItemStack> acceptedAmmo;
    public final double accuracy;
    public int currentAmmo = 0;

    public SuSyGunBehavior(int internalCapacity, double muzzleVelocity, List<ItemStack> acceptedAmmo, double accuracy) {
        this.ammoCapacity = internalCapacity;
        this.muzzleVelocity = muzzleVelocity;
        this.acceptedAmmo = acceptedAmmo;
        this.accuracy = accuracy;
    }

}
