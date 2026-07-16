package supersymmetry.common.metatileentities.single.electric;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gregtech.api.metatileentity.multiblock.IMaintenance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

public class MetaTileEntityDefoliator extends TieredMetaTileEntity {

    private int currentRadius = 0;

    private static final Field PROGRESS_TIME_FIELD;

    static {
        try {
            PROGRESS_TIME_FIELD = AbstractRecipeLogic.class.getDeclaredField("progressTime");
            PROGRESS_TIME_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find progressTime field in AbstractRecipeLogic", e);
        }
    }

    public MetaTileEntityDefoliator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDefoliator(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("harmRadius", currentRadius);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        currentRadius = data.getInteger("harmRadius");
    }

    @Override
    public void update() {
        super.update();

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);

        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity()) return;
        if (getWorld().isRemote) return;

        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());
        processShell(getWorld(), getPos(), currentRadius);

        if (currentRadius <= 32) currentRadius++;
    }

    private void processShell(World world, BlockPos center, int radius) {
        if (radius == 0) return;

        Map<ResourceLocation, IBlockState> replacements = DefoliatorReplacements.getReplacements();
        Map<String, IBlockState> metaReplacements = DefoliatorReplacements.getMetaReplacements();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos target = center.add(dx, dy, dz);
                    if (!world.isValid(target)) continue;
                    if (target.equals(center)) continue;

                    IBlockState state = world.getBlockState(target);

                    if (state.getBlock() != Blocks.AIR) {
                        ResourceLocation blockName = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                        if (blockName != null) {
                            String metaKey = blockName + ":" + state.getBlock().getMetaFromState(state);
                            IBlockState replacement = metaReplacements.get(metaKey);
                            if (replacement == null) replacement = replacements.get(blockName);
                            if (replacement != null) {
                                world.setBlockState(target, replacement, 2);
                            }
                        }
                    }

                    TileEntity te = world.getTileEntity(target);
                    if (!(te instanceof IGregTechTileEntity)) continue;
                    MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
                    if (mte == null) continue;

                    boolean isSuSyGreenhouse = mte.getClass() == SuSyMetaTileEntities.GREENHOUSE.getClass(); //susy one
                    boolean isGTFOGreenhouse = mte.getClass() == gregtechfoodoption.machines.multiblock.MetaTileEntityGreenhouse.class; //gtfo one

                    if (!(isSuSyGreenhouse || isGTFOGreenhouse)) {
                        continue;
                    }

                    if (mte instanceof IMaintenance) { //prob redundant to check
                        ((IMaintenance) mte).causeMaintenanceProblems();
                    }
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.defoliator.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.defoliator.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.generic.tooltip.radius_warning"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    // -------------------------------------------------------------------------

    public static class DefoliatorReplacements {

        private static Map<ResourceLocation, IBlockState> replacements = null;
        private static Map<String, IBlockState> metaReplacements = null;

        public static Map<ResourceLocation, IBlockState> getReplacements() {
            if (replacements == null) buildReplacements();
            return replacements;
        }

        public static Map<String, IBlockState> getMetaReplacements() {
            if (metaReplacements == null) buildReplacements();
            return metaReplacements;
        }

        // appease gtfo, without this we don't get resource location
        public static void init() {
            replacements = null;
            metaReplacements = null;
        }

        private static IBlockState stateOf(String domain, String path, int meta) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(domain, path));
            return block != null ? block.getStateFromMeta(meta) : Blocks.AIR.getDefaultState();
        }

        private static void buildReplacements() {
            // spotless:off

            // no metadata
            IBlockState AIR         = stateOf("minecraft", "air",  0);
            IBlockState WATER       = stateOf("minecraft", "water", 0);
            IBlockState COARSE_DIRT = stateOf("minecraft", "dirt",  1);
            IBlockState LOAMY_DIRT  = stateOf("biomesoplenty", "dirt", 8);
            IBlockState SANDY_DIRT  = stateOf("biomesoplenty", "dirt", 9);
            IBlockState SILTY_DIRT  = stateOf("biomesoplenty", "dirt", 10);

            Map<ResourceLocation, IBlockState> map = new HashMap<>();

            // Minecraft
            map.put(new ResourceLocation("minecraft", "tallgrass"),            AIR);
            map.put(new ResourceLocation("minecraft", "yellow_flower"),        AIR);
            map.put(new ResourceLocation("minecraft", "red_flower"),           AIR);
            map.put(new ResourceLocation("minecraft", "double_plant"),         AIR);
            map.put(new ResourceLocation("minecraft", "deadbush"),             AIR);
            map.put(new ResourceLocation("minecraft", "brown_mushroom"),       AIR);
            map.put(new ResourceLocation("minecraft", "brown_mushroom_block"), AIR);
            map.put(new ResourceLocation("minecraft", "red_mushroom"),         AIR);
            map.put(new ResourceLocation("minecraft", "red_mushroom_block"),   AIR);
            map.put(new ResourceLocation("minecraft", "vine"),                 AIR);
            map.put(new ResourceLocation("minecraft", "waterlily"),            AIR);
            map.put(new ResourceLocation("minecraft", "cactus"),               AIR);
            map.put(new ResourceLocation("minecraft", "reeds"),                AIR);
            map.put(new ResourceLocation("minecraft", "leaves"),               AIR);
            map.put(new ResourceLocation("minecraft", "leaves2"),              AIR);
            map.put(new ResourceLocation("minecraft", "sapling"),              AIR);
            map.put(new ResourceLocation("minecraft", "grass"),                COARSE_DIRT);
            map.put(new ResourceLocation("minecraft", "farmland"),             COARSE_DIRT);
            map.put(new ResourceLocation("minecraft", "grass_path"),           COARSE_DIRT);
            map.put(new ResourceLocation("minecraft", "mycelium"),             COARSE_DIRT);

            // Biomes O' Plenty
            map.put(new ResourceLocation("biomesoplenty", "bamboo"),          AIR);
            map.put(new ResourceLocation("biomesoplenty", "mushroom"),        AIR);
            map.put(new ResourceLocation("biomesoplenty", "waterlily"),       AIR);
            map.put(new ResourceLocation("biomesoplenty", "double_plant"),    AIR);
            map.put(new ResourceLocation("biomesoplenty", "willow_wine"),     AIR);
            map.put(new ResourceLocation("biomesoplenty", "bramble_plant"),   AIR);
            map.put(new ResourceLocation("biomesoplenty", "ivy"),             AIR);
            map.put(new ResourceLocation("biomesoplenty", "bamboo_tatching"), AIR);

            for (int i = 0; i <= 5; i++)
                map.put(new ResourceLocation("biomesoplenty", "leaves_"  + i), AIR);
            for (int i = 0; i <= 2; i++)
                map.put(new ResourceLocation("biomesoplenty", "sapling_" + i), AIR);
            for (int i = 0; i <= 1; i++)
                map.put(new ResourceLocation("biomesoplenty", "plant_"   + i), AIR);
            for (int i = 0; i <= 1; i++)
                map.put(new ResourceLocation("biomesoplenty", "flower_"   + i), AIR);

            // GregTech Food Option
            for (int i = 0; i <= 1; i++)
                map.put(new ResourceLocation("gregtechfoodoption", "gtfo_sapling_" + i), AIR);
            for (int i = 0; i <= 2; i++)
                map.put(new ResourceLocation("gregtechfoodoption", "gtfo_leaves_"  + i), AIR);

            for (ResourceLocation rl : Block.REGISTRY.getKeys()) {
                if (rl.getNamespace().equals("gregtechfoodoption") && rl.getPath().startsWith("crop_"))
                    map.put(rl, AIR);
            }

            //gregtech
            map.put(new ResourceLocation("gregtech", "rubber_leaves"), AIR);

            replacements = map;

            // metadata
            Map<String, IBlockState> metaMap = new HashMap<>();

            metaMap.put("biomesoplenty:coral:0",  WATER);
            metaMap.put("biomesoplenty:grass:2",  LOAMY_DIRT);
            metaMap.put("biomesoplenty:grass:3",  SANDY_DIRT);
            metaMap.put("biomesoplenty:grass:4",  SILTY_DIRT);
            metaMap.put("biomesoplenty:grass:5",  COARSE_DIRT);
            metaMap.put("biomesoplenty:grass:7",  COARSE_DIRT);

            metaReplacements = metaMap;
            // spotless:on
        }

        private DefoliatorReplacements() {}
    }
}
