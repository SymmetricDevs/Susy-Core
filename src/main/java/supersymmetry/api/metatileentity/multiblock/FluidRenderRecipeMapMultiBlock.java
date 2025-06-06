package supersymmetry.api.metatileentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;
import java.util.function.Consumer;


/** This class is used to render a non-consumable input fluid according to given pattern.
 * It was designed for use in {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityClarifier} and {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityFrothFlotationTank}.
 * Expects a recipemap with a fluid input and at least one consumable fluid input in every recipe.
 * @author h3tR / RMI
 */
public abstract class FluidRenderRecipeMapMultiBlock extends RecipeMapMultiblockController {

    private static final Cuboid6 FLUID_RENDER_CUBOID = new Cuboid6(0,0,0,1,3/16F,1);
    private static final int FLUID_RENDERER_DISCRIMINATOR = 0x4652;  //Spells out FR in hex (for fluid renderer)
    private static final int FLUID_RENDER_STATUS_DISCRIMINATOR = FLUID_RENDERER_DISCRIMINATOR + 1;

    private Recipe previousRecipe;
    protected TextureAtlasSprite fluidTexture;
    protected int fluidColor;
    protected boolean renderFluid = false;

    private final String[] fluidPattern;
    private final Vec3i patternOffset;

    public FluidRenderRecipeMapMultiBlock(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, String[] fluidPattern, Vec3i patternOffset) {
        super(metaTileEntityId, recipeMap);
        this.patternOffset = patternOffset;
        this.fluidPattern = fluidPattern;
    }

    @Override
    public void update() {
        super.update();
        if(this.isActive() && !getWorld().isRemote){
                //Updates fluid texture and color when recipe is changed
            Recipe currentRecipe = getRecipeMapWorkable().getPreviousRecipe();
            if((currentRecipe != previousRecipe || getWorld().getMinecraftServer().getTickCounter() % 100 == 0) && currentRecipe != null) { //update on recipe change and every 5 seconds
                Optional<Fluid> fluid = getFluidToRender(currentRecipe);
                fluid.ifPresent(fluidToRender -> {
                    this.writeCustomData(FLUID_RENDERER_DISCRIMINATOR, buf -> {
                        buf.writeInt(fluidToRender.getColor());
                        buf.writeResourceLocation(fluidToRender.getStill());
                    });
                    this.writeCustomData(FLUID_RENDER_STATUS_DISCRIMINATOR, buf -> {
                        buf.writeBoolean(true);
                    });
                    previousRecipe = currentRecipe;
                });
                //ifPresentOrElse doesn't exist in this java version. Absolutely deplorable
                if(!fluid.isPresent())
                    this.writeCustomData(FLUID_RENDER_STATUS_DISCRIMINATOR, buf -> {
                        buf.writeBoolean(false);
                    });
            }
        }
    }

    protected Optional<Fluid> getFluidToRender(Recipe currentRecipe){
        //filters the input fluids for a consumed input fluid (ignores fluid flotation agents in case of froth flotation)
        return currentRecipe.getFluidInputs().stream().filter(fluidInput -> !fluidInput.isNonConsumable()).findFirst().map(GTRecipeInput::getInputFluidStack).map(FluidStack::getFluid);
    }


    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if(this.isActive() && renderFluid)
            atEachFluidPos(pos -> renderFluid(pos, renderState, translation));
    }

    protected void atEachFluidPos(Consumer<Vec3i> consumer) {
        EnumFacing facing = this.getFrontFacing().getOpposite();
        for (int z = 0; z < fluidPattern.length; z++) {
            for (int x = 0; x < fluidPattern[z].length(); x++) {
                if(fluidPattern[z].charAt(x) == ' ') continue;

                int patternXOffset = x + patternOffset.getX();
                int patternZOffset = z + patternOffset.getZ();

                if(this.isFlipped ^ (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) ){
                    patternXOffset = -patternXOffset;
                }

                consumer.accept(new Vec3i(
                        patternZOffset*facing.getXOffset() + patternXOffset*facing.getZOffset(),
                            patternOffset.getY(),
                        patternXOffset*facing.getXOffset() + patternZOffset*facing.getZOffset()
                ));
            }
        }
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        switch (discriminator) {
            case FLUID_RENDERER_DISCRIMINATOR: {
                this.fluidColor = buf.readInt();
                this.fluidTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(buf.readResourceLocation().toString());
                break;
            }
            case FLUID_RENDER_STATUS_DISCRIMINATOR: {
                this.renderFluid = buf.readBoolean();
                break;
            }
        }
    }

    private void renderFluid(Vec3i offset, CCRenderState renderState, Matrix4 translation) {
        IVertexOperation[] fluid_render_pipeline = {
                new IconTransformation(fluidTexture),
                new ColourMultiplier(fluidColor)
        };
        Textures.renderFace(renderState, translation.copy().translate(Vector3.fromVec3i(offset)), fluid_render_pipeline, EnumFacing.UP, FLUID_RENDER_CUBOID, fluidTexture, BlockRenderLayer.CUTOUT_MIPPED);
    }


    //This should never be overwritten as it is not supported
    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
