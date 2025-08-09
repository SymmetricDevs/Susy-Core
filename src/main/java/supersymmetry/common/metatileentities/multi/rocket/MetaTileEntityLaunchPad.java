package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityLaunchPad extends RecipeMapMultiblockController {
    public MetaTileEntityLaunchPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_LAUNCH_PAD);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLaunchPad(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("DDDAAAAAAAAADDD", "DDDAAAAAAAAADDD", "DDDDDDDDDDDDDDD")
                .aisle("DDAAACCCCCAAADD", "DDAAACCCCCAAADD", "DDDDDCCCCCDDDDD")
                .aisle("DAACC     CCAAD", "DAACC     CCAAD", "DDDCC     CCDDD")
                .aisle("AAC         CAA", "AAC         CAA", "DDC         CDD")
                .aisle("AAC         CAA", "AAC         CAA", "DDC         CDD")
                .aisle("AC           CA", "AC           CA", "DC           CD")
                .aisle("AC           CA", "AC           CA", "DC           CD")
                .aisle("AC           CA", "AC           CA", "DC           CD")
                .aisle("AC           CA", "AC           CA", "DC           CD")
                .aisle("AC           CA", "AC           CA", "DC           CD")
                .aisle("AAC         CAA", "AAC         CAA", "DDC         CDD")
                .aisle("AAC         CAA", "AAC         CAA", "DDC         CDD")
                .aisle("DAACC     CCAAD", "DAACC     CCAAD", "DDDCC     CCDDD")
                .aisle("DDAAACCCCCAAADD", "DDAAACCCCCAAADD", "DDDDDCCCCCDDDDD")
                .aisle("DDDAAAAAAAAADDD", "DDDAAAAAAAAADDD", "DDDDDDDSDDDDDDD")
                .where('A', any())
                .where(' ', air())
                .where('S', selfPredicate())
                .where('D', states(getFoundationState()).or(autoAbilities()))
                .where('C', states(getReinforcedFoundationState()))
                .build();
    }

    public IBlockState getFoundationState() {
        return SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION);
    }

    public IBlockState getReinforcedFoundationState() {
        return SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }
}
