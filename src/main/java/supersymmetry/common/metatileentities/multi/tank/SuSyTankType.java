package supersymmetry.common.metatileentities.multi.tank;

import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TITANIUM_STABLE;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.STEEL_SOLID;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST;
import static gregtech.common.blocks.BlockSteamCasing.SteamCasingType.WOOD_WALL;
import static gregtech.common.blocks.MetaBlocks.METAL_CASING;
import static gregtech.common.blocks.MetaBlocks.STEAM_CASING;

import static gregtech.api.unification.material.Materials.Steel;
import static gregtech.api.unification.material.Materials.StainlessSteel;
import static gregtech.api.unification.material.Materials.Titanium;
import static gregtech.api.unification.material.Materials.TungstenSteel;
import static gregtech.api.unification.material.Materials.Wood;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import supersymmetry.api.fluids.SuSyFluidAttributes;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing2;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.function.Function;

public enum SuSyTankType {

    WOOD("wood", false, 18_750, 2,
        STEAM_CASING.getState(WOOD_WALL),
        Textures.WOOD_WALL,
        STEAM_CASING.getItemVariant(WOOD_WALL),
        Wood),
    STEEL("steel", false, 25_000, 4,
        METAL_CASING.getState(STEEL_SOLID),
        Textures.SOLID_STEEL_CASING,
        METAL_CASING.getItemVariant(STEEL_SOLID),
        Steel),
    MONEL("monel_400", true, 27_500, 8, 
        SuSyBlocks.MULTIBLOCK_CASING_2.getState(BlockSuSyMultiblockCasing2.CasingType.MONEL_400_CASING), 
        SusyTextures.MONEL_400_CASING, 
        SuSyBlocks.MULTIBLOCK_CASING_2.getItemVariant(BlockSuSyMultiblockCasing2.CasingType.MONEL_400_CASING), 
        null),
    STAINLESS_STEEL("stainless_steel", true, 30_250, 16,
        METAL_CASING.getState(STAINLESS_CLEAN),
        Textures.CLEAN_STAINLESS_STEEL_CASING,
        METAL_CASING.getItemVariant(STAINLESS_CLEAN),
        StainlessSteel),
    TITANIUM("titanium", true, 33_275, 32,
        METAL_CASING.getState(TITANIUM_STABLE),
        Textures.STABLE_TITANIUM_CASING,
        METAL_CASING.getItemVariant(TITANIUM_STABLE),
        Titanium),
    TUNGSTEN_STEEL("tungsten_steel", false, 36_603, 64,
        METAL_CASING.getState(TUNGSTENSTEEL_ROBUST),
        Textures.ROBUST_TUNGSTENSTEEL_CASING,
        METAL_CASING.getItemVariant(TUNGSTENSTEEL_ROBUST),
        TungstenSteel);

    public final String Material;
    public final boolean baseProof;
    public final int kLPerBlock;
    public final int maxAirBlocks;
    public final IBlockState casingState;
    public final ICubeRenderer baseTexture;
    public final ItemStack casingStack;
    public final Material recipeMaterial;

    SuSyTankType(String Material, boolean baseProof, int kLPerBlock, int maxAirBlocks, 
                 IBlockState casingState, ICubeRenderer baseTexture, ItemStack casingStack, Material recipeMaterial) {
        this.Material = Material;
        this.baseProof = baseProof;
        this.kLPerBlock = kLPerBlock;
        this.maxAirBlocks = maxAirBlocks;
        this.casingState = casingState;
        this.baseTexture = baseTexture;
        this.casingStack = casingStack;
        this.recipeMaterial = recipeMaterial;
    }

    public FluidPipeProperties getPipeProperties() {
        Material mat = this.recipeMaterial;
        if (mat == null && this.Material != null) {
            mat = GregTechAPI.materialManager.getMaterial(this.Material);
            if (mat == null) mat = GregTechAPI.materialManager.getMaterial("susy:" + this.Material);
        }
        return mat != null ? mat.getProperty(PropertyKey.FLUID_PIPE) : null;
    }

    private <T> T getPipeProp(Function<FluidPipeProperties, T> getter, T fallback) {
        FluidPipeProperties pipe = getPipeProperties();
        return pipe != null ? getter.apply(pipe) : fallback;
    }

    public int getMaxTemperature()  { return getPipeProp(FluidPipeProperties::getMaxFluidTemperature, 300); }
    public boolean isGasProof()     { return getPipeProp(FluidPipeProperties::isGasProof, false); }
    public boolean isAcidProof()    { return getPipeProp(FluidPipeProperties::isAcidProof, false); }
    public boolean isCryoProof()    { return getPipeProp(FluidPipeProperties::isCryoProof, false); }
    public boolean isPlasmaProof()  { return getPipeProp(FluidPipeProperties::isPlasmaProof, false); }

    public PropertyFluidFilter createFluidFilter() {
        PropertyFluidFilter filter = new PropertyFluidFilter(
            getMaxTemperature(), isGasProof(), isAcidProof(), isCryoProof(), isPlasmaProof()
        );
        filter.setCanContain(SuSyFluidAttributes.BASE, this.baseProof);
        return filter;
    }
}