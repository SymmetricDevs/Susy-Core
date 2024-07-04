package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockMultiblockTank;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityClarifier extends RecipeMapMultiblockController {
    public MetaTileEntityClarifier(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.CLARIFIER);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityClarifier(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("      AAAA      ", "      AAAA      ", "      AAAA      ", "                ")
                .aisle("    AAAAAAAA    ", "    AADDDDAA    ", "    AA    AA    ", "                ")
                .aisle("   AAAAAAAAAA   ", "   ADDDDDDDDA   ", "   A        A   ", "                ")
                .aisle("  AAAAAAAAAAAA  ", "  ADDDDDDDDDDA  ", "  A          A  ", "                ")
                .aisle(" AAAAAAAAAAAAAA ", " ADDDDDDDDDDDDA ", " A            A ", "                ")
                .aisle(" AAAAAAAAAAAAAA ", " ADDDDDDDDDDDDA ", " A            A ", "                ")
                .aisle("AAAAAAAAAAAAAAAA", "ADDDDDDDDDDDDDDA", "A              A", "                ")
                .aisle("AAAAAAAAAAAAAAAA", "ADDDDDDBBDDDDDDA", "A      BB      A", "       EE       ")
                .aisle("AAAAAAAAAAAAAAAA", "ADDDDDDBBDDDDDDA", "A      BBF     A", "       EE       ")
                .aisle("AAAAAAAAAAAAAAAA", "ADDDDDDDDDDDDDDA", "A       FFF    A", "                ")
                .aisle(" AAAAAAAAAAAAAA ", " ADDDDDDDDDDDDA ", " A       FFF  A ", "                ")
                .aisle(" AAAAAAAAAAAAAA ", " ADDDDDDDDDDDDA ", " A        FFF A ", "                ")
                .aisle("  AAAAAAAAAAAA  ", "  ADDDDDDDDDDA  ", "  A        FFA  ", "                ")
                .aisle("   AAAAAAAAAC   ", "   ADDDDDDDDA   ", "   A        A   ", "                ")
                .aisle("    AAAAAAAAC   ", "    AADDDDAA    ", "    AA    AA    ", "                ")
                .aisle("      AAAA BBB  ", "      AAAA BSB  ", "      AAAA      ", "                ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)).setMinGlobalLimited(250)
                        .or(autoAbilities()))
                .where('B', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)))
                .where('C', states(MetaBlocks.BOILER_CASING.getState((BoilerCasingType.STEEL_PIPE))))
                .where('D', states(SuSyBlocks.MULTIBLOCK_TANK.getState(BlockMultiblockTank.MultiblockTankType.CLARIFIER)))
                .where('E', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('F', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where(' ', any())
                .build();
    }
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc", new Object[0]));
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.CLARIFIER_OVERLAY;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
