package supersymmetry.common.rocketry.rockets;

import static supersymmetry.SuSyValues.GRAVITATIONAL_CONSTANT;
import static supersymmetry.api.rocketry.NozzleFlow.GAS_CONSTANT;
import static supersymmetry.api.space.CelestialObjects.*;
import static supersymmetry.common.rocketry.SuccessCalculation.ESCAPE_VELOCITY_CONSTANT;
import static supersymmetry.common.rocketry.SuccessCalculation.augmentSuccess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.SuSyValues;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.IAFSImprovable;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.space.CelestialObjects;
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
        boolean solidRocket = false;

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

        public Builder solidRocket() {
            this.solidRocket = true;
            return this;
        }

        public SimpleStagedRocketBlueprint build() {
            SimpleStagedRocketBlueprint blueprint = new SimpleStagedRocketBlueprint(name, location);
            blueprint.setStages(stages);
            blueprint.solidRocket(solidRocket);
            assert blueprint.isFullBlueprint() : "full blueprint produced by the builder, thats not meant to happen :C";
            return blueprint;
        }
    }

    private void solidRocket(boolean solidRocket) {
        this.solidRocket = solidRocket;
    }

    public long AFSimprovement = 0;
    public boolean solidRocket = false;

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
        NBTTagCompound instrumentList = new NBTTagCompound();
        this.getInstruments().forEach(instrumentList::setInteger);
        tag.setTag("instruments", instrumentList);
        tag.setBoolean("solidRocket", solidRocket);

        return tag;
    }

    @Override
    public boolean readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("name", NBT.TAG_STRING))
            return false;
        if (!tag.hasKey("buildstat"))
            return false;
        this.stages.clear();

        this.setName(tag.getString("name"));
        if (tag.getBoolean("buildstat")) {
            boolean ok = tag.getTagList("stages", NBT.TAG_COMPOUND).tagList.stream().map(x -> (NBTTagCompound) x)
                    .map(comp -> {
                        RocketStage s = new RocketStage();
                        if (s.readFromNBT(comp)) {
                            this.stages.add(s);
                            return true;
                        }
                        return false;
                    }).allMatch(Boolean::booleanValue);
            if (!ok)
                return false;
        } else {
            this.stages = new ArrayList<>(AbstractRocketBlueprint.getBlueprintsRegistry().get(name).stages);
        }
        this.setName(tag.getString("name"));
        this.AFSimprovement = tag.getLong("AFSimprovement");
        this.solidRocket = tag.getBoolean("solidRocket");

        return true;
    }

    public double calculateVelocity(RocketFuelEntry fuel, double cargo) {
        double remainingWeight = this.getMass() + fuel.getDensity() * this.getFuelVolume() + cargo;
        double deltaV = 0;
        // TODO: somehow incorporate cargo mass in a fair way
        for (RocketStage stage : this.stages) {
            double currentFuelWeight = stage.getFuelCapacity() * fuel.getDensity();
            deltaV += stage.getEffectiveFuelVelocity(fuel) *
                    Math.log(remainingWeight / (remainingWeight - currentFuelWeight));

            remainingWeight -= stage.getMass() + currentFuelWeight;
        }

        return deltaV;
    }

    public double getMaximumCargoMass(RocketFuelEntry fuel, double escapeVelocity) {
        // DeltaV given some cargo mass x from the above is as such, with w_x being the
        // base wet weight at stage x and
        // d_x being the base dry weight:
        // escapeVelocity = sum(ln((w_x + x) / (d_x + x)) * v_i)
        // We have to solve numerically
        // Newton's method, finding largest root specifically. The guess must start at
        // zero since it's decreasing
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
                fprime += stage.getEffectiveFuelVelocity(fuel) * dryWeight / remainingWeight * -currentFuelWeight /
                        Math.pow(dryWeight, 2);
                f += stage.getEffectiveFuelVelocity(fuel) * Math.log(remainingWeight / dryWeight);
                remainingWeight -= stage.getMass() + currentFuelWeight;
            }
            f -= escapeVelocity;
            if (guess == 0 && f < 0) {
                return 0; // Exit early as to not blow up
            }
            guess -= f / fprime;
            if (f < 1e-8)
                break;
        }
        return guess;
    }

    // lobotomized version of the function below to only take in the blueprint
    public SuccessCalculation.AFSStats calculateInitialSuccess(double gravity, double ambientPressure,
                                                               RocketFuelEntry fuel, long augmentation) {
        double success = 1;
        double gravMult = gravity / SuSyValues.G0;
        double weight = this.getMass() * gravMult;
        double thrust = this.getThrust(fuel, "engine", ambientPressure);
        double thrustToWeightRatio = thrust / weight;
        if (thrustToWeightRatio < 1)
            success = 0;

        double velocitySpeedup = calculateVelocity(fuel, 0);

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

        double deltaV = simulateRocketTakeoff(EARTH, fuel, gravity * 1000);
        if (deltaV > 0) {
            success = Math.clamp(Math.sqrt(deltaV / 100), 0, 1);
        } else {
            success = 0;
        }
        success *= Math.pow(0.995, this.getComponentCount("engine"));
        double radialInstability = this.getTotalRadiusMismatch();
        // success *= (1 - (0.02 * radialInstability * Math.exp(radialInstability / 10)));

        success *= this.getGuidanceMultiplier();
        double redundancyMult = Math.clamp(0.7 + this.getRedundancy() * 0.4, 0.7, 1.1);
        success *= redundancyMult;
        success = Math.max(0, success);

        success = augmentSuccess(success, augmentation);

        return new SuccessCalculation.AFSStats(success, weight, fuel.getDensity() * this.getFuelVolume(),
                velocitySpeedup, escapeVelocity, getMaximumCargoMass(fuel, escapeVelocity), radialInstability, thrust,
                oblateness);
    }

    public SuccessCalculation.LaunchResult calculateSuccess(EntityAbstractRocket rocket, long augmentation) {
        double success = 1;
        // Thrust to weight ratio
        double gravity = SuSyValues.G0;
        double escapeVelocity = 11186;
        double ambientPressure = CelestialObjects.EARTH.getSurfacePressure();
        if (rocket.world.provider instanceof WorldProviderPlanet planet) {
            gravity = planet.getPlanet().gravity * SuSyValues.G0;
            Planetoid launchSite = Planetoid.PLANETOIDS.inverse().get(rocket.world.provider.getDimension());
            escapeVelocity = launchSite.getEscapeVelocity();
            ambientPressure = launchSite.getSurfacePressure();
        }
        double gravMult = gravity / SuSyValues.G0;

        double weight = (this.getMass() + rocket.getCargoMass()) * gravMult;
        double thrust = this.getThrust(rocket.getFuel(), "engine", ambientPressure);
        double thrustToWeightRatio = thrust / weight;

        if (thrustToWeightRatio < 1) {
            return SuccessCalculation.LaunchResult.CRASHES;
        } else {
            success *= (1 - (0.5 * Math.exp(1 - thrustToWeightRatio)));
        }

        double velocitySpeedup = this.calculateVelocity(rocket.getFuel(), rocket.getCargoMass());
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
        double radialInstability = this.getTotalRadiusMismatch();
        success *= (1 - (0.02 * radialInstability * Math.exp(radialInstability / 10)));

        // Guidance system;
        double weatherChallenge = rocket.world.rainingStrength + rocket.world.thunderingStrength;

        success *= (this.getGuidanceMultiplier() - (weatherChallenge * (1 - this.getGuidanceMultiplier())));
        success = Math.max(0, success);

        double redundancyMult = Math.clamp(0.7 + this.getRedundancy() * 0.4, 0.7, 1.1);
        success *= redundancyMult;
        success = Math.max(0, success);
        success = augmentSuccess(success, augmentation);

        if (Math.random() < success) {
            return SuccessCalculation.LaunchResult.LAUNCHES;
        } else {
            double engineActivity = thrust * this.getComponentCount("tank");
            double chanceExplosion = 1 - Math.exp(-engineActivity / 10000000);
            return Math.random() < chanceExplosion ? SuccessCalculation.LaunchResult.EXPLODES :
                    SuccessCalculation.LaunchResult.CRASHES;
        }
    }

    /**
     * Simulates a ground-to-low-orbit flight of the given staged rocket on the given planet.
     * Returns the delta-V budget available to the rocket once it has reached said orbit, or -1 if it can't.
     */
    public double simulateRocketTakeoff(Planetoid planet, RocketFuelEntry fuel, double turnAltitude) {
        int time = 0; // seconds
        LinkedHashMap<RocketStage, Double> activeStages = new LinkedHashMap<>(); // stage, remaining fuel mass
        LinkedHashMap<RocketStage, Double> remainingStages = new LinkedHashMap<>();
        for (RocketStage stage : this.stages) {
            remainingStages.put(stage, stage.getFuelCapacity() * fuel.getDensity());
        }

        double speed = 0; // m/s, speed along gravity direction

        double horizontalSpeed = 2 * Math.PI * planet.getRadius() * EARTH_RADIUS /
                (planet.getRotationPeriod() * EARTH_SIDEREAL_ROTATION_PERIOD);
        // speed perpendicular to gravity direction
        // we'll be nice and assume the player is always launching from the equator
        double altitude = 0; // m
        double headingAngle = 0; // angle between -gravity and thrust vectors
        double orbitalSpeed = Math.sqrt((GRAVITATIONAL_CONSTANT * planet.getMass() * EARTH_MASS) /
                (planet.getRadius() * EARTH_RADIUS + planet.getLowOrbitAltitude())); // the minimum speed to remain in
                                                                                     // orbit for this planet
        while (!remainingStages.isEmpty() ||
                (!activeStages.isEmpty() && activeStages.firstEntry().getKey().getComponentCount("engine") > 0)) { //
            // ignite stages when previous ones have burned out
            if (activeStages.isEmpty()) {
                Map.Entry<RocketStage, Double> entry = remainingStages.pollFirstEntry();
                activeStages.put(entry.getKey(), entry.getValue());
            }
            // I'm assuming all parallel-burning stages are going to be called "boosters" in the future
            if (activeStages.size() == 1 && activeStages.firstEntry().getKey().name.equals("boosters")) {
                Map.Entry<RocketStage, Double> entry = remainingStages.pollFirstEntry();
                activeStages.put(entry.getKey(), entry.getValue());
            }

            double currentThrust = 0;
            double gravitationalForce = 0;
            double dragForce = 0;
            double currentMass = 0;
            List<Map.Entry<RocketStage, Double>> stagesToRemove = new ArrayList<>();
            for (Map.Entry<RocketStage, Double> currentStage : activeStages.entrySet()) {
                currentThrust += currentStage.getKey().getThrust(fuel, "engine",
                        planet.getPressureFromAltitude(altitude));
                currentStage.setValue(currentStage.getValue() - currentStage.getKey().getFuelThroughput("engine"));
                currentMass += currentStage.getKey().getMass() + currentStage.getValue();
                if (currentStage.getValue() <= 0) {
                    stagesToRemove.add(currentStage);
                }

            }
            for (Map.Entry<RocketStage, Double> stage : stagesToRemove) {
                activeStages.remove(stage.getKey());
            }

            for (Map.Entry<RocketStage, Double> remainingStage : remainingStages.entrySet()) {
                currentMass += remainingStage.getKey().getMass() + remainingStage.getValue();
            }
            gravitationalForce = GRAVITATIONAL_CONSTANT * (currentMass * planet.getMass() * EARTH_MASS) /
                    (Math.pow((planet.getRadius() * EARTH_RADIUS + altitude), 2));
            double atmosphereDensity = planet.getAtmosphereMolarMass() * planet.getPressureFromAltitude(altitude) /
                    (GAS_CONSTANT * planet.getGroundTemperature()); // this ignores temperature change with altitude,
                                                                    // but calculating that isn't easily possible :(

            double dragCoeff = this.getMaxRadius() / this.getHeight() + this.getTotalRadiusMismatch() / 200.0;
            // it's stupid but I'm not gonna implement actual aerodynamics

            // https://en.wikipedia.org/wiki/Drag_equation
            dragForce = 0.5 * atmosphereDensity * speed * speed * dragCoeff * this.getMaxRadius() *
                    this.getMaxRadius() * Math.PI;
            // this doesn't take into account stage separation, but after the first stage separates drag is already
            // fairly miniscule
            dragForce = (dragForce > 10000 ? dragForce : 0); // ignore drag if it's small
            double vertThrust = currentThrust * Math.cos(headingAngle);
            double horiThrust = currentThrust * Math.sin(headingAngle);
            double accel = (vertThrust - gravitationalForce - dragForce) / currentMass;
            double horiAccel = horiThrust / currentMass;
            altitude += speed;
            speed += accel;
            horizontalSpeed += horiAccel;
            headingAngle = Math.asin(
                    horiThrust / Math.sqrt(horiThrust * horiThrust + Math.pow(vertThrust - gravitationalForce, 2)));
            // Gravity turn maneuver (this should consume a little thrust but idc)
            if (headingAngle == 0 && altitude > turnAltitude) {
                headingAngle = Math.PI / 72.0; // 2.5 degrees
            }
            if (altitude < 0) {
                altitude = 0;
                speed = 0;
            }
            time++;
        }
        double finalSpeed = Math.sqrt(speed * speed + horizontalSpeed * horizontalSpeed);
        // the altitude check is to prevent completely stupid things
        if (finalSpeed > orbitalSpeed && altitude > 0.66 * planet.getLowOrbitAltitude()) {
            return finalSpeed - orbitalSpeed;
        }
        return -1;
    }

    @Override
    public boolean isSolidRocket() {
        return solidRocket;
    }
}
