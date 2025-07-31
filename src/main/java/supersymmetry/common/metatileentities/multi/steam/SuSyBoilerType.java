package supersymmetry.common.metatileentities.multi.steam;

import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;

import static gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType.BRONZE_PIPE;
import static gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType.STEEL_PIPE;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.BRONZE_FIREBOX;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.BRONZE_BRICKS;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.STEEL_SOLID;
import static gregtech.common.blocks.MetaBlocks.*;

public enum SuSyBoilerType {
    BRONZE(1536, 1200, 1,
            METAL_CASING.getState(BRONZE_BRICKS),
            BOILER_FIREBOX_CASING.getState(BRONZE_FIREBOX),
            BOILER_CASING.getState(BRONZE_PIPE),
            Textures.BRONZE_PLATED_BRICKS,
            Textures.BRONZE_FIREBOX,
            Textures.BRONZE_FIREBOX_ACTIVE,
            Textures.LARGE_BRONZE_BOILER),

    STEEL(3072, 1800, 1.5,
            METAL_CASING.getState(STEEL_SOLID),
            BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX),
            BOILER_CASING.getState(STEEL_PIPE),
            Textures.SOLID_STEEL_CASING,
            Textures.STEEL_FIREBOX,
            Textures.STEEL_FIREBOX_ACTIVE,
            Textures.LARGE_STEEL_BOILER);

    // Workable Data
    private final int steamPerTick;
    private final int ticksToBoiling;

    private final double efficiency;
    // Structure Data
    public final IBlockState casingState;
    public final IBlockState fireboxState;
    public final IBlockState pipeState;

    // Rendering Data
    public final ICubeRenderer casingRenderer;
    public final ICubeRenderer fireboxIdleRenderer;
    public final ICubeRenderer fireboxActiveRenderer;
    public final ICubeRenderer frontOverlay;

    SuSyBoilerType(int steamPerTick, int ticksToBoiling, double efficiency,
               IBlockState casingState,
               IBlockState fireboxState,
               IBlockState pipeState,
               ICubeRenderer casingRenderer,
               ICubeRenderer fireboxIdleRenderer,
               ICubeRenderer fireboxActiveRenderer,
               ICubeRenderer frontOverlay) {
        this.steamPerTick = steamPerTick;
        this.ticksToBoiling = ticksToBoiling;
        this.efficiency = efficiency;

        this.casingState = casingState;
        this.fireboxState = fireboxState;
        this.pipeState = pipeState;

        this.casingRenderer = casingRenderer;
        this.fireboxIdleRenderer = fireboxIdleRenderer;
        this.fireboxActiveRenderer = fireboxActiveRenderer;
        this.frontOverlay = frontOverlay;
    }

    public int steamPerTick() {
        return steamPerTick;
    }

    public int getTicksToBoiling() {
        return ticksToBoiling;
    }

    public int runtimeBoost(int ticks) {
        return (int) (efficiency * ticks);
    }

}
