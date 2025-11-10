package supersymmetry.common.rocketry.instruments;

import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.ItemStackHandler;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.rocketry.LanderSpawnEntry;
import supersymmetry.common.rocketry.LanderSpawnQueue;
import supersymmetry.common.rocketry.RocketConfiguration;

import java.util.ArrayList;
import java.util.List;

import static supersymmetry.common.rocketry.RocketConfiguration.*;

public class InstrumentRobotArm implements Instrument {

    @Override
    public void act(int count, EntityAbstractRocket rocket) {
        // Check if an unmanned collection mission is next in the configuration
        RocketConfiguration config = rocket.getRocketConfiguration();
        MissionConfiguration mission = config.popFront();
        if (!(mission.missionType == MissionType.UnmannedCollection &&
                mission.destinationType == DestinationType.Orbit)) {
            return;
        }

        // Then, the next mission must have a landing destination type
        MissionConfiguration nextMission = config.popFront();
        if (!(nextMission.destinationType == DestinationType.Landing)) {
            return;
        }

        List<ItemStack> dummyInputs = new ArrayList<>();
        dummyInputs.add(Planetoid.PLANETOIDS.inverse().get(mission.dimension).getDisplayItem());
        Recipe salvagingRecipe = SuSyRecipeMaps.SALVAGING_RECIPES.findRecipe(0, dummyInputs, null, false);
        if (salvagingRecipe == null) {
            return;
        }

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        BlockPos landingPos = nextMission.landingPos;
        List<ItemStack> outputs = salvagingRecipe.getResultItemOutputs(0, 0, SuSyRecipeMaps.SALVAGING_RECIPES);
        // Turn into non-null list
        NonNullList<ItemStack> nonNullList = NonNullList.from(ItemStack.EMPTY, outputs.toArray(new ItemStack[0]));
        LanderSpawnEntry entry = new LanderSpawnEntry(
                nextMission.dimension, landingPos, salvagingRecipe.getDuration(),
                new ItemStackHandler(nonNullList).serializeNBT()
        );
        LanderSpawnQueue.get(server.getWorld(nextMission.dimension)).addEntry(entry);
    }
}
