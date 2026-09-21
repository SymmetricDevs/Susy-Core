package supersymmetry.common.metatileentities.multi.tank;

import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TITANIUM_STABLE;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.STEEL_SOLID;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST;
import static gregtech.common.blocks.MetaBlocks.METAL_CASING;
import static gregtech.common.blocks.MetaBlocks.PLANKS;
import static gregtech.common.blocks.wood.BlockGregPlanks.BlockType.TREATED_PLANK;

import static gregtech.api.unification.material.Materials.Steel;
import static gregtech.api.unification.material.Materials.StainlessSteel;
import static gregtech.api.unification.material.Materials.Titanium;
import static gregtech.api.unification.material.Materials.TungstenSteel;
import static gregtech.api.unification.material.Materials.Wood;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.client.renderer.textures.custom.ExtenderRender;
import supersymmetry.client.renderer.textures.custom.VatCasingRenderer;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyMetaBlocks;

import org.jetbrains.annotations.Nullable;

public enum SuSyTankType {

    WOOD("wood", 187_500, 2, PLANKS.getState(TREATED_PLANK), Textures.WOOD_WALL, PLANKS.getItemVariant(TREATED_PLANK), Wood),

    STEEL("steel", 250_000, 4, METAL_CASING.getState(STEEL_SOLID), Textures.SOLID_STEEL_CASING, METAL_CASING.getItemVariant(STEEL_SOLID), Steel),

    //MONEL("monel_400", 275_000, 8, SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.MONEL_500_CASING), SusyTextures.MONEL_500_CASING, SuSyBlocks.MULTIBLOCK_CASING.getItemVariant(BlockSuSyMultiblockCasing.CasingType.MONEL_500_CASING), null),

    STAINLESS_STEEL("stainless_steel", 302_500, 16, METAL_CASING.getState(STAINLESS_CLEAN), Textures.CLEAN_STAINLESS_STEEL_CASING, METAL_CASING.getItemVariant(STAINLESS_CLEAN), StainlessSteel),

    TITANIUM("titanium", 332_750, 32, METAL_CASING.getState(TITANIUM_STABLE), Textures.STABLE_TITANIUM_CASING, METAL_CASING.getItemVariant(TITANIUM_STABLE), Titanium),

    TUNGSTEN_STEEL("tungsten_steel", 366_025, 64, METAL_CASING.getState(TUNGSTENSTEEL_ROBUST), Textures.ROBUST_TUNGSTENSTEEL_CASING, METAL_CASING.getItemVariant(TUNGSTENSTEEL_ROBUST), TungstenSteel);

    public final String Material;
    public final int kLPerBlock;
    public final int maxAirBlocks;
    public final IBlockState casingState;
    public final ICubeRenderer baseTexture;
    public final ItemStack casingStack;
    public final Material recipeMaterial;

    SuSyTankType(String Material, int kLPerBlock, int maxAirBlocks, IBlockState casingState, ICubeRenderer baseTexture, ItemStack casingStack, Material recipeMaterial) {
        this.Material = Material;
        this.kLPerBlock = kLPerBlock;
        this.maxAirBlocks = maxAirBlocks;
        this.casingState = casingState;
        this.baseTexture = baseTexture;
        this.casingStack = casingStack;
        this.recipeMaterial = recipeMaterial;
    }
}
