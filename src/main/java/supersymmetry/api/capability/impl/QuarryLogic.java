package supersymmetry.api.capability.impl;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Random;

public class QuarryLogic {
    private static final int MAX_LINE_PROGRESS = 13;
    private static final Vec3i DEFAULT_DIRECTION = new  Vec3i(1, 0, 0);

    private final MetaTileEntity metaTileEntity;

    private BlockPos originPos;


    private int topLayerY;
    private Vec3i lineProgressDirection;
    private BlockPos nextPos;
    private int turns;
    private int layerMaxLineProgress;
    private int lineProgress;
    private Random random;

    public QuarryLogic(MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        lineProgressDirection = DEFAULT_DIRECTION;
        layerMaxLineProgress = 1;
        random = new Random();
    }


    public void init(){
        this.originPos = metaTileEntity.getPos().offset(metaTileEntity.getFrontFacing().getOpposite(), 7).add(0,-2,0);
        nextPos = originPos;
    }

    public void doQuarryOperation(){
        if(metaTileEntity.getWorld().isRemote)
            return;

        World world = metaTileEntity.getWorld();
        IBlockState state = world.getBlockState(nextPos);

        /* TODO: prevent overflow
        if(world.isAirBlock(nextPos)){
            updateNextPos();
            doQuarryOperation();
        }
        */

        Item drop = state.getBlock().getItemDropped(state,random,0);
        metaTileEntity.addNotifiedOutput(drop);

        //TODO: prevent breaking bedrock and such
        if(!state.getBlock().hasTileEntity(state)) //Don't break TEs
            world.setBlockToAir(nextPos);

        updateNextPos();
    }

    private void updateNextPos(){
        lineProgress++;
        if(lineProgress >= layerMaxLineProgress){
            lineProgress = 0;
            lineProgressDirection = lineProgressDirection.crossProduct(new Vec3i(0, 1, 0)); //rotate 90° around Y axis
            turns++;
            if(turns == 2) {
                turns = 0;
                layerMaxLineProgress++;
                if(layerMaxLineProgress >= MAX_LINE_PROGRESS){
                    nextPos = originPos.add(new Vec3i(0,-1,0)); //TODO: replace -1 with proper Y coordinate
                    return;
                    //TODO: update layer(s)
                }
            }

        }
        nextPos = nextPos.add(lineProgressDirection);
    }

}
