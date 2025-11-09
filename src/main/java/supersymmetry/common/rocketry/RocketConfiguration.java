package supersymmetry.common.rocketry;

import static supersymmetry.api.space.Planetoid.PLANETOIDS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import supersymmetry.api.space.Planetoid;

public class RocketConfiguration {

    public enum MissionType {
        Manned,
        UnmannedCargo,
        UnmannedCollection
    }

    public enum DestinationType {
        Landing,
        Orbit
    }

    public static class MissionConfiguration {

        public final int dimension;
        public final BlockPos landingPos;
        public final MissionType missionType;
        public final DestinationType destinationType;

        public MissionConfiguration(NBTTagCompound landing) {
            this.dimension = landing.getInteger("dimension");
            this.landingPos = new BlockPos(
                    landing.getInteger("landing_x"),
                    landing.getInteger("landing_y"),
                    landing.getInteger("landing_z"));
            this.missionType = MissionType.values()[landing.getInteger("mission_type")];
            this.destinationType = DestinationType.values()[landing.getInteger("destination_type")];
        }

        public NBTTagCompound serialize() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("dimension", dimension);
            tag.setInteger("landing_x", landingPos.getX());
            tag.setInteger("landing_y", landingPos.getY());
            tag.setInteger("landing_z", landingPos.getZ());
            tag.setInteger("mission_type", missionType.ordinal());
            tag.setInteger("destination_type", destinationType.ordinal());
            return tag;
        }

        public int getDimension() {
            return this.dimension;
        }
    }

    private final List<MissionConfiguration> missions = new ArrayList<>();

    public RocketConfiguration(NBTTagCompound tag) {
        for (int i = 0; i < 10; i++) {
            NBTTagCompound missionTag = tag.getCompoundTag("page_" + i);
            if (!missionTag.isEmpty() && missionTag.getInteger("landing_y") != 0) {
                missions.add(new MissionConfiguration(missionTag));
            }
        }
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        for (int i = 0; i < missions.size(); i++) {
            tag.setTag("page_" + i, missions.get(i).serialize());
        }
        return tag;
    }

    public boolean setBudget(int startingDim, int budget) {
        // To go between two bodies within the same planetary system requires a budget of 1
        // And to go between two bodies in the same solar system requires a budget of 3

        // Get all dimensions in a row
        List<Integer> dimensions = new ArrayList<>();
        dimensions.add(startingDim);
        dimensions.addAll(missions.stream().map(MissionConfiguration::getDimension).collect(Collectors.toList()));
        int budgetUsed = 0;
        for (int i = 0; i < dimensions.size() - 1; i++) {
            Planetoid p1 = PLANETOIDS.inverse().get(dimensions.get(i));
            Planetoid p2 = PLANETOIDS.inverse().get(dimensions.get(i + 1));
            Planetoid parent1 = p1.getPlanetarySystem();
            Planetoid parent2 = p2.getPlanetarySystem();
            if (parent1 == null || parent2 == null) {
                budgetUsed += 10;
            } else if (parent1 == parent2) {
                budgetUsed += 1;
            } else if (parent1.getStarSystem() == parent2.getStarSystem()) {
                budgetUsed += 3;
            } else {
                budgetUsed += 8;
            }

            if (budgetUsed > budget) {
                // Remove all missions after this point.
                this.missions.subList(i + 1, this.missions.size()).clear();
                return false;
            }
        }
        return true;
    }

    public MissionConfiguration popFront() {
        return this.missions.remove(0);
    }
}
