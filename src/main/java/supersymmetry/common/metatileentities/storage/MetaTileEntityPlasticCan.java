package supersymmetry.common.metatileentities.storage;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.mixins.gregtech.MetaTileEntityDrumAccessor;

public class MetaTileEntityPlasticCan extends MetaTileEntityDrum {

    private final MetaTileEntityDrumAccessor self;

    public MetaTileEntityPlasticCan(ResourceLocation metaTileEntityId, @NotNull Material material, int tankSize) {
        super(metaTileEntityId, material, tankSize);
        this.self = (MetaTileEntityDrumAccessor) this;
    }

    public MetaTileEntityPlasticCan(ResourceLocation metaTileEntityId, @NotNull IPropertyFluidFilter fluidFilter,
                                    int color, int tankSize) {
        super(metaTileEntityId, fluidFilter, false, color, tankSize);
        this.self = (MetaTileEntityDrumAccessor) this;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPlasticCan(this.metaTileEntityId, self.getFluidFilter(), self.getColor(),
                self.getTankSize());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier = new ColourMultiplier(
                ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(self.getColor()),
                        GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        SusyTextures.PLASTIC_CAN.render(renderState, translation, ArrayUtils.add(pipeline, multiplier),
                getFrontFacing());
        SusyTextures.PLASTIC_CAN_OVERLAY.render(renderState, translation, pipeline);
        if (self.isAutoOutput()) {
            Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        }
    }
}
