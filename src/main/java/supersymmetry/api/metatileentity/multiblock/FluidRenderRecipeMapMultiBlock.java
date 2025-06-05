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
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fluids.Fluid;

import java.util.function.Consumer;


/** This class is used to render a non-consumable input fluid according to given pattern.
 * It was designed for use in {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityClarifier} and {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityFrothFlotationTank}
 * @author h3tR / RMI
 */
public abstract class FluidRenderRecipeMapMultiBlock extends RecipeMapMultiblockController {

    private static final Cuboid6 FLUID_RENDER_CUBOID = new Cuboid6(0,0,0,1,3/16F,1);

    private Recipe previousRecipe;
    protected TextureAtlasSprite fluidTexture;
    protected int fluidColor;

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
                if((currentRecipe != previousRecipe || getWorld().getWorldTime() % 100 == 0) && currentRecipe != null) { //update on recipe change and every 5 seconds
                    //filters the input fluids for the consumed input fluid (not flotation agents)
                    currentRecipe.getFluidInputs().stream().filter(fluidInput -> !fluidInput.isNonConsumable()).findFirst().ifPresent(fluidInput -> {
                        System.out.println("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH I HAVE PARTICLE AIDS");
                        Fluid fluid = fluidInput.getInputFluidStack().getFluid();
                        this.writeCustomData(0x4646, buf -> {
                            buf.writeInt(fluid.getColor());
                            buf.writeResourceLocation(fluid.getStill());
                        });
                        previousRecipe = currentRecipe;
                    });
                }
            }
        }


    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if(this.isActive() && this.fluidTexture != null)
            atEachTank(pos -> renderFluid(pos, renderState, translation));
    }

    protected void atEachTank(Consumer<Vec3i> consumer) {
        EnumFacing facing = this.getFrontFacing().getOpposite();
        for (int z = 0; z < fluidPattern.length; z++) {
            for (int x = 0; x < fluidPattern[z].length(); x++) {
                if(fluidPattern[z].charAt(x) == ' ') continue;
                consumer.accept(new Vec3i(
                        (z+patternOffset.getZ())*facing.getXOffset() + (x+patternOffset.getX())*facing.getZOffset(),
                            patternOffset.getY(),
                        (x+patternOffset.getX())*facing.getXOffset() + (z+patternOffset.getZ())*facing.getZOffset()
                ));
            }
        }
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == 0x4652) { //Spells out FR in hex (for fluid renderer)
            this.fluidColor = buf.readInt();
            this.fluidTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(buf.readResourceLocation().toString());
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
