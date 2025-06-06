package supersymmetry.api.metatileentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.pattern.PatternMatchContext;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;


/** This class is used to render a non-consumable input fluid according to given pattern.
 * It was designed for use in {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityClarifier} and {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityFrothFlotationTank}.
 * Expects a recipemap with a fluid input and at least one consumable fluid input in every recipe.
 * @author h3tR / RMI
 */
public abstract class FluidRenderRecipeMapMultiBlock extends CachedPatternRecipeMapMultiblock {

    private static final Cuboid6 FLUID_RENDER_CUBOID = new Cuboid6(0,0,0,1,3/16F,1);

    public static final int UPDATE_FLUID_INFO = GregtechDataCodes.assignId();
    public static final int CHANGE_FLUID_RENDER_STATUS = GregtechDataCodes.assignId();

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite fluidTexture;
    @SideOnly(Side.CLIENT)
    protected int fluidColor;
    @SideOnly(Side.CLIENT)
    protected boolean renderFluid = false;



    public FluidRenderRecipeMapMultiBlock(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, boolean perfectOC) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, perfectOC){
            @Override
            protected void setupRecipe(Recipe recipe) {
                super.setupRecipe(recipe);
                updateRenderInfo(recipe);
            }
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        updateRenderInfo(recipeMapWorkable.getPreviousRecipe());
    }

    public void updateRenderInfo(Recipe recipe){
        if(recipe == null) return;
        Optional<Fluid> fluid = getFluidToRender(recipe);
        fluid.ifPresent(fluidToRender -> {
            this.writeCustomData(UPDATE_FLUID_INFO, buf -> {
                buf.writeInt(fluidToRender.getColor());
                buf.writeResourceLocation(fluidToRender.getStill());
            });
            this.writeCustomData(CHANGE_FLUID_RENDER_STATUS, buf -> buf.writeBoolean(true));
        });
        //ifPresentOrElse doesn't exist in this java version. Absolutely deplorable
        if(!fluid.isPresent())
            this.writeCustomData(CHANGE_FLUID_RENDER_STATUS, buf -> buf.writeBoolean(false));
    }


    protected Optional<Fluid> getFluidToRender(Recipe recipe){
        //filters the input fluids for a consumed input fluid (ignores fluid flotation agents in case of froth flotation)
        return recipe.getFluidInputs().stream().filter(fluidInput -> !fluidInput.isNonConsumable()).findFirst().map(GTRecipeInput::getInputFluidStack).map(FluidStack::getFluid);
    }


    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if(this.isActive() && renderFluid)
            for(Vec3i pos: cachedPattern)
                renderFluid(pos, renderState, translation);

    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        //Can't use a switch statment here as the dataIds aren't constants.
        if(dataId == UPDATE_FLUID_INFO) {
            this.fluidColor = buf.readInt();
            this.fluidTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(buf.readResourceLocation().toString());
        }else if(dataId == CHANGE_FLUID_RENDER_STATUS) {
            this.renderFluid = buf.readBoolean();
        }
    }

    private void renderFluid(Vec3i offset, CCRenderState renderState, Matrix4 translation) {
        IVertexOperation[] fluid_render_pipeline = {
                new IconTransformation(fluidTexture),
                new ColourMultiplier(fluidColor)
        };
        Textures.renderFace(renderState, translation.copy().translate(Vector3.fromVec3i(offset)), fluid_render_pipeline, EnumFacing.UP, FLUID_RENDER_CUBOID, fluidTexture, BlockRenderLayer.CUTOUT_MIPPED);
    }

}
