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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockEvaporationBed;
import supersymmetry.common.blocks.BlockMultiblockTank;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityEvaporationPool extends RecipeMapMultiblockController {
    public MetaTileEntityEvaporationPool(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.EVAPORATION_POOL);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityEvaporationPool(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "                                ")
                .aisle("ABBBBBBBBBBBBBBBBBBBBBBBBBBBBBBA", " BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABCCCCCCCCCCCCCCCCCCCCCCCCCCCCBA", " B                            B ")
                .aisle("ABBBBBBBBBBBBBBBBBBBBBBBBBBBBBBA", " BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB ")
                .aisle("AAAAAAAAAAAAAAAASAAAAAAAAAAAAAAA", "                                ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT))
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('B', blocks(Blocks.DIRT))
                .where('C', states(SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT)))
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
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
