package supersymmetry.common.rocketry.rockets;

import static supersymmetry.common.rocketry.SuccessCalculation.ESCAPE_VELOCITY_CONSTANT;
import static supersymmetry.common.rocketry.SuccessCalculation.augmentSuccess;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.IAFSImprovable;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.rocketry.SuccessCalculation;
import supersymmetry.common.world.WorldProviderPlanet;

public class SimpleStagedRocketBlueprint extends AbstractRocketBlueprint implements IAFSImprovable {

    public static class Builder {

        String name;
        ResourceLocation location;
        int stageCount = 0;
        public List<RocketStage> stages = new ArrayList<>();

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

        public SimpleStagedRocketBlueprint build() {
            SimpleStagedRocketBlueprint blueprint = new SimpleStagedRocketBlueprint(name, location);
            blueprint.setStages(stages);
            assert blueprint.isFullBlueprint() : "full blueprint produced by the builder, thats not meant to happen :C";
            return blueprint;
        }
    }

    public long AFSimprovement = 0;

    public SimpleStagedRocketBlueprint(String name, ResourceLocation entity) {
        super(name, entity);
    }

    public long getAFSImprovement() {
        return AFSimprovement;
    }

    public void setAFSImprovement(long a) {
        this.AFSimprovement = a;
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

        tag.setLong("AFSimprovement", this.AFSimprovement);
        tag.setDouble("maxVolume", this.getFuelVolume());
        tag.setDouble("maxCargoVolume", this.getCargoVolume());

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
        this.AFSimprovement = tag.getLong("AFSimprovement");
        return true;
    }

    public double calculateVelocity(double gravity, RocketFuelEntry fuel, double cargo) {
        double remainingWeight = this.getMass() + fuel.getDensity() * this.getFuelVolume();
        double deltaV = 0;
        // TODO: somehow incorporate cargo mass in a fair way
        for (RocketStage stage : this.stages) {
            double currentFuelWeight = stage.getFuelCapacity() * fuel.getDensity();
            deltaV += stage.getEffectiveFuelVelocity(fuel, gravity) *
                    Math.log(remainingWeight / (remainingWeight - currentFuelWeight));

            remainingWeight -= stage.getMass() + currentFuelWeight;
        }

        return deltaV;
    }

    public double getMaximumCargoMass(double gravity, RocketFuelEntry fuel, double escapeVelocity) {
        // DeltaV given some cargo mass x from the above is as such, with w_x being the base wet weight at stage x and
        // d_x being the base dry weight:
        // escapeVelocity = sum(ln((w_x + x) / (d_x + x)) * v_i)
        // We have to solve numerically
        // Newton's method, finding largest root specifically. The guess must start at zero since it's decreasing
        // concave up
        double guess = 0;
        double totalWeight = this.getMass() + fuel.getDensity() * this.getFuelVolume();
        for (int i = 0; i < 10; i++) {
            double fprime = 0;
            double f = 0;
            double remainingWeight = totalWeight + guess;

            // d/dx (ln((w_x + x) / (d_x + x)) * v_i) =
            // v_i * (d_x + x) / (w_x + x) * (d_x - w_x) / (d_x + x)^2
            // x is already accounted for
            for (RocketStage stage : this.stages) {
                double currentFuelWeight = stage.getFuelCapacity() * fuel.getDensity();
                double dryWeight = remainingWeight - currentFuelWeight;
                fprime += stage.getEffectiveFuelVelocity(fuel, gravity) * dryWeight / remainingWeight *
                        -currentFuelWeight / Math.pow(dryWeight, 2);
                f += stage.getEffectiveFuelVelocity(fuel, gravity) * Math.log(remainingWeight / dryWeight);
                remainingWeight -= stage.getMass() + currentFuelWeight;
            }
            f -= escapeVelocity;
            if (guess == 0 && f < 0) {
                return 0; // Exit early as to not blow up
            }
            guess -= f / fprime;
            if (f < 1e-8) break;
        }
        return guess;
    }

