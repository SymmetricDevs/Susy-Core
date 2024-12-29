package supersymmetry.api.metatileentity.multiblock;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.capability.ICoolantHandler;
import supersymmetry.api.capability.IFuelRodHandler;
import supersymmetry.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort;

@SuppressWarnings("InstantiationOfUtilityClass")
public class SuSyMultiblockAbilities {

    public static final MultiblockAbility<IItemHandlerModifiable> PRIMITIVE_IMPORT_ITEMS = new MultiblockAbility<>("primitive_import_items");
    public static final MultiblockAbility<IItemHandlerModifiable> PRIMITIVE_EXPORT_ITEMS = new MultiblockAbility<>("primitive_export_items");

    public static final MultiblockAbility<IFuelRodHandler> IMPORT_FUEL_ROD = new MultiblockAbility<>("import_fuel_rod");
    public static final MultiblockAbility<IItemHandlerModifiable> EXPORT_FUEL_ROD = new MultiblockAbility<>("export_fuel_rod");
    public static final MultiblockAbility<ICoolantHandler> IMPORT_COOLANT = new MultiblockAbility<>("import_coolant");
    public static final MultiblockAbility<ICoolantHandler> EXPORT_COOLANT = new MultiblockAbility<>("export_coolant");
    public static final MultiblockAbility<MetaTileEntityControlRodPort> CONTROL_ROD_PORT = new MultiblockAbility<>("control_rod_port");
}
