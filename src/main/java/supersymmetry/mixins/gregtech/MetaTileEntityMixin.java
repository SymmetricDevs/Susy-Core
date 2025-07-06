package supersymmetry.mixins.gregtech;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.api.metatileentity.IMui2MetaTileEntity;
import supersymmetry.api.metatileentity.MetaTileEntityGuiFactory;

@Deprecated
@ScheduledForRemoval(inVersion = "Next CEu update")
@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityMixin implements IMui2MetaTileEntity {

    @Shadow
    public abstract World getWorld();

    @Shadow
    protected abstract boolean openGUIOnRightClick();

    @Inject(method = "onRightClick", at = @At("HEAD"), cancellable = true)
    private void onRightClickMUI(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        if (useMui2() && !playerIn.isSneaking() && openGUIOnRightClick()) {
            if (getWorld() != null && !getWorld().isRemote) {
                MetaTileEntityGuiFactory.open(playerIn, (MetaTileEntity & IGuiHolder<PosGuiData>) (Object) this);
            }
            cir.setReturnValue(true);
        }
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public boolean useMui2() {
        return false;
    }
}
