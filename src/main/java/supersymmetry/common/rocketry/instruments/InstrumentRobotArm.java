package supersymmetry.common.rocketry.instruments;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static supersymmetry.common.rocketry.RocketConfiguration.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.ItemStackHandler;

import gregtech.api.recipes.Recipe;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.rocketry.LanderSpawnEntry;
import supersymmetry.common.rocketry.LanderSpawnQueue;
import supersymmetry.common.rocketry.RocketConfiguration;

public class InstrumentRobotArm implements Instrument {

    @Override
    public void act(int count, EntityAbstractRocket rocket) {
        // Check if an unmanned collection mission is next in the configuration
        RocketConfiguration config = rocket.getRocketConfiguration();
        MissionConfiguration mission = config.popFront();
        NBTTagCompound rocketNBT = rocket.getEntityData().getCompoundTag("rocket");
        AbstractRocketBlueprint blueprint = AbstractRocketBlueprint.getCopyOf(rocketNBT.getString("name"));

        if (!(mission.destinationType == DestinationType.Orbit)) {
            return;
        }

        // Then, the next mission must have a landing destination type
        MissionConfiguration nextMission = config.popFront();
        if (nextMission.destinationType != DestinationType.Landing) {
            return;
        }

        List<ItemStack> dummyInputs = new ArrayList<>();
        dummyInputs.add(Planetoid.PLANETOIDS.inverse().get(mission.dimension).getDisplayItem());
        Recipe salvagingRecipe = SuSyRecipeMaps.SALVAGING_RECIPES.findRecipe(1, dummyInputs, new ArrayList<>(), false);
        if (salvagingRecipe == null) {
            return;
        }

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        BlockPos landingPos = nextMission.landingPos;
        List<ItemStack> outputs = salvagingRecipe.getResultItemOutputs(0, 0, SuSyRecipeMaps.SALVAGING_RECIPES);
        double collEff = 0;
        // checks all payload stage components for collection efficiency - it's dumb but the "proper" method didn't work
        for (int i = 0; i < 10; i++) {
            NBTTagCompound compTag = rocketNBT.getTagList("stages", TAG_COMPOUND).getCompoundTagAt(3).getCompoundTag("componentValues").getCompoundTag(String.valueOf(i));
            if (compTag.getDouble("collectionEfficiency") > 0) {
                collEff = compTag.getDouble("collectionEfficiency");
                break;
            }
        }
        for (ItemStack output : outputs) {
            output.setCount((int) Math.round(output.getCount() * collEff));
        }
        // Turn into non-null list
        NonNullList<ItemStack> nonNullList = NonNullList.from(ItemStack.EMPTY, outputs.toArray(new ItemStack[0]));
        LanderSpawnEntry entry = new LanderSpawnEntry(nextMission.dimension, landingPos, salvagingRecipe.getDuration(),
                new ItemStackHandler(nonNullList).serializeNBT());
        LanderSpawnQueue.get(server.getWorld(nextMission.dimension)).addEntry(entry);
    }
}
