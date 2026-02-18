package supersymmetry.api.pipenet.beamline;

import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockBeamlinePipe extends Block implements IParticleNetworkPart{

    private final BeamLineType beamLineType;

    public BlockBeamlinePipe(BeamLineType beamLineType) {
        super(Material.IRON);
        this.beamLineType = beamLineType;
        setTranslationKey("beamline_" + beamLineType.getName());
        //TODO SUSY creative tabs

        // setCreativeTab(GTCreativeTabs.TAB_GREGTECH);
        setHarvestLevel(ToolClasses.WRENCH, 1);
        setHardness(2f);
        setResistance(10f);
    }

    @Override
    public void onBlockPlacedBy(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isRemote) return;
        // first find all neighbouring networks
        List<ParticleNetwork> networks = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(pos).move(facing);
            IBlockState neighborState = worldIn.getBlockState(offsetPos);
            IParticleNetworkPart networkPart = IParticleNetworkPart.tryGet(worldIn, offsetPos, neighborState);
            if (networkPart != null && networkPart.getBeamLineType() == getBeamLineType()) {
                // neighbor is a valid pipe block
                ParticleNetwork network = ParticleNetwork.get(worldIn, offsetPos);
                if (network == null) {
                    // if for some reason there is not a network at the neighbor, create one
                    network = networkPart.getBeamLineType().createNetwork(worldIn);
                    network.recalculateNetwork(Collections.singleton(offsetPos.toImmutable()));
                    return;
                }
                if (!network.getBeamLineType().isValidPart(networkPart)) {
                    throw new IllegalStateException("NetworkPart " + networkPart + " beamLineType " +
                            network.getBeamLineType() + " is not valid for network type " + network.getBeamLineType());
                }
                IBeamLineEndpoint endpoint = IBeamLineEndpoint.tryGet(worldIn, offsetPos);
                // only count the network as connected if it's not an endpoint or the endpoints input or output face is
                // connected
                if (endpoint == null || endpoint.getFrontFacing().getAxis() == facing.getAxis()) {
                    networks.add(network);
                }
            }
        }
        offsetPos.release();
        if (networks.isEmpty()) {
            // create network
            ParticleNetwork network = this.beamLineType.createNetwork(worldIn);
            network.onPlacePipe(pos);
        } else if (networks.size() == 1) {
            // add to connected network
            networks.get(0).onPlacePipe(pos);
        } else {
            // merge all connected networks together
            ParticleNetwork main = networks.get(0);
            main.onPlacePipe(pos);
            networks.remove(0);
            for (ParticleNetwork network : networks) {
                main.mergePipeNet(network);
            }
        }
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (worldIn.isRemote) return;
        ParticleNetwork network = ParticleNetwork.get(worldIn, pos);
        if (network != null) {
            network.onRemovePipe(pos);
        }
    }

    /*@Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        if (itemIn == GTCreativeTabs.TAB_GREGTECH) {
            items.add(new ItemStack(this));
        }
    }*/

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("gregtech.block.tooltip.no_mob_spawning"));
    }

    public @NotNull BeamLineType getBeamLineType() {
        return beamLineType;
    }

}
