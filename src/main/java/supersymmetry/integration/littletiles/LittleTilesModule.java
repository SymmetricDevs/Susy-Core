package supersymmetry.integration.littletiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.LittleStorage;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import supersymmetry.Supersymmetry;
import supersymmetry.api.capability.impl.InaccessibleHandlerDelegate;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.modules.SuSyModules;

@GregTechModule(
                moduleID = SuSyModules.MODULE_LITTLETILES,
                containerID = Supersymmetry.MODID,
                modDependencies = "littletiles",
                name = "SuSy LittleTiles Integration",
                description = "SuSy LittleTiles Integration Module")
public class LittleTilesModule extends IntegrationSubmodule {

    @SubscribeEvent
    public static void registerCapabilityItems(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TileEntityLittleTiles ltte) {
            event.addCapability(SuSyUtility.susyId("lt_inventory_wrapper"), new LTInvWrapper(ltte));
        }
    }

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(LittleTilesModule.class);
    }

    public static class LTInvWrapper implements ICapabilityProvider {

        private final TileEntityLittleTiles ltte;
        private IItemHandler handler;

        public LTInvWrapper(TileEntityLittleTiles ltte) {
            this.ltte = ltte;
        }

        public IItemHandler getHandler() {
            if (this.handler == null) {
                List<IItemHandler> subHandlers = new ArrayList<>();
                ltte.groups().forEach(parent -> {
                    if (parent.isStructure()) {
                        try {
                            LittleStructure structure = parent.getStructure();
                            if (structure instanceof LittleStorage storage) {
                                subHandlers.add(new InvWrapper(storage.inventory));
                            }
                        } catch (Exception ignored) {
                            /* Do Nothing */
                        }
                    }
                });
                this.handler = new InaccessibleHandlerDelegate(new ItemHandlerList(subHandlers));
            }
            return handler;
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            return facing == null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        }

        @Override
        public @Nullable <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            if (facing == null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getHandler());
            }
            return null;
        }
    }
}
