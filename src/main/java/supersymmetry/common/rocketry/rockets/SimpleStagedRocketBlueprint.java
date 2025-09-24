package supersymmetry.common.rocketry.rockets;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;

public class SimpleStagedRocketBlueprint extends AbstractRocketBlueprint {

    public static class Builder {

        String name;
        ResourceLocation location;
        int stageCount = 0;
        public List<RocketStage> stages = new ArrayList<>();
        public List<List<Integer>> ignitionSequence = new ArrayList<>();

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
            ignitionSequence.add(l);
            stageCount++;

            return this;
        }

        public Builder ignitesWith(RocketStage stage) {
            this.stages.add(stage);

            List<Integer> ignitions = ignitionSequence.get(ignitionSequence.size() - 1 /* last one added i guess */);
            ignitions.add(stageCount);
            stageCount++;
            return this;
        }

        public SimpleStagedRocketBlueprint build() {
            SimpleStagedRocketBlueprint blueprint = new SimpleStagedRocketBlueprint(name, location);
            blueprint.setStages(stages);
            blueprint.setIgnitionStages(
                    ignitionSequence.stream()
                            .map(inner -> inner.stream().mapToInt(Integer::intValue).toArray())
                            .collect(Collectors.toList()));
            assert blueprint.isFullBlueprint() : "full blueprint produced by the builder, thats not meant to happen :C";
            return blueprint;
        }
    }

    public SimpleStagedRocketBlueprint(String name, ResourceLocation entity) {
        super(name, entity);
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList stageList = new NBTTagList();
        NBTTagList ignitionOrder = new NBTTagList();
        this.getStages().stream().forEach(x -> stageList.appendTag(x.writeToNBT()));
        this.getIgnitionStages().stream().forEach(x -> ignitionOrder.appendTag(new NBTTagIntArray(x)));

        tag.setTag("stages", stageList);
        tag.setTag("ignitionOrder", ignitionOrder);
        tag.setString("name", this.getName());
        tag.setBoolean("buildstat", this.isFullBlueprint());

        return tag;
    }

    @Override
    public boolean readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("stages", NBT.TAG_LIST)) return false;
        if (!tag.hasKey("ignitionOrder", NBT.TAG_LIST)) return false;
        if (!tag.hasKey("name", NBT.TAG_STRING)) return false;
        if (!tag.hasKey("buildstat")) return false;
        this.stages.clear();
        this.ignitionStages.clear();

        List<NBTTagCompound> stagesCompounds = tag.getTagList("stages", NBT.TAG_COMPOUND).tagList.stream()
                .map(x -> (NBTTagCompound) x)
                .collect(Collectors.toList());
        for (var comp : stagesCompounds) {
            var stageRead = new RocketStage();
            if (stageRead.readFromNBT(comp)) {
                this.stages.add(stageRead);
            } else {
                return false;
            }
        }
        tag.getTagList("ignitionOrder", NBT.TAG_INT_ARRAY).tagList.stream()
                .map(x -> (NBTTagIntArray) x)
                .forEach(t -> this.ignitionStages.add(t.getIntArray()));
        this.setName(tag.getString("name"));
        return true;
    }
}
