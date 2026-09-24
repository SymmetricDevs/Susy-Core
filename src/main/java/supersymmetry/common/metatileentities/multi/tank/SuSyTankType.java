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

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

import supersymmetry.api.fluids.SuSyFluidAttributes;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing2;
import supersymmetry.common.blocks.SuSyBlocks;

public enum SuSyTankType {

    WOOD("wood", 340, false, false, false, false, false,
     18_750, 2, STEAM_CASING.getState(WOOD_WALL), Textures.WOOD_WALL, STEAM_CASING.getItemVariant(WOOD_WALL), Wood),
    STEEL("steel", 1855, true, false, false, false, false,
     25_000, 4, METAL_CASING.getState(STEEL_SOLID), Textures.SOLID_STEEL_CASING, METAL_CASING.getItemVariant(STEEL_SOLID), Steel),
    MONEL("monel_400", 811, true, true, false, false, true,
     27_500, 8, SuSyBlocks.MULTIBLOCK_CASING_2.getState(BlockSuSyMultiblockCasing2.CasingType.MONEL_400_CASING), SusyTextures.MONEL_400_CASING, SuSyBlocks.MULTIBLOCK_CASING_2.getItemVariant(BlockSuSyMultiblockCasing2.CasingType.MONEL_400_CASING), null),
    STAINLESS_STEEL("stainless_steel", 2428, true, true, true, false, true,
     30_250, 16, METAL_CASING.getState(STAINLESS_CLEAN), Textures.CLEAN_STAINLESS_STEEL_CASING, METAL_CASING.getItemVariant(STAINLESS_CLEAN), StainlessSteel),
    TITANIUM("titanium", 2426, true, false, false, false, true,
     33_275, 32, METAL_CASING.getState(TITANIUM_STABLE), Textures.STABLE_TITANIUM_CASING, METAL_CASING.getItemVariant(TITANIUM_STABLE), Titanium),
    TUNGSTEN_STEEL("tungsten_steel", 3587, true, true, false, false, false,
     36_603, 64, METAL_CASING.getState(TUNGSTENSTEEL_ROBUST), Textures.ROBUST_TUNGSTENSTEEL_CASING, METAL_CASING.getItemVariant(TUNGSTENSTEEL_ROBUST), TungstenSteel);

    public final String Material;
    public final int maxTemperature;
    public final boolean gasProof;
    public final boolean acidProof;
    public final boolean cryoProof;
    public final boolean plasmaProof;
    public final boolean baseProof;

    public final int kLPerBlock;
    public final int maxAirBlocks;
    public final IBlockState casingState;
    public final ICubeRenderer baseTexture;
    public final ItemStack casingStack;
    public final Material recipeMaterial;

    SuSyTankType(String Material, int maxTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean baseProof,
                 int kLPerBlock, int maxAirBlocks, IBlockState casingState, ICubeRenderer baseTexture, ItemStack casingStack, Material recipeMaterial) {
        this.Material = Material;
        this.maxTemperature = maxTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.baseProof = baseProof;
        this.kLPerBlock = kLPerBlock;
        this.maxAirBlocks = maxAirBlocks;
        this.casingState = casingState;
        this.baseTexture = baseTexture;
        this.casingStack = casingStack;
        this.recipeMaterial = recipeMaterial;
    }

    public PropertyFluidFilter createFluidFilter() {
        PropertyFluidFilter filter = new PropertyFluidFilter(
            this.maxTemperature,
            this.gasProof,
            this.acidProof,
            this.cryoProof,
            this.plasmaProof
        );
    
        filter.setCanContain(SuSyFluidAttributes.BASE, this.baseProof);
    
        return filter;
    }
}