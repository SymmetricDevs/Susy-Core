package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockMultiblockTank;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityFrothFlotationTank extends RecipeMapMultiblockController {

    private Recipe previousRecipe;
    private TextureAtlasSprite fluidTexture;
    private int fluidColor;
    private static final ThreadLocal<BlockRenderer.BlockFace> blockFaces = ThreadLocal
            .withInitial(BlockRenderer.BlockFace::new);
    private static final Cuboid6 FLUID_RENDER_CUBOID = new Cuboid6(0,0,0,1,1/16F,1);

    public MetaTileEntityFrothFlotationTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.FROTH_FLOTATION);
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
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN)).setMinGlobalLimited(51)
                        .or(autoAbilities(true, true, true, true, true, true, true)))
                .where('B', states(MetaBlocks.BOILER_CASING.getState((BoilerCasingType.STEEL_PIPE))))
                .where('D', states(SuSyBlocks.MULTIBLOCK_TANK.getState(BlockMultiblockTank.MultiblockTankType.FLOTATION)))
                .where('E', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
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
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if(this.isActive()) {
            if (this.fluidTexture == null)
                return;
            EnumFacing facing = this.getFrontFacing().getOpposite();
            //I'm not sure if hardcoding this pattern is the best approach, but it is surely the easiest.
            for (int x = -1; x < 2; x++) {
                for (int y = 2; y < 5; y++) {
                    if(x == 0 && y == 3) continue; //center block doesn't have fluid in it.
                    int xPos = y*facing.getXOffset() + x*facing.getZOffset();
                    int zPos = x*facing.getXOffset() + y*facing.getZOffset();
                    renderFluid(new Vec3i(xPos, 2, zPos),renderState);
                }
            }

        }
        super.renderMetaTileEntity(renderState, translation, pipeline);
    }


    @Override
    public void update() {
        super.update();
        if(this.isActive() && !getWorld().isRemote) {
            //Updates fluid texture and color when recipe is changed
            if(getRecipeMapWorkable().getPreviousRecipe() != previousRecipe && getRecipeMapWorkable().getPreviousRecipe() != null) {
                //filters the input fluids for the consumed input fluid (not flotation agents)
                getRecipeMapWorkable().getPreviousRecipe().getFluidInputs().stream().filter(fluidInput -> !fluidInput.isNonConsumable()).findFirst().ifPresent(fluidInput -> {
                    Fluid fluid = fluidInput.getInputFluidStack().getFluid();
                    this.writeCustomData(0x4646, buf -> {
                        buf.writeInt(fluid.getColor());
                        buf.writeResourceLocation(fluid.getStill());
                    });
                    previousRecipe = getRecipeMapWorkable().getPreviousRecipe();
                });
            }
        }
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == 0x4646) { //Spells out FF in hex (for froth flotation)
            this.fluidColor = buf.readInt();
            this.fluidTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(buf.readResourceLocation().toString());
        }
    }


    private void renderFluid(Vec3i pos, CCRenderState renderState){
        IVertexOperation[] A_pipeline = {
                new Translation(this.getPos().add(pos)),
                new IconTransformation(fluidTexture),
                new ColourMultiplier(fluidColor)
        };
        BlockRenderer.BlockFace blockFace = blockFaces.get();
        blockFace.loadCuboidFace(FLUID_RENDER_CUBOID, EnumFacing.UP.getIndex());
        renderState.setPipeline(blockFace, 0, blockFace.verts.length, A_pipeline);
        renderState.render();
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.FROTH_FLOTATION_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
