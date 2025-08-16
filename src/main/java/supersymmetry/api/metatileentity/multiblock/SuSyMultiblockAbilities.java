package supersymmetry.api.metatileentity.multiblock;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.capability.IStrandProvider;

public class SuSyMultiblockAbilities {
    public static final MultiblockAbility<IItemHandlerModifiable> PRIMITIVE_IMPORT_ITEMS = new MultiblockAbility<>("primitive_import_items");
    public static final MultiblockAbility<IItemHandlerModifiable> PRIMITIVE_EXPORT_ITEMS = new MultiblockAbility<>("primitive_export_items");
    public static final MultiblockAbility<IStrandProvider> STRAND_IMPORT = new MultiblockAbility<>("strand_import");
    public static final MultiblockAbility<IStrandProvider> STRAND_EXPORT = new MultiblockAbility<>("strand_export");
    public static final MultiblockAbility<IItemHandlerModifiable> SCANNER = new MultiblockAbility<>("scanner");
}
