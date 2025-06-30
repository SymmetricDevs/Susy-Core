package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.item.SuSyMetaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;


public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase {
    public DataStorageLoader master_blueprint = null;
    public List<Map<String,List<DataStorageLoader>>> components = new ArrayList<>();
    public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
       super(metaTileEntityId);
       master_blueprint = new DataStorageLoader(this, item -> {int meta = SuSyMetaItems.isMetaItem(item);return meta == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue;});
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCSMC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .where('M', maintenancePredicate())
                .where('S', selfPredicate())
                .where('A', air())
                .where('C', states(getCasingState()))
                .where('P', states(getComputerState()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    public IBlockState getComputerState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }
    @Override
    public ModularUI getModularUI(EntityPlayer entityPlayer) {
    
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private enum guiState {
       rocket_blueprint_assebler,
       rocket_stats,
       not_formed
    }


    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        int width = 400;
        int height = 200;
        
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.slot(master_blueprint, 0, width/2, height/2, GuiTextures.SLOT);
        if (master_blueprint.getStackInSlot(0).getCount() != 1) {
          builder.label(width/2, height/2+6, "susy.machine.rocket_simulator.master_blueprint.missing");
        } 
        builder.label(width/2, height/2+6, "susy.machine.rocket_simulator.master_blueprint");

        // builder.widget(new IndicatorImageWidget(174, 93, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
        //         .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
        //         .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.bindPlayerInventory(entityPlayer.inventory,width);
        return builder;
    }


    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
    }
}