    // lobotomized version of the function below to only take in the blueprint
    public SuccessCalculation.AFSStats calculateInitialSuccess(double gravity, RocketFuelEntry fuel,
                                                               long augmentation) {
        double success = 1;
        double weight = this.getMass();
        double thrust = this.getThrust(fuel, gravity, "engine");
        double thrustToWeightRatio = thrust / weight;
        if (thrustToWeightRatio < 1) success = 0;

        double velocitySpeedup = calculateVelocity(gravity, fuel, 0);

        // Very approximate, assuming constant density rho = 5515 kg/m^3
        // g = GM / R^2
        // g = GR * 4/3pi * rho
        // R = 3/(4G * rho * pi) * g
        // escape velocity = sqrt(2gR) = g * sqrt(3/(2G * rho * pi))
        double escapeVelocity = ESCAPE_VELOCITY_CONSTANT * gravity;

        if (velocitySpeedup < escapeVelocity) {
            success = 0;
        } else {
            success *= (1 - (0.1 * Math.exp(10 * (escapeVelocity - velocitySpeedup) / escapeVelocity)));
        }

        success *= (1 - (0.5 * Math.exp(1 - thrustToWeightRatio)));
        double oblateness = this.getHeight() / this.getMaxRadius();
        success *= (1 - (0.2 * Math.exp(-oblateness)));
        success *= Math.pow(0.995, this.getComponentCount("engine"));
        double radialInstability = this.getTotalRadiusMismatch();
        success *= (1 - (0.02 * radialInstability * Math.exp(radialInstability / 10)));

        double smallThrust = this.getThrust(fuel, gravity, "engine_small");
        success *= (1 - (0.2 * Math.exp(3 - smallThrust)));

        if (thrust / smallThrust > 10) {
            success *= (1 - (0.2 * Math.exp((thrust / smallThrust) - 10)));
        } else if (thrust / smallThrust < 3) {
            success *= (1 - (0.5 * Math.exp(3 - (thrust / smallThrust))));
        }
        success *= this.getGuidanceMultiplier();
        success = Math.max(0, success);

        success = augmentSuccess(success, augmentation);

        return new SuccessCalculation.AFSStats(success, weight, fuel.getDensity() * this.getFuelVolume(),
                velocitySpeedup, escapeVelocity,
                getMaximumCargoMass(gravity, fuel, escapeVelocity), radialInstability, thrust, oblateness);
    }

    public SuccessCalculation.LaunchResult calculateSuccess(EntityAbstractRocket rocket, long augmentation) {
        double success = 1;
        // Thrust to weight ratio
        double gravity = 9.81;
        double escapeVelocity = 11186;
        if (rocket.world.provider instanceof WorldProviderPlanet planet) {
            gravity = planet.getPlanet().gravity * 9.81;
            escapeVelocity = Planetoid.PLANETOIDS.inverse().get(rocket.world.provider.getDimension())
                    .getEscapeVelocity();
        }
        double weight = this.getMass() * gravity;
        double thrust = this.getThrust(rocket.getFuel(), gravity, "engine");
        double thrustToWeightRatio = thrust / weight;

        if (thrustToWeightRatio < 1) {
            return SuccessCalculation.LaunchResult.CRASHES;
        } else {
            success *= (1 - (0.5 * Math.exp(1 - thrustToWeightRatio)));
        }

        double velocitySpeedup = this.calculateVelocity(gravity, rocket.getFuel(), rocket.getCargoMass());
        if (velocitySpeedup < escapeVelocity) {
            return SuccessCalculation.LaunchResult.CRASHES;
        } else {
            success *= (1 - (0.1 * Math.exp(10 * (escapeVelocity - velocitySpeedup) / escapeVelocity)));
        }

        // Oblateness (height / radius)
        double oblateness = this.getHeight() / this.getMaxRadius();
        success *= (1 - (0.1 * Math.exp(-oblateness)));

        // Number of engines, radius mismatch
        success *= Math.pow(0.995, this.getComponentCount("engine"));
        success *= (1 - (0.5 * Math.exp(this.getTotalRadiusMismatch() / 10)));

        // Small engines shouldn't have that much throughput
        double smallThrust = this.getThrust(rocket.getFuel(), gravity, "engine_small");
        double torqueNeeded = 1 + rocket.world.rainingStrength + rocket.world.thunderingStrength;
        success *= (1 - (0.2 * Math.exp(torqueNeeded - smallThrust)));

        if (thrust / smallThrust > 10) {
            success *= (1 - (0.2 * Math.exp((thrust / smallThrust) - 10)));
        } else if (thrust / smallThrust < 3) {
            success *= (1 - (0.5 * Math.exp(3 - (thrust / smallThrust))));
        }

        // Guidance system
        success *= this.getGuidanceMultiplier();
        success = Math.max(0, success);

        success = augmentSuccess(success, augmentation);

        if (Math.random() < success) {
            return SuccessCalculation.LaunchResult.LAUNCHES;
        } else {
            double engineActivity = this.getThrust(rocket.getFuel(), gravity, "engine") *
                    this.getComponentCount("tank");
            double chanceExplosion = 1 - Math.exp(-engineActivity / 100000);
            return Math.random() < chanceExplosion ? SuccessCalculation.LaunchResult.EXPLODES :
                    SuccessCalculation.LaunchResult.CRASHES;
        }
    }
}
