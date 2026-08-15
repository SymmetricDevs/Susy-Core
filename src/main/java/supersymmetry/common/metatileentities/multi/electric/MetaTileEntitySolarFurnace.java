package supersymmetry.common.metatileentities.multi.electric;

import static gregtech.api.metatileentity.MetaTileEntityHolder.TRACKED_TICKS;
import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.*;
import static supersymmetry.common.blocks.BlockEpoxySolarFurnaceMirror.MIRROR_SIDES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.SolarFurnaceMinPowerProperty;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.common.util.RecipeCheckUtils;

public class MetaTileEntitySolarFurnace extends RecipeMapMultiblockController {

    public static final int WATTS_PER_HELIOSTAT = 2000;
    public int numValidHeliostats = 0;
    public int currentPower = 0;
    public boolean hasEnoughPower = false;

    private final int[] recipeSpeedStats = new int[TRACKED_TICKS];
    private int statsIndex = 0;

    public MetaTileEntitySolarFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SOLAR_FURNACE_RECIPES);
        this.recipeMapWorkable = new SolarFurnaceRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySolarFurnace(metaTileEntityId);
    }

    @NotNull @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                // spotless:off
                .aisle("      RRR      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("   RRRRRRRRR   ", "     RRRRR     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle(" RRRRRRRRRRRRR ", "   RRRRRRRRR   ", "    RRRRRRR    ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRRRRRRRRRRR", " RRRRRRRRRRRRR ", "  RRRRRRRRRRR  ", "     RRRRR     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRRRRRRRRRRR", "RRRRRRRRRRRRRRR", " RRRRRRRRRRRRR ", "   RRRRRRRRR   ", "     RRRRR     ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRRRRRRRRRRR", "RRRRRRBBARRRRRR", "RRRRRBBBAARRRRR", " RRRRBBBAARRRR ", "   RRDDDCCRR   ", "      DDC      ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRBBBAARRRRR", "RRRRBB###AARRRR", "RRRBB#####AARRR", "RRRBB#####AARRR", "  RDD#####CCR  ", "    DD###CC    ", "    DDDDCCC    ", "      DDC      ", "               ", "               ", "               ")
                .aisle("RRRRB#####ARRRR", "RRBB#######AARR", "RBB#########AAR", "RBB#########AAR", " DD#########CC ", "  DD#######CC  ", "   D#######C   ", "   DDD###CCC   ", "      DDC      ", "      DDC      ", "               ")
                .aisle("RRBB#######AARR", "RB###########AR", "B#############A", "B#############A", "D#############C", " D###########C ", " DD#########CC ", "  D#########C  ", "  DDDD###CCCC  ", "    DD###CC    ", "      DDC      ")
                .aisle("RB###########AR", "B#############A", "###############", "###############", "###############", "D#############C", "D#############C", " D###########C ", " D###########C ", "  DD#######CC  ", "    DD###CC    ")
                .aisle("B#############A", "###############", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#######I#######", "###############", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("######III######", "#######I#######", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#####IIIII#####", "######IRI######", "#######U#######", "#######X#######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("##### III #####", "###### I ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#####  S  #####", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "H#H#H#   #H#H#H", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "H#H#H#H H#H#H#H", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "H#H#H#H H#H#H#H", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "H#H#H#H#H#H#H#H", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "H#H#H#H#H#H#H#H", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "H#H#H#H#H#H#H#H", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", " #H#H#H#H#H#H# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", " #H#H#H#H#H#H# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "  H#H#H#H#H#H  ", "    #######    ")
                //spotless:on
                .where('S', selfPredicate())
                .where('R', states(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.HIGHLAND))
                        .or(states(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND)))
                        .or(states(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.KREEP))))
                .where('I', states(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.HIGHLAND))
                        .or(states(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND)))
                        .or(states(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.KREEP)))
                        .or(autoAbilities(false, true, true,
                                true, true, true, false)))
                .where('H', heliostat(this).or(air()))
                .where('#', air())
                .where('X', redirectingMirrorOrientation())
                .where('U',
                        states(SuSyBlocks.SOLAR_FURNACE_CRUCIBLE
                                .getState(BlockSolarFurnaceCrucible.SolarFurnaceCrucibleType.DEFAULT)))
                .where(' ', any())
                .where('A', epoxyMirror(this, EnumMirrorSides.TOP_RIGHT)
                        .or(steelMirror(this, EnumMirrorSides.TOP_RIGHT)))
                .where('B', epoxyMirror(this, EnumMirrorSides.TOP_LEFT)
                        .or(steelMirror(this, EnumMirrorSides.TOP_LEFT)))
                .where('C', epoxyMirror(this, EnumMirrorSides.BOTTOM_RIGHT)
                        .or(steelMirror(this, EnumMirrorSides.BOTTOM_RIGHT)))
                .where('D', epoxyMirror(this, EnumMirrorSides.BOTTOM_LEFT)
                        .or(steelMirror(this, EnumMirrorSides.BOTTOM_LEFT)))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() { // FIXME: the rotations are all wrong no matter what is set
                                                           // here
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>(); // the alternator coils were for testing, the
                                                                      // issue seems to only happen with blocks that are
                                                                      // part of the structure already?
        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder()
                .where('S', SuSyMetaTileEntities.SOLAR_FURNACE, EnumFacing.SOUTH)
                .where('>', SuSyBlocks.HELIOSTAT.getDefaultState().withProperty(FACING, EnumFacing.NORTH))
                .where('X', SuSyBlocks.SOLAR_FURNACE_REDIRECTING_MIRROR.getState(
                        BlockSolarFurnaceRedirectingMirror.SolarFurnaceRedirectingMirrorType.DEFAULT, EnumFacing.SOUTH))
                .where('A',
                        SuSyBlocks.EPOXY_SOLAR_FURNACE_MIRROR
                                .getState(BlockEpoxySolarFurnaceMirror.EpoxyMirrorType.EPOXY)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.TOP_RIGHT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('B',
                        SuSyBlocks.EPOXY_SOLAR_FURNACE_MIRROR
                                .getState(BlockEpoxySolarFurnaceMirror.EpoxyMirrorType.EPOXY)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.TOP_LEFT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('C',
                        SuSyBlocks.EPOXY_SOLAR_FURNACE_MIRROR
                                .getState(BlockEpoxySolarFurnaceMirror.EpoxyMirrorType.EPOXY)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.BOTTOM_RIGHT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('D',
                        SuSyBlocks.EPOXY_SOLAR_FURNACE_MIRROR
                                .getState(BlockEpoxySolarFurnaceMirror.EpoxyMirrorType.EPOXY)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.BOTTOM_LEFT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('a',
                        SuSyBlocks.STEEL_SOLAR_FURNACE_MIRROR
                                .getState(BlockSteelSolarFurnaceMirror.SteelMirrorType.STEEL)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.TOP_RIGHT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('b',
                        SuSyBlocks.STEEL_SOLAR_FURNACE_MIRROR
                                .getState(BlockSteelSolarFurnaceMirror.SteelMirrorType.STEEL)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.TOP_LEFT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('c',
                        SuSyBlocks.STEEL_SOLAR_FURNACE_MIRROR
                                .getState(BlockSteelSolarFurnaceMirror.SteelMirrorType.STEEL)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.BOTTOM_RIGHT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('d',
                        SuSyBlocks.STEEL_SOLAR_FURNACE_MIRROR
                                .getState(BlockSteelSolarFurnaceMirror.SteelMirrorType.STEEL)
                                .withProperty(MIRROR_SIDES, EnumMirrorSides.BOTTOM_LEFT)
                                .withProperty(BlockDirectional.FACING, EnumFacing.SOUTH))
                .where('U',
                        SuSyBlocks.SOLAR_FURNACE_CRUCIBLE
                                .getState(BlockSolarFurnaceCrucible.SolarFurnaceCrucibleType.DEFAULT))
                .where('R', SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.HIGHLAND))
                .where('F', MetaBlocks.FRAMES.get(Materials.Aluminium).getBlock(Materials.Aluminium))
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.EV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.EV], EnumFacing.SOUTH)
                .where('i', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.EV], EnumFacing.SOUTH)
                .where('o', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.EV], EnumFacing.SOUTH)
                .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                        MetaBlocks.FRAMES.get(Materials.Aluminium).getBlock(Materials.Aluminium), EnumFacing.SOUTH);
        shapeInfo.add(baseBuilder.shallowCopy()
                // spotless:off
                .aisle("      RRR      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("   RRRRRRRRR   ", "     RRRRR     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle(" RRRRRRRRRRRRR ", "   RRRRRRRRR   ", "    RRRRRRR    ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRRRRRRRRRRR", " RRRRRRRRRRRRR ", "  RRRRRRRRRRR  ", "     RRRRR     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRRRRRRRRRRR", "RRRRRRRRRRRRRRR", " RRRRRRRRRRRRR ", "   RRRRRRRRR   ", "     RRRRR     ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRRRRRRRRRRR", "RRRRRRBBARRRRRR", "RRRRRBBBAARRRRR", " RRRRBBBAARRRR ", "   RRDDDCCRR   ", "      DDC      ", "               ", "               ", "               ", "               ", "               ")
                .aisle("RRRRRBBBAARRRRR", "RRRRBB###AARRRR", "RRRBB#####AARRR", "RRRBB#####AARRR", "  RDD#####CCR  ", "    DD###CC    ", "    DDDDCCC    ", "      DDC      ", "               ", "               ", "               ")
                .aisle("RRRRB#####ARRRR", "RRBB#######AARR", "RBB#########AAR", "RBB#########AAR", " DD#########CC ", "  DD#######CC  ", "   D#######C   ", "   DDD###CCC   ", "      DDC      ", "      DDC      ", "               ")
                .aisle("RRBB#######AARR", "RB###########AR", "B#############A", "B#############A", "D#############C", " D###########C ", " DD#########CC ", "  D#########C  ", "  DDDD###CCCC  ", "    DD###CC    ", "      DDC      ")
                .aisle("RB###########AR", "B#############A", "###############", "###############", "###############", "D#############C", "D#############C", " D###########C ", " D###########C ", "  DD#######CC  ", "    DD###CC    ")
                .aisle("B#############A", "###############", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#######R#######", "###############", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("######RRR######", "#######R#######", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#####IRRRO#####", "######iRo######", "#######U#######", "#######X#######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("##### RRR #####", "###### M ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#####  S  #####", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", ">#>#>#   #>#>#>", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", ">#>#>#> >#>#>#>", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", ">#>#>#> >#>#>#>", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", ">#>#>#>#>#>#>#>", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", ">#>#>#>#>#>#>#>", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", ">#>#>#>#>#>#>#>", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", " #>#>#>#>#>#># ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", " #>#>#>#>#>#># ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "  >#>#>#>#>#>  ", "    #######    ")
                .build());
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("      RRR      ", "               ", "               ", "               ", "               ","               ", "               ", "               ", "               ", "               ","               ")
                .aisle("   RRRRRRRRR   ", "     RRRRR     ", "               ", "               ", "               ","               ", "               ", "               ", "               ", "               ","               ")
                .aisle(" RRRRRRRRRRRRR ", "   RRRRRRRRR   ", "    RRRRRRR    ", "               ", "               ","               ", "               ", "               ", "               ", "               ","               ")
                .aisle("RRRRRRRRRRRRRRR", " RRRRRRRRRRRRR ", "  RRRRRRRRRRR  ", "     RRRRR     ", "               ","               ", "               ", "               ", "               ", "               ","               ")
                .aisle("RRRRRRRRRRRRRRR", "RRRRRRRRRRRRRRR", " RRRRRRRRRRRRR ", "   RRRRRRRRR   ", "     RRRRR     ","               ", "               ", "               ", "               ", "               ","               ")
                .aisle("RRRRRRRRRRRRRRR", "RRRRRRbbaRRRRRR", "RRRRRbbbaaRRRRR", " RRRRbbbaaRRRR ", "   RRdddccRR   ","      ddc      ", "               ", "               ", "               ", "               ","               ")
                .aisle("RRRRRbbbaaRRRRR", "RRRRbb###aaRRRR", "RRRbb#####aaRRR", "RRRbb#####aaRRR", "  Rdd#####ccR  ","    dd###cc    ", "    ddddccc    ", "      ddc      ", "               ", "               ","               ")
                .aisle("RRRRb#####aRRRR", "RRbb#######aaRR", "Rbb#########aaR", "Rbb#########aaR", " dd#########cc ","  dd#######cc  ", "   d#######c   ", "   ddd###ccc   ", "      ddc      ", "      ddc      ","               ")
                .aisle("RRbb#######aaRR", "Rb###########aR", "b#############a", "b#############a", "d#############c"," d###########c ", " dd#########cc ", "  d#########c  ", "  dddd###cccc  ", "    dd###cc    ","      ddc      ")
                .aisle("Rb###########aR", "b#############a", "###############", "###############", "###############","d#############c", "d#############c", " d###########c ", " d###########c ", "  dd#######cc  ","    dd###cc    ")
                .aisle("b#############a", "###############", "###############", "###############", "###############","###############", "###############", " ############# ", " ############# ", "  ###########  ","    #######    ")
                .aisle("#######R#######", "###############", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("######RRR######", "#######R#######", "###############", "###############", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#####IRRRO#####", "######iRo######", "#######U#######", "#######X#######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("##### RRR #####", "###### M ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("#####  S  #####", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", ">#>#>#   #>#>#>", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "####### #######", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", ">#>#>#> >#>#>#>", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "####### #######", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", ">#>#>#> >#>#>#>", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "###############", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", ">#>#>#>#>#>#>#>", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "###############", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", ">#>#>#>#>#>#>#>", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "###############", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", ">#>#>#>#>#>#>#>", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", " ############# ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", " #>#>#>#>#>#># ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", " ############# ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", " #>#>#>#>#>#># ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "  ###########  ", "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "  >#>#>#>#>#>  ", "    #######    ")
                .build());
                //spotless:on
        return shapeInfo;
    }

    public enum EnumMirrorSides implements IStringSerializable {

        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right");

        public static EnumMirrorSides fromInteger(int number) {
            switch (number) {
                case 1:
                    return TOP_RIGHT;
                case 2:
                    return BOTTOM_LEFT;
                case 3:
                    return BOTTOM_RIGHT;
                default:
                    return TOP_LEFT;
            }
        }

        private final String name;

        private EnumMirrorSides(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.FROST_PROOF_CASING;
    }

    @NotNull @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.SOLAR_FURNACE_OVERLAY;
    }

    protected List<BlockPos> heliostats;

    @Override
    protected void formStructure(PatternMatchContext context) {
        this.heliostats = context.getOrDefault("HeliostatPositions", new LinkedList<>());
        super.formStructure(context);
    }

    protected IBlockState redirectingMirrorState() {
        return SuSyBlocks.SOLAR_FURNACE_REDIRECTING_MIRROR
                .getState(BlockSolarFurnaceRedirectingMirror.SolarFurnaceRedirectingMirrorType.DEFAULT);
    }

    protected TraceabilityPredicate redirectingMirrorOrientation() {
        return SuSyPredicates.orientation(this, redirectingMirrorState(), RelativeDirection.BACK, FACING);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (getOffsetTimer() % 75 == 0 && isStructureFormed()) {
            numValidHeliostats = getNumOfValidHeliostats();
        }
        currentPower = WATTS_PER_HELIOSTAT * numValidHeliostats;
    }

    public int getNumOfValidHeliostats() {
        World world = this.getWorld();
        int i = 0;
        if (heliostats != null && !heliostats.isEmpty()) {
            for (BlockPos pos : heliostats) {
                if (world.getBlockState(pos).getBlock() == SuSyBlocks.HELIOSTAT && getWorld().canSeeSky(pos)) {
                    i++;
                }
            }
        }
        return i;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("susy.multiblock.solar_furnace.num_heliostats", numValidHeliostats));
        textList.add(new TextComponentTranslation("susy.multiblock.solar_furnace.current_power", currentPower / 1000));
        textList.add(new TextComponentTranslation("susy.multiblock.solar_furnace.average_speed", getAverageSpeed()));
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isStructureFormed() && this.isActive() && !hasEnoughPower) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                    "susy.multiblock.solar_furnace.low_power"));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.numValidHeliostats = 0;
        this.currentPower = 0;
        this.hasEnoughPower = false;
    }

    private void updateSpeedStats(int progress) {
        recipeSpeedStats[statsIndex] = progress;
        statsIndex = (statsIndex + 1) % TRACKED_TICKS;
    }

    public float getAverageSpeed() {
        return ((float) Arrays.stream(recipeSpeedStats).sum()) / TRACKED_TICKS;
    }

    public int getCurrentPower() {
        return currentPower;
    }

    public int getCurrentValidHeliostats() {
        return numValidHeliostats;
    }

    public class SolarFurnaceRecipeLogic extends MultiblockRecipeLogic {

        private int recipePower;
        private int heatBuffer = 0;
        private boolean isHalted;

        public SolarFurnaceRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            return super.checkRecipe(recipe) && RecipeCheckUtils.checkDimension(recipe, this.metaTileEntity) &&
                    recipe.hasProperty(SolarFurnaceMinPowerProperty.getInstance()) &&
                    recipe.getProperty(SolarFurnaceMinPowerProperty.getInstance(), 2147483647) <= currentPower;
        }

        @Override
        protected void setupRecipe(Recipe recipe) {
            super.setupRecipe(recipe);
            this.recipePower = recipe.getProperty(SolarFurnaceMinPowerProperty.getInstance(), 0);
            this.heatBuffer = 0;
        }

        /// Do not overclock
        @Override
        protected int @NotNull [] calculateOverclock(@NotNull Recipe recipe) {
            return new int[] { recipe.getEUt(), recipe.getDuration() };
        }

        @Override
        protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
            return true;
        }

        @Override
        protected void updateRecipeProgress() {
            if (this.canRecipeProgress) {

                int totalHeat = currentPower + heatBuffer;

                int remainingHeat = 0;
                int maxProgress;
                if (currentPower >= getRecipePower()) {
                    remainingHeat = totalHeat % getRecipePower();
                    maxProgress = totalHeat / getRecipePower();
                    hasEnoughPower = true;
                } else {
                    maxProgress = (currentPower - getRecipePower()) / 2000; // regress if not enough power
                    hasEnoughPower = false;
                }

                updateSpeedStats(maxProgress);

                boolean halted = maxProgress == 0;
                if (this.isHalted != halted) {
                    this.isHalted = halted;
                    writeCustomData(SuSyDataCodes.UPDATE_WORK_HALTED, buf -> buf.writeBoolean(halted));
                }

                this.progressTime += maxProgress;
                this.heatBuffer = remainingHeat;
                if (progressTime < 0) {
                    progressTime = 0;
                }
                if (this.progressTime > this.maxProgressTime) {
                    this.completeRecipe();
                }
            }
            if (getOffsetTimer() % 100 == 0) {
                numValidHeliostats = getNumOfValidHeliostats();
            }
            currentPower = WATTS_PER_HELIOSTAT * numValidHeliostats;
        }

        @Deprecated
        protected int getRecipePower() {
            return recipePower != 0 ? recipePower : 12000; // copied from evap pool idk what this does exactly
        }

        @Override
        protected void completeRecipe() {
            super.completeRecipe();
            this.recipePower = 0;
            this.heatBuffer = 0;
        }

        @Override
        public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
            super.receiveCustomData(dataId, buf);
            if (dataId == SuSyDataCodes.UPDATE_WORK_HALTED) {
                this.isHalted = buf.readBoolean();
            }
        }

        @Override
        public void writeInitialSyncData(@NotNull PacketBuffer buf) {
            super.writeInitialSyncData(buf);
            buf.writeBoolean(this.isHalted);
        }

        @Override
        public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
            super.receiveInitialSyncData(buf);
            this.isHalted = buf.readBoolean();
        }

        @NotNull @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = super.serializeNBT();
            if (this.progressTime > 0) {
                compound.setInteger("RecipePower", recipePower);
            }
            compound.setBoolean("IsHalted", this.isHalted);
            return compound;
        }

        @Override
        public void deserializeNBT(@NotNull NBTTagCompound compound) {
            super.deserializeNBT(compound);
            if (this.progressTime > 0) {
                recipePower = compound.getInteger("RecipePower");
            }
            this.isHalted = compound.getBoolean("IsHalted");
        }
    }
}
