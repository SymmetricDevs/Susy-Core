package supersymmetry.common.pipelike.tanklessfluid;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import codechicken.lib.lighting.LightMatrix;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import codechicken.lib.raytracer.IndexedCuboid6;
import dev.tianmi.sussypatches.api.core.mixin.extension.MaterialPipeExtension;
import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.client.renderer.pipe.PipeRenderer;
import lombok.val;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;
import supersymmetry.client.renderer.pipe.TanklessFluidPipeRenderer;
import supersymmetry.common.pipelike.tanklessfluid.net.WorldTanklessFluidPipeNet;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipe;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipeTickable;

import static gregtech.api.metatileentity.MetaTileEntity.FULL_CUBE_COLLISION;

public class BlockTanklessFluidPipe
        extends BlockMaterialPipe<TanklessFluidPipeType, TanklessFluidPipeProperties, WorldTanklessFluidPipeNet>
        implements MaterialPipeExtension {

    private final SortedMap<Material, TanklessFluidPipeProperties> enabledMaterials = new TreeMap<>();

    public BlockTanklessFluidPipe(TanklessFluidPipeType pipeType, MaterialRegistry registry) {
        super(pipeType, registry);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    public void addPipeMaterial(Material material, TanklessFluidPipeProperties properties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(properties, "material %s tanklessFluidPipeProperties was null", material);
        Preconditions.checkArgument(material.getRegistry().getNameForObject(material) != null,
                "material %s is not registered", material);
        this.enabledMaterials.put(material, properties);
    }

    @Override
    public TileEntityPipeBase<TanklessFluidPipeType, TanklessFluidPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityTanklessFluidPipeTickable() : new TileEntityTanklessFluidPipe();
    }

    @Override
    public Class<TanklessFluidPipeType> getPipeTypeClass() {
        return TanklessFluidPipeType.class;
    }

    @Override
    protected TanklessFluidPipeProperties getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public WorldTanklessFluidPipeNet getWorldPipeNet(World world) {
        return WorldTanklessFluidPipeNet.getWorldPipeNet(world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return TanklessFluidPipeRenderer.INSTANCE.getParticleTexture((IPipeTile<?, ?>) world.getTileEntity(blockPos));
    }

    @Override
    protected TanklessFluidPipeProperties createProperties(TanklessFluidPipeType pipeType, Material material) {
        return pipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @SideOnly(Side.CLIENT)
    @NotNull @Override
    public PipeRenderer getPipeRenderer() {
        return TanklessFluidPipeRenderer.INSTANCE;
    }

    @Override
    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        for (val material : enabledMaterials.keySet()) {
            items.add(getItem(material));
        }
    }

    @Override
    public boolean canPipesConnect(IPipeTile<TanklessFluidPipeType, TanklessFluidPipeProperties> selfTile,
                                   EnumFacing side,
                                   IPipeTile<TanklessFluidPipeType, TanklessFluidPipeProperties> sideTile) {
        return selfTile instanceof TileEntityTanklessFluidPipe &&
                (sideTile instanceof TileEntityTanklessFluidPipe || ((IPipeTile<?, ?>) sideTile) instanceof TileEntityFluidPipe);
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<TanklessFluidPipeType, TanklessFluidPipeProperties> selfTile,
                                         EnumFacing side, TileEntity tile) {
        return tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        val stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockTanklessFluidPipe;
    }

    @Override
    protected List<IndexedCuboid6> getCollisionBox(IBlockAccess world, BlockPos pos, @Nullable Entity entityIn) {
        val result = super.getCollisionBox(world, pos, entityIn);
        if (result.isEmpty() || result.getFirst() == FULL_CUBE_COLLISION) return result;

        val pipeTile = (TileEntityTanklessFluidPipe) getPipeTileEntity(world, pos);
        int visualConnections = pipeTile.getVisualConnections();
        int connections = pipeTile.getConnections();
        float thickness = pipeType.getThickness();
        for (val side : EnumFacing.VALUES) {
            if (
                    (visualConnections & 1 << side.getIndex()) > 0 && // Connected
                    (connections & 1 << (12 + side.getIndex())) <= 0 && // No cover (dedupe)
                    pipeTile.isFlangeVisible(side) // Flange visible
            ) {
                val flange = TanklessFluidPipeRenderer.getFlangeBox(side, thickness, false);
                result.add(new IndexedCuboid6(new PipeConnectionData(side), flange));
            }
        }
        return result;
    }

    @Override
    public EnumActionResult onPipeToolUsed(World world, BlockPos pos, ItemStack stack, EnumFacing coverSide,
                                           IPipeTile<TanklessFluidPipeType, TanklessFluidPipeProperties> pipeTile,
                                           EntityPlayer entityPlayer, EnumHand hand) {
        if (coverSide != null && pipeTile instanceof TileEntityTanklessFluidPipe tanklessPipe &&
                ToolHelper.isTool(stack, ToolClasses.HARD_HAMMER)) {
            if (!world.isRemote) {
                tanklessPipe.toggleFlangeVisible(coverSide);
                ToolHelper.playToolSound(stack, entityPlayer);
            }
            entityPlayer.swingArm(hand);
            return EnumActionResult.SUCCESS;
        }
        return super.onPipeToolUsed(world, pos, stack, coverSide, pipeTile, entityPlayer, hand);
    }

    @Override
    @NotNull @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return TanklessFluidPipeRenderer.INSTANCE.getBlockRenderType();
    }
}
