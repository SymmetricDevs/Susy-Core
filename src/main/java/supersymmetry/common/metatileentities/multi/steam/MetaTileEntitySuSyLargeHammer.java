package supersymmetry.common.metatileentities.multi.steam;

import gregtech.api.capability.impl.SteamMultiWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntitySuSyLargeHammer extends RecipeMapSteamMultiblockController {

    private static final int PARALLEL_LIMIT = 8;

    public MetaTileEntitySuSyLargeHammer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMap.getByName("forge_hammer"), CONVERSION_RATE);
        this.recipeMapWorkable = new SteamMultiWorkable(this, CONVERSION_RATE);
        this.recipeMapWorkable.setParallelLimit(PARALLEL_LIMIT);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySuSyLargeHammer(this.metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "X   X", "     ", "     ", "     ", "     ", "     ")
                .aisle("XXAXX", "X###X", "X#H#X", "XPSPX", " PYP ", "  Y  ", "  Y  ")
                .aisle("XXXXX", "X   X", "     ", "     ", "     ", "     ", "     ")
                .where('S', this.selfPredicate())
                .where('A', blocks(Blocks.ANVIL))
                .where('H', blocks(MetaBlocks.COMPRESSED_BLOCKS.get(20))) // FIXME: should be a steel block
                .where('Y', states(this.getCasingState()))
                .where('X', states(this.getCasingState()).or(autoAbilities()))
                .where('P', states(this.getPipeCasingState()))
                .where('#', air())
                .where(' ', any())
                .build();
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }
    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.steam_.duration_modifier"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", PARALLEL_LIMIT));
        tooltip.add(TooltipHelper.BLINKING_ORANGE + I18n.format("gregtech.multiblock.require_steam_parts"));
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public int getItemOutputLimit() {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FORGE_HAMMER_OVERLAY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (isActive()) {
            VanillaParticleEffects.RANDOM_SPARKS.runEffect(this);
        }
    }

    // TODO: add jei info tab
}
