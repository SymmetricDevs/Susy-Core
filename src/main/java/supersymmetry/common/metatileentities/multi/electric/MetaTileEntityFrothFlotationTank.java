package supersymmetry.common.metatileentities.multi.electric;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.metatileentity.multiblock.FluidRenderRecipeMapMultiBlock;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.particles.SusyParticleFrothBubble;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockMultiblockTank;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityFrothFlotationTank extends FluidRenderRecipeMapMultiBlock {

    private final static String[][] FLUID_PATTERN = { { "FFF", "F F", "FFF" } };
    private final static Vec3i PATTERN_OFFSET = new Vec3i(-1, 2, 2);

    public MetaTileEntityFrothFlotationTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.FROTH_FLOTATION, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFrothFlotationTank(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("   B   ", "   B   ", "   B   ", "       ", "       ")
                .aisle("  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ")
                .aisle(" AAAAA ", " ABBBA ", " ABBBA ", " ADDDA ", " A   A ")
                .aisle("BAAAAAB", "BABBBAB", "BABBBAB", " ADBDA ", " A E A ")
                .aisle(" AAAAA ", " ABBBA ", " ABBBA ", " ADDDA ", " A   A ")
                .aisle("  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ", "  AAA  ")
                .aisle("   B   ", "   B   ", "   S   ", "       ", "       ")
                .where('S', selfPredicate())
                .where('A',
                        states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN))
                                .setMinGlobalLimited(51)
                                .or(autoAbilities(true, true, true, true, true, true, true)))
                .where('B', states(MetaBlocks.BOILER_CASING.getState((BoilerCasingType.STEEL_PIPE))))
                .where('D',
                        states(SuSyBlocks.MULTIBLOCK_TANK.getState(BlockMultiblockTank.MultiblockTankType.FLOTATION)))
                .where('E',
                        states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where(' ', any())
                .build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc", new Object[0]));
    }

    @Override
    public void update() {
        super.update();
        if (this.isActive() && getWorld().isRemote && this.renderFluid) {
            renderParticles();
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderParticles() {
        Random rand = getWorld().rand;
        for (Vec3i offset : cachedPattern) {
            BlockPos pos = this.getPos().add(offset);
            Minecraft.getMinecraft().effectRenderer
                    .addEffect(new SusyParticleFrothBubble(getWorld(), pos.getX() + rand.nextDouble(),
                            pos.getY() + 2.5F / 16, pos.getZ() + rand.nextDouble(), 0, .005, 0, fluidColor));
        }
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.FROTH_FLOTATION_OVERLAY;
    }

    @Override
    protected String[][] getPattern() {
        return FLUID_PATTERN;
    }

    @Override
    protected Vec3i getPatternOffset() {
        return PATTERN_OFFSET;
    }
}
