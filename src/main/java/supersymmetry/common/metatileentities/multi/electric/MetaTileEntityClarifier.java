package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
import java.util.function.Consumer;

public class MetaTileEntityClarifier extends RecipeMapMultiblockController {

    private Recipe previousRecipe;
    private TextureAtlasSprite fluidTexture;
    private int fluidColor;
    private boolean hasFluidSelected = false;
    private static final ThreadLocal<BlockRenderer.BlockFace> blockFaces = ThreadLocal
            .withInitial(BlockRenderer.BlockFace::new);
    private static final Cuboid6 FLUID_RENDER_CUBOID = new Cuboid6(0,0,0,1,3/16F,1);




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
                .where('F', frames(Materials.Steel))
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
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if(this.isActive() && this.fluidTexture != null)
            atEachTank(pos -> renderFluid(pos,renderState));

    }


    @Override
    public void update() {
        super.update();
        if(this.isActive()) {
            if (!getWorld().isRemote){
                //Updates fluid texture and color when recipe is changed
                Recipe currentRecipe = getRecipeMapWorkable().getPreviousRecipe();
                if((currentRecipe != previousRecipe || getWorld().getWorldTime() % 100 == 0) && currentRecipe != null) { //update on recipe change and every 5 seconds
                    //filters the input fluids for the consumed input fluid (not flotation agents)
                    currentRecipe.getFluidInputs().stream().filter(fluidInput -> !fluidInput.isNonConsumable()).findFirst().ifPresent(fluidInput -> {
                        System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH I HAVE PARTICLE AIDS");
                        Fluid fluid = fluidInput.getInputFluidStack().getFluid();
                        this.writeCustomData(0x434C4152, buf -> {
                            buf.writeInt(fluid.getColor());
                            buf.writeResourceLocation(fluid.getStill());
                        });
                        previousRecipe = currentRecipe;
                    });
                }
            }
        }
    }


    private final static String[] fluidPattern = {
            "     DDDD     ",
            "   DDDDDDDD   ",
            "  DDDDDDDDDD  ",
            " DDDDDDDDDDDD ",
            " DDDDDDDDDDDD ",
            "DDDDDDDDDDDDDD",
            "DDDDDD  DDDDDD",
            "DDDDDD  DDDDDD",
            "DDDDDDDDDDDDDD",
            " DDDDDDDDDDDD ",
            " DDDDDDDDDDDD ",
            "  DDDDDDDDDD  ",
            "   DDDDDDDD   ",
            "     DDDD     "
    };
    //the coordinates where the pattern is relative to the multi controller
    private final int patternXOffset = -11;
    private final static int patternYOffset = 1;

    private void atEachTank(Consumer<BlockPos> consumer) {
        EnumFacing facing = this.getFrontFacing().getOpposite();
        for (int y = 0; y < fluidPattern.length; y++) {
            for (int x = 0; x < fluidPattern[y].length(); x++) {
                if(fluidPattern[y].charAt(x) == ' ') continue;
                int patternX = (x + patternXOffset);
                int patternY = (y + patternYOffset);
                if (this.isFlipped)
                    patternX = -patternX;
                consumer.accept(getPos().add(
                        patternY * facing.getXOffset() + patternX * facing.getZOffset(),
                        1,
                        patternX * facing.getXOffset() + patternY * facing.getZOffset()
                ));
            }
        }
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == 0x434C4152) { //Spells out CLAR in hex (for clarifier)
            this.fluidColor = buf.readInt();
            this.fluidTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(buf.readResourceLocation().toString());
        }
    }


    private void renderFluid(Vec3i pos, CCRenderState renderState){
        IVertexOperation[] fluid_render_pipeline = {
                new Translation(pos),
                new IconTransformation(fluidTexture),
                new ColourMultiplier(fluidColor)
        };

        BlockRenderer.BlockFace blockFace = blockFaces.get();
        blockFace.loadCuboidFace(FLUID_RENDER_CUBOID, EnumFacing.UP.getIndex());
        renderState.setPipeline(blockFace, 0, blockFace.verts.length, fluid_render_pipeline);
        renderState.render();
    }
}
