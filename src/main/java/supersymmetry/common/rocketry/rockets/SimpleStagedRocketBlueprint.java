package supersymmetry.common.rocketry.rockets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.IAFSimprovable;
import supersymmetry.api.rocketry.rockets.RocketStage;

public class SimpleStagedRocketBlueprint extends AbstractRocketBlueprint implements IAFSimprovable {

    public static class Builder {

        String name;
        ResourceLocation location;
        int stageCount = 0;
        public List<RocketStage> stages = new ArrayList<>();
        public double minSuccessChance = 0.01;

        public Builder(String name) {
            this.name = name;
        }

        public Builder entityResourceLocation(ResourceLocation rocket) {
            this.location = rocket;
            return this;
        }

        public Builder stage(RocketStage stage) {
            this.stages.add(stage);
            List<Integer> l = new ArrayList<>();
            l.add(stageCount);
            stageCount++;

            return this;
        }

        public Builder minSuccessChance(double f) {
            this.minSuccessChance = f;
            return this;
        }

        public SimpleStagedRocketBlueprint build() {
            SimpleStagedRocketBlueprint blueprint = new SimpleStagedRocketBlueprint(name, location);
            blueprint.setStages(stages);
            blueprint.setMinimalSuccessChance(this.minSuccessChance);
            assert blueprint.isFullBlueprint() : "full blueprint produced by the builder, thats not meant to happen :C";
            return blueprint;
        }
    }

    public double minimalSuccessChance = 0.01;

    public long AFSimporvement = 0;

    public SimpleStagedRocketBlueprint(String name, ResourceLocation entity) {
        super(name, entity);
    }

    public double getMinimalSuccessChance() {
        return this.minimalSuccessChance;
    }

    public void setMinimalSuccessChance(double minimalSuccessChance) {
        this.minimalSuccessChance = minimalSuccessChance;
    }

    public long getAFSimprovement() {
        return AFSimporvement;
    }

    public void setAFSimprovement(long a) {
        this.AFSimporvement = a;
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        boolean complete = this.isFullBlueprint();
        NBTTagList stageList = new NBTTagList();
        if (complete) {
            this.getStages().stream().forEach(x -> stageList.appendTag(x.writeToNBT()));
            tag.setTag("stages", stageList);
        }

        tag.setString("name", this.getName());
        tag.setBoolean("buildstat", complete);

        tag.setLong("AFSimporvement", this.AFSimporvement);

        return tag;
    }

    @Override
    public boolean readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("name", NBT.TAG_STRING)) return false;
        if (!tag.hasKey("buildstat")) return false;
        this.stages.clear();

        this.setName(tag.getString("name"));
        if (tag.getBoolean("buildstat")) {
            boolean ok = tag.getTagList("stages", NBT.TAG_COMPOUND).tagList.stream()
                    .map(x -> (NBTTagCompound) x)
                    .map(
                            comp -> {
                                RocketStage s = new RocketStage();
                                if (s.readFromNBT(comp)) {
                                    this.stages.add(s);
                                    return true;
                                }
                                return false;
                            })
                    .allMatch(Boolean::booleanValue);
            if (!ok) return false;
        } else {
            this.stages = new ArrayList<>(AbstractRocketBlueprint.getBlueprintsRegistry().get(name).stages);
        }
        this.setName(tag.getString("name"));
        this.AFSimporvement = tag.getLong("AFSimporvement");
        return true;
    }
}
