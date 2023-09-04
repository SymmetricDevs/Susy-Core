package supersymmetry.common.metatileentities.multi.electric;

import cam72cam.immersiverailroading.IRBlocks;
import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.entity.EntityBuildableRollingStock;
import cam72cam.immersiverailroading.entity.EntityMoveableRollingStock;
import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.items.ItemRollingStock;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.ItemComponentType;
import cam72cam.immersiverailroading.physics.TickPos;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.TrackBase;
import cam72cam.immersiverailroading.util.PlacementInfo;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.Speed;
import cam72cam.mod.entity.ModdedEntity;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.world.World;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.world.DummyWorld;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.SuSyValues;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.particles.SusyParticleFlame;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;


import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class MetaTileEntityRailroadEngineeringStation extends RecipeMapMultiblockController {
    private AxisAlignedBB structureAABB;
    private Gauge gauge = Gauge.from(Gauge.STANDARD);
    private boolean canFindTrain = true;
    private List<EntityRollingStock> rollingStocks = new ArrayList<>();
    private Int2ObjectOpenHashMap<net.minecraft.item.ItemStack> rollingStockItemStackMap = new Int2ObjectOpenHashMap<>();
    private EntityRollingStock selectedRollingStock;
    private EntityRollingStock spawnedRollingStock;
    List<ItemComponentType> spawnedRollingStackComponentsSorted;
    private NotifiableItemStackHandler trainOutputSlot;
    private NotifiableItemStackHandler trainInputSlot;

    private UUID previousEntityUUID;

    public MetaTileEntityRailroadEngineeringStation(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.RAILROAD_ENGINEERING_STATION_RECIPES);
        this.recipeMapWorkable = new RailroadEngineeringStationWorkable(this, trainInputSlot, trainOutputSlot);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRailroadEngineeringStation(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        // When changing the structure, do not forget to update the preview and the structure AABB - It's best ot just bug MTBO if you do not understand what that means
        return FactoryBlockPattern.start()
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("  CCC  BBB  CCC  ", "  CCC  BBB  CCC  ", "   C    B    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCFCCCCFCCCCFCCC", "CCCFCCCCFCCCCFCCC", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", " FFFFFFFFFFFFFFF ", "  FF   FGF   FF  ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCC", "   C    C    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("CCCCCCCCCCCCCCCCC", "RRRRRRRRRRRRRRRRR", "                 ", "                 ", "                 ", "                 ", "                 ", "       MMM       ", "       FGF       ", "       MMM       ")
                .aisle("                 ", "RRRRRRRRRRRRRRRRR", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       FMF       ", "                 ")
                .aisle("CCCCCCCCCCCCCCCCC", "RRRRRRRRRRRRRRRRR", "                 ", "                 ", "                 ", "                 ", "                 ", "       MMM       ", "       FGF       ", "       MAM       ")
                .aisle("CCCCCCCCCCCCCCCCC", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCC", "   C    C    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCFCCCCFCCCCFCCC", "CCCFCCCCFCCCCFCCC", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", " FFFFFFFFFFFFFFF ", "  FF   FGF   FF  ", "       F F       ")
                .aisle("  CCC  CCC  CCC  ", "  CCC  CSC  CCC  ", "   C    C    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .where('S', selfPredicate())
                .where('F', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('M', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('C', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)).or(this.autoAbilities(true,false)))
                .where('B', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)).or(autoAbilities(true,false, true, true, true, false, false)))
                .where(' ', any())
                .where('R', SuSyPredicates.rails())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.RAILROAD_ENGINEERING_STATION_OVERLAY;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();

        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("  CCC  CCC  CCC  ", "  CCC  CCC  CCC  ", "   C    C    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCFCCCCFCCCCFCCC", "CCCFCCCCFCCCCFCCC", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", " FFFFFFFFFFFFFFF ", "  FF   FGF   FF  ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCC", "   C    C    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("CCCCCCCCCCCCCCCCC", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       MMM       ", "       FGF       ", "       MMM       ")
                .aisle("CCCCCCCCCCCCCCCCC", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       FMF       ", "                 ")
                .aisle("CCCCCCCCCCCCCCCCC", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       MMM       ", "       FGF       ", "       MAM       ")
                .aisle("CCCCCCCCCCCCCCCCC", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCCCC", "   C    C    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("CCCCCCCCCCCCCCCCC", "CCCFCCCCFCCCCFCCC", "CCCFCCCCFCCCCFCCC", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", "   F    F    F   ", " FFFFFFFFFFFFFFF ", "  FF   FGF   FF  ", "       F F       ")
                .aisle("  CCC  CCC  CCC  ", "  CCC  CSC  CCC  ", "   C    C    C   ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "       F F       ", "       F F       ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .aisle("                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "                 ", "       F F       ", "                 ")
                .where('S', SuSyMetaTileEntities.RAILROAD_ENGINEERING_STATION, EnumFacing.SOUTH)
                .where('F', MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel))
                .where('M', MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                .where('A', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID), EnumFacing.SOUTH)
                .where('G', MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX))
                .where('C', MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT))
                .where(' ', Blocks.AIR.getDefaultState());

        MultiblockShapeInfo preInfo = builder.build();

        /*
        if(Loader.isModLoaded(SuSyValues.MODID_IMMERSIVERAILROADING)) {
            ItemStack trackBlueprintStack = new ItemStack(IRItems.ITEM_TRACK_BLUEPRINT, 0);
            trackBlueprintStack.internal.setTagInfo("length", new NBTTagInt(17));
            trackBlueprintStack.internal.setTagInfo("degrees", new NBTTagFloat(0));
            PlacementInfo placementInfo = new PlacementInfo(trackBlueprintStack, 270, new Vec3d(0.5,0.5,0.5));
            RailInfo railInfo = new RailInfo(trackBlueprintStack, placementInfo, (PlacementInfo)null);
            World irWorld = World.get(DummyWorld.INSTANCE);
            BuilderBase trackBuilder = railInfo.getBuilder(irWorld, new Vec3i(0,1,6));
            List<TrackBase> tracks = trackBuilder.getTracksForRender();

            BlockInfo[][][] blockInfos = preInfo.getBlocks();

            for(TrackBase track : tracks) {
                track.setRailHeight(0.5F);
                TileRailBase tr = track.placeTrack(true);

                BlockInfo blockInfo;

                if(tr instanceof TileRail) {
                    blockInfo = new BlockInfo(IRBlocks.BLOCK_RAIL.internal.getDefaultState(), tr.internal);
                } else {
                    blockInfo = new BlockInfo(IRBlocks.BLOCK_RAIL_GAG.internal.getDefaultState(), tr.internal);
                }

                blockInfos[track.getPos().x][1][track.getPos().z] = blockInfo;

            }
            preInfo = new MultiblockShapeInfo(blockInfos);
        }

         */

        shapeInfo.add(preInfo);

        return shapeInfo;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.setStructureAABB();
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();

        if(isFirstTick()) {
            List<ModdedEntity> trains = getWorld().getEntitiesWithinAABB(ModdedEntity.class, this.structureAABB);
            if(!trains.isEmpty()) {
                for (ModdedEntity forgeTrainEntity : trains) {
                    if(forgeTrainEntity.getSelf() instanceof EntityRollingStock rollingStock) {
                        if(rollingStock.getUUID().equals(this.previousEntityUUID)) {
                            this.spawnedRollingStock = rollingStock;
                            this.spawnedRollingStackComponentsSorted = rollingStock.getDefinition().getItemComponents().stream()
                                    .sorted(Comparator.comparingInt(i -> i.ordinal()))
                                    .collect(Collectors.toList());
                            break;
                        }
                    }
                }
            }
        }

        if(this.getOffsetTimer() % 20 == 1) {
            if(recipeMapWorkable.isActive()) {
                List<EntityPlayer> players = getWorld().getEntitiesWithinAABB(EntityPlayer.class, this.structureAABB);
                for (EntityPlayer player : players) {
                    player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 21, 1));
                }
            }

            if(this.canFindTrain) {
                List<ModdedEntity> trains = getWorld().getEntitiesWithinAABB(ModdedEntity.class, this.structureAABB);
                this.rollingStocks = new ArrayList<>();

                if(!trains.isEmpty()) {
                    for (ModdedEntity forgeTrainEntity : trains) {
                        if(forgeTrainEntity.getSelf() instanceof EntityRollingStock rollingStock) {
                            this.rollingStocks.add(rollingStock);
                        }
                    }
                }

                // This logic may need to be cleaned up
                // Currently this just selects the first train entity it found
                // Probably using the UI, showing shows each found entity
                // And the player select one specific entity
                if(!this.rollingStocks.isEmpty()) {
                    this.setSelectedEntity(this.rollingStocks.get(0));
                } else {
                    this.trainInputSlot.setStackInSlot(
                            0,
                            net.minecraft.item.ItemStack.EMPTY
                    );
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.getWorld().isRemote && this.isActive() && this.getOffsetTimer() % 2 == 0) {
            this.spawnWorkingParticles();
        }
    }

    private net.minecraft.item.ItemStack getTrainItemStackFromCache(EntityRollingStock rollingStock) {
        net.minecraft.item.ItemStack is;
        int id = rollingStock.getId();
        if ((is = this.rollingStockItemStackMap.get(id)) == null) {
            if ((is = this.getTrainItemStack(rollingStock)) != null) {
                this.rollingStockItemStackMap.put(id, is);
            }
        }
        return is.copy();
    }

    private net.minecraft.item.ItemStack getTrainItemStack(EntityRollingStock rollingStock) {
        EntityRollingStockDefinition def = rollingStock.getDefinition();
        ItemStack stack = new ItemStack(IRItems.ITEM_ROLLING_STOCK, 1);
        ItemRollingStock.Data data = new ItemRollingStock.Data(stack);
        data.def = def;
        data.gauge = this.gauge;
        data.write();
        return stack.internal;
    }

    private void setSelectedEntity(EntityRollingStock rollingStock) {
        this.selectedRollingStock = rollingStock;
        this.trainInputSlot.setStackInSlot(
                0,
                this.getTrainItemStackFromCache(this.selectedRollingStock)
        );
    }

    public float getTrainSpawnAngle() {
        if (this.frontFacing == EnumFacing.NORTH) {
            return 90;
        } else if (this.frontFacing == EnumFacing.EAST) {
            return 180;
        } else if (this.frontFacing == EnumFacing.SOUTH) {
            return 270;
        } else {
            return 0;
        }
    }

    public BlockPos getRailPos(net.minecraft.util.math.Vec3i direction) {
        return getPos().add(direction.getX() * 5, 0, direction.getZ() * 5);
    }

    public EntityRollingStock spawnRollingStock(ItemStack stack) {
        ItemRollingStock.Data data = new ItemRollingStock.Data(stack);

        EntityRollingStockDefinition def = data.def;

        if (def != null) {
            World irWorld = World.get(getWorld());

            //net.minecraft.util.math.Vec3i direction = this.getFrontFacing().getOpposite().getDirectionVec();

            // Centered around the middle rail

            //double offset = def.getCouplerPosition(EntityCoupleableRollingStock.CouplerType.BACK, gauge) - Config.ConfigDebug.couplerRange;

            BlockPos railPos = getRailPos(this.getFrontFacing().getOpposite().getDirectionVec());

            TickPos tp = new TickPos(
                    0,
                    Speed.ZERO,
                    (new Vec3d(railPos.getX() , railPos.getY(), railPos.getZ())).add(0.0, 0.25, 0.0).add(0.5, 0.0, 0.5),
                    this.getTrainSpawnAngle(),
                    this.getTrainSpawnAngle(),
                    this.getTrainSpawnAngle(),
                    0.0F,
                    false);
            EntityRollingStock stock = def.spawn(irWorld, tp.position, 0, gauge, data.texture);

            if (stock instanceof EntityMoveableRollingStock) {
                EntityMoveableRollingStock mrs = (EntityMoveableRollingStock)stock;
                tp.speed = Speed.ZERO;
                mrs.initPositions(tp);
            }

            // Buildable Rolling Stocks will be continuously built - we have to spawn them now as opposed to after the recipe completion
            if(stock instanceof EntityBuildableRollingStock) {
                this.setStockInWorld(stock);
                // Also sort the item components of the thing
                this.spawnedRollingStackComponentsSorted = def.getItemComponents().stream()
                        .sorted(Comparator.comparingInt(i -> i.ordinal()))
                        .collect(Collectors.toList());
            }
            return stock;
        }

        return null;
    }

    public void updateSpawnedStock(float recipeProgress) {
        if(this.spawnedRollingStock instanceof EntityBuildableRollingStock) {
            int idx = (int) (recipeProgress * spawnedRollingStackComponentsSorted.size());
            ((EntityBuildableRollingStock) this.spawnedRollingStock).setComponents(spawnedRollingStackComponentsSorted.subList(0, idx));
        }
    }

    public void completeSpawnedStock() {
        if(this.spawnedRollingStock instanceof EntityBuildableRollingStock) {
            // Finish the rolling stock
            ((EntityBuildableRollingStock) this.spawnedRollingStock).setComponents(((EntityBuildableRollingStock) this.spawnedRollingStock).getItemComponents());
        } else {
            this.setStockInWorld(this.spawnedRollingStock);
        }
        this.spawnedRollingStock = null;
        this.spawnedRollingStackComponentsSorted = null;
    }

    public void setStockInWorld(EntityRollingStock stock) {
        World irWorld = World.get(getWorld());
        irWorld.spawnEntity(stock);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        trainOutputSlot = new NotifiableItemStackHandler(1, this, true);
        trainInputSlot = new NotifiableItemStackHandler(1, this, false);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.structureAABB = null;
        this.selectedRollingStock = null;
        if(this.spawnedRollingStock != null) {
            this.spawnedRollingStock.kill();
            this.spawnedRollingStock = null;
        }
        this.rollingStocks = new ArrayList<>();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        if(this.spawnedRollingStock != null) {
            UUID rollingStockEntityID = spawnedRollingStock.getUUID();
            data.setUniqueId("RollingStockEntityID", rollingStockEntityID);
        }
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        // UUIDs get saved as two IDs I guess
        super.readFromNBT(data);
        if(data.hasKey("RollingStockEntityIDMost")) {
            this.setStructureAABB();
            this.previousEntityUUID = data.getUniqueId("RollingStockEntityID");
        }
    }

    @Override
    public void onRemoval() {
        if(this.spawnedRollingStock != null) {
            this.spawnedRollingStock.kill();
        }
        super.onRemoval();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!getWorld().isRemote) {

            if (this.spawnedRollingStock != null) {
                this.spawnedRollingStock.kill();
            }
        }
    }

    public void setStructureAABB() {
        // Had to make it overshoot a little :(
        net.minecraft.util.math.BlockPos offsetBottomLeft = new net.minecraft.util.math.BlockPos(9, -1, 2);
        net.minecraft.util.math.BlockPos offsetTopRight = new net.minecraft.util.math.BlockPos(-9, 8, 7);

        switch(this.getFrontFacing()) {
            case EAST:
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.CLOCKWISE_90);
                offsetTopRight = offsetTopRight.rotate(Rotation.CLOCKWISE_90);
                break;
            case SOUTH:
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.CLOCKWISE_180);
                offsetTopRight = offsetTopRight.rotate(Rotation.CLOCKWISE_180);
                break;
            case WEST:
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.COUNTERCLOCKWISE_90);
                offsetTopRight = offsetTopRight.rotate(Rotation.COUNTERCLOCKWISE_90);
                break;
            default:
                break;
        }

        this.structureAABB = new AxisAlignedBB(getPos().add(offsetBottomLeft), getPos().add(offsetTopRight));
    }

    @SideOnly(Side.CLIENT)
    public void spawnWorkingParticles() {
        SusyParticleFlame spark = new SusyParticleFlame(
                this.getWorld(),
                this.getPos().getX() + this.getFrontFacing().getOpposite().getDirectionVec().getX() * 5 + (1 - this.getFrontFacing().getOpposite().getDirectionVec().getX()) * 3 * (GTValues.RNG.nextFloat() - 0.5),
                this.getPos().getY() + 0.5,
                this.getPos().getZ() + this.getFrontFacing().getOpposite().getDirectionVec().getZ() * 5 + (1 - this.getFrontFacing().getOpposite().getDirectionVec().getZ()) * 3 * (GTValues.RNG.nextFloat() - 0.5),
                (GTValues.RNG.nextFloat() - 0.5) * 1.2F,
                GTValues.RNG.nextFloat() * 1.5F,
                (GTValues.RNG.nextFloat() - 0.5) * 1.2F);
        Minecraft.getMinecraft().effectRenderer.addEffect(spark);
    }

    protected static class RailroadEngineeringStationWorkable extends MultiblockRecipeLogic {

        NotifiableItemStackHandler trainOutput;
        NotifiableItemStackHandler trainInput;

        public RailroadEngineeringStationWorkable(RecipeMapMultiblockController tileEntity, NotifiableItemStackHandler trainInput, NotifiableItemStackHandler trainOutput) {
            super(tileEntity);
            this.trainInput = trainInput;
            this.trainOutput = trainOutput;
        }

        @Override
        public MetaTileEntityRailroadEngineeringStation getMetaTileEntity() {
            return (MetaTileEntityRailroadEngineeringStation) super.getMetaTileEntity();
        }

        @Override
        protected IItemHandlerModifiable getInputInventory() {
            IItemHandlerModifiable inputs = super.getInputInventory();

            List<IItemHandlerModifiable> inputList = new ArrayList<>();

            if(inputs != null) inputList.add(inputs);
            inputList.add(trainInput);

            return new ItemHandlerList(inputList);
        }

        @Override
        protected IItemHandlerModifiable getOutputInventory() {
            return trainOutput;
        }

        @Override
        protected boolean setupAndConsumeRecipeInputs(Recipe recipe, IItemHandlerModifiable importInventory) {
            boolean result = super.setupAndConsumeRecipeInputs(recipe, importInventory);

            if (result) {
                MetaTileEntityRailroadEngineeringStation mte = this.getMetaTileEntity();

                if(this.trainInput.getStackInSlot(0).isEmpty() && mte.selectedRollingStock != null) {
                    mte.selectedRollingStock.kill();
                    mte.selectedRollingStock = null;
                }

                mte.spawnedRollingStock = mte.spawnRollingStock(new ItemStack(recipe.getOutputs().get(0)));
            }
            return result;
        }

        @Override
        protected void updateRecipeProgress() {
            if (this.canRecipeProgress && this.drawEnergy(this.recipeEUt, true)) {
                this.drawEnergy(this.recipeEUt, false);

                this.getMetaTileEntity().updateSpawnedStock((float) this.progressTime/this.maxProgressTime);

                if (++this.progressTime > this.maxProgressTime) {
                    this.completeRecipe();
                }

                if (this.hasNotEnoughEnergy && this.getEnergyInputPerSecond() > 19L * (long)this.recipeEUt) {
                    this.hasNotEnoughEnergy = false;
                }
            } else if (this.recipeEUt > 0) {
                this.hasNotEnoughEnergy = true;
                if (this.progressTime >= 2) {
                    if (ConfigHolder.machines.recipeProgressLowEnergy) {
                        this.progressTime = 1;
                    } else {
                        this.progressTime = Math.max(1, this.progressTime - 2);
                    }
                }
            }

        }

        @Override
        protected void completeRecipe() {
            performMaintenanceMufflerOperations();
            GTTransferUtils.addFluidsToFluidHandler(this.getOutputTank(), false, this.fluidOutputs);
            this.progressTime = 0;
            this.setMaxProgress(0);
            this.recipeEUt = 0;
            this.fluidOutputs = null;
            this.itemOutputs = null;
            this.hasNotEnoughEnergy = false;
            this.wasActiveAndNeedsUpdate = true;
            this.parallelRecipesPerformed = 0;
            this.overclockResults = new int[]{0, 0};
            this.getMetaTileEntity().completeSpawnedStock();
        }
    }
}
