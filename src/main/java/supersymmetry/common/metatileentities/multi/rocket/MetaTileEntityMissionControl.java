package supersymmetry.common.metatileentities.multi.rocket;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.entities.EntityBlueprintRocket;
import supersymmetry.common.entities.EntityEarthLandingSystem;
import supersymmetry.common.entities.EntityLander;
import supersymmetry.common.mui.widget.ConditionalWidget;
import supersymmetry.common.rocketry.RocketConfiguration.*;
import supersymmetry.common.rocketry.SuccessCalculation.LaunchResult;
import supersymmetry.common.rocketry.rockets.SimpleStagedRocketBlueprint;

import static supersymmetry.SuSyValues.GRAVITATIONAL_CONSTANT;
import static supersymmetry.api.rocketry.NozzleFlow.GAS_CONSTANT;
import static supersymmetry.api.space.CelestialObjects.*;

public class MetaTileEntityMissionControl extends MultiblockWithDisplayBase implements IDataStickIntractable {

    private static final List<MetaTileEntityMissionControl> MISSION_CONTROLS = new ArrayList<>();
    private UUID selectedRocketUuid;
    private EntityBlueprintRocket selectedRocket = null;
    private EntityAbstractRocket selectedLander = null;

    // private MetaTileEntityGroundStation groundStation;
    private final Map<Integer, String> dimensionNames = Map.of(
            0, "Earth",
            800, "Moon");
    // Rocket Stat Simulation
    double[] prevMotion = {0, 0, 0};

    double turnAltitude;
    int time; // in ticks
    double dryMass;
    double fuelMass;
    LinkedHashMap<RocketStage, Double> activeStages = new LinkedHashMap<>(); // stage, remaining fuel mass
    LinkedHashMap<RocketStage, Double> remainingStages = new LinkedHashMap<>();
    // don't forget to populate them
    double speed;
    double altitude;
    double headingAngle;
    double dragCoeff;
    double vertThrust;
    double horiThrust;
    double totalThrust;
    double acceleration; // per tick

    List<Double> stageSepAltitudes = new ArrayList<>();
    List<Double> stageSepTimes = new ArrayList<>();

    double cargoMass;
    RocketFuelEntry fuel;
    Planetoid planet;
    boolean launching = false;

    public MetaTileEntityMissionControl(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        MISSION_CONTROLS.add(this);
    }

    @Override
    protected void updateFormedValid() {
        SimpleStagedRocketBlueprint selectedRocketBlueprint = null;

        if (selectedRocket != null)  {
            selectedRocketBlueprint = (SimpleStagedRocketBlueprint) selectedRocket.getBlueprint();
        } else {
            launching = false;
        }

        if (!launching) {
            if (selectedRocket != null && selectedRocketBlueprint != null && selectedRocket.isLaunched()) {
                launching = true;

                NBTTagCompound rocketnbt = selectedRocket.getEntityData();
                NBTTagCompound configTag = rocketnbt.getCompoundTag(EntityAbstractRocket.ROCKET_CONFIG_KEY);
                turnAltitude = configTag.getFloat("turn_altitude") * 1000;
                time = 0;
                cargoMass = selectedRocket.getCargoMass();
                dryMass = cargoMass;
                fuelMass = 0;
                fuel = selectedRocket.getFuel();

                planet = Planetoid.PLANETOIDS.inverse().get(selectedRocket.world.provider.getDimension());

                if (planet == null) {
                    launching = false;
                    return;
                }

                for (RocketStage stage : selectedRocketBlueprint.stages) {
                    remainingStages.put(stage, stage.getFuelCapacity() * fuel.getDensity());
                    dryMass += stage.getMass();
                    fuelMass += stage.getFuelCapacity() * fuel.getDensity();
                }

                dragCoeff = selectedRocketBlueprint.getMaxRadius() / selectedRocketBlueprint.getHeight() + selectedRocketBlueprint.getTotalRadiusMismatch() / 200;
                speed = 0;
            } else if (selectedRocket != null) {
                fuelMass = selectedRocket.getFuelVolume() * selectedRocket.getFuel().getDensity();
                cargoMass = selectedRocket.getCargoMass();
            }
        } else {
            if (planet == null || fuel == null || selectedRocketBlueprint == null) {
                launching = false;
            } else if (!remainingStages.isEmpty() ||
                    (!activeStages.isEmpty() && activeStages.firstEntry().getKey().getComponentCount("engine") > 0)) {
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
                double currentMass = cargoMass;
                List<Map.Entry<RocketStage, Double>> stagesToRemove = new ArrayList<>();
                for (Map.Entry<RocketStage, Double> currentStage : activeStages.entrySet()) {
                    currentThrust += currentStage.getKey().getThrust(fuel, "engine",
                            planet.getPressureFromAltitude(altitude));
                    currentStage.setValue(currentStage.getValue() - currentStage.getKey().getFuelThroughput("engine") / 20.0);
                    currentMass += currentStage.getKey().getMass() + currentStage.getValue();
                    if (currentStage.getValue() <= 0) {
                        stagesToRemove.add(currentStage);
                        stageSepAltitudes.add(altitude);
                        stageSepTimes.add((double) time / 20.0); // in seconds
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

                // https://en.wikipedia.org/wiki/Drag_equation
                dragForce = 0.5 * atmosphereDensity * speed * speed * dragCoeff * selectedRocketBlueprint.getMaxRadius() *
                        selectedRocketBlueprint.getMaxRadius() * Math.PI;
                // this doesn't take into account stage separation, but after the first stage separates drag is already
                // fairly miniscule
                dragForce = (dragForce > 10000 ? dragForce : 0); // ignore drag if it's small
                vertThrust = currentThrust * Math.cos(headingAngle);
                horiThrust = currentThrust * Math.sin(headingAngle);
                totalThrust = Math.sqrt(Math.pow(vertThrust, 2) + Math.pow(horiThrust, 2));

                fuelMass -= (totalThrust / (fuel.getSpecificImpulse() * 9.807)) * 0.05;

                acceleration = Math.sqrt(Math.pow(selectedRocket.motionX - prevMotion[0], 2) + Math.pow(selectedRocket.motionY - prevMotion[1], 2) + Math.pow(selectedRocket.motionZ - prevMotion[2], 2));
                prevMotion = new double[]{selectedRocket.motionX, selectedRocket.motionY, selectedRocket.motionZ};

                headingAngle = Math.asin(
                        horiThrust / Math.sqrt(horiThrust * horiThrust + Math.pow(vertThrust - gravitationalForce, 2)));
                // Gravity turn maneuver (this should consume a little thrust but idc)
                if (headingAngle == 0 && selectedRocket.getPosition().getY() > turnAltitude) {
                    headingAngle = Math.PI / 72.0; // 2.5 degrees
                }
                time++;
            }
        }
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "CCCCC")
                .aisle("CCCCC", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "CCCCC")
                .aisle("CCSCC", "CCCCC", "CCCCC")
                .where('S', selfPredicate())
                .where(' ', air())
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setMinGlobalLimited(1, 1))
                        .or(maintenancePredicate()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HIGH_POWER_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMissionControl(metaTileEntityId);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tag = super.writeToNBT(data);
        if (selectedRocketUuid != null) {
            tag.setUniqueId("RocketUUID", selectedRocketUuid);
        }
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        UUID rocketUuid = data.getUniqueId("RocketUUID");
        if (rocketUuid != null) {
            selectedRocketUuid = rocketUuid;
        }
    }

    @Override
    public void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = dataStick.getTagCompound();
        if (tag == null || !player.isSneaking()) {
            return;
        }

        UUID rocketUuid = tag.getUniqueId("RocketUUID");
        if (rocketUuid != null) {
            this.selectedRocketUuid = rocketUuid;
            getRocket();
        }
    }

    @Override
    public boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        return false;
    }

    private void getRocket() {

        if (this.selectedRocketUuid == null) {
            this.selectedRocket = null;
        }

        if (!(this.getWorld() instanceof WorldServer worldServer)) {
            return;
        }
        this.selectedRocket = (EntityBlueprintRocket) worldServer.getEntityFromUuid(this.selectedRocketUuid);
    }

    private String formatSI(double number) {
        if (number > 1000000) {
            return String.format("%.2f M", number / 1000000);
        } else if (number > 1000) {
            return String.format("%.2f k", number / 1000);
        }
        return String.format("%.2f ", number);
    }

    public static void notifyLanderSpawned(EntityAbstractRocket rocket, EntityAbstractRocket dropPod) {
        UUID rocketUuid = rocket.getUniqueID();
        for (MetaTileEntityMissionControl missionControl : MISSION_CONTROLS) {
            if (missionControl.selectedRocketUuid.equals(rocketUuid)) {
                missionControl.spawnedLander(dropPod);
            }
        }
    }

    void spawnedLander(EntityAbstractRocket dropPod) {
        this.selectedRocketUuid = dropPod.getUniqueID();
        this.selectedLander = dropPod;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        int width = 320;
        int height = 240;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.image(4, 4, width - 8, height - 8, GuiTextures.DISPLAY);
        builder.image(width - 24, height - 24, 16, 16, GuiTextures.GREGTECH_LOGO_XMAS);

        ConditionalWidget mainGroup = new ConditionalWidget(4, 4, width - 8, height - 8, () -> true);

        // TODO: switch to lang
        mainGroup.addWidget(new AdvancedTextWidget(4, 4, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Selected Rocket: " + selectedRocket.getName()));
            } else if (selectedLander != null) {
                l.add(new TextComponentString("Selected Rocket: " + selectedLander.getName()));
            } else {
                l.add(new TextComponentString("No Rocket Selected"));
            }
        }, 0xe38a0e));
        mainGroup.addWidget(new AdvancedTextWidget(4, 16, (l) -> {
            if (selectedRocket != null) {
                if (selectedRocket.isLaunched()) {
                    if (selectedRocket.posY == selectedRocket.prevPosY) {
                        LaunchResult result = selectedRocket.getLaunchResult();
                        if (result == LaunchResult.CRASHES) {
                            l.add(new TextComponentString("Launch Status: Crashed, Position: " +
                                    selectedRocket.getCrashPosition().toString()));
                        } else if (result == LaunchResult.EXPLODES) {
                            l.add(new TextComponentString("Launch Status: Exploded"));
                        } else {
                            l.add(new TextComponentString("Launch Status: Unknown, rocket stopped moving up"));
                        }
                    } else {
                        // TODO: make sure it is actually launching, like not in orbit or smth
                        l.add(new TextComponentString("Launch Status: Launching"));
                    }
                } else {
                    l.add(new TextComponentString("Launch Status: Waiting to launch"));
                }
            }
        }, 0xffffff));

        mainGroup.addWidget(new AdvancedTextWidget(4, 28, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Fuel: " + String.format("%,.2f", fuelMass / 1000) + " tonnes Cargo: " + formatSI(cargoMass * 1000) + "kg"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget(4, 40, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Altitude: " + formatSI(selectedRocket.getPosition().getY()) + "m Heading Angle: " + Math.round(Math.toDegrees(headingAngle)) + "°"));
            }
        }, 0xffffff));

        mainGroup.addWidget(new AdvancedTextWidget(4, 52, (l) -> {
            if (selectedRocket != null) {
                double totalVelocity = Math.sqrt(Math.pow(selectedRocket.motionX, 2) + Math.pow(selectedRocket.motionY, 2) + Math.pow(selectedRocket.motionZ, 2));
                l.add(new TextComponentString("Velocity: " + formatSI(totalVelocity) + "ms⁻¹ Acceleration: " + formatSI(acceleration) + "ms⁻²"));
            }
        }, 0xffffff));

        mainGroup.addWidget(new AdvancedTextWidget(4, 64, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Thrust: " + formatSI(totalThrust) + "N"));
            }
        }, 0xffffff));

        mainGroup.addWidget(new AdvancedTextWidget(4, 76, (l) -> {
            if (selectedRocket != null) {
                if (selectedRocket.hasActed()) {
                    l.add(new TextComponentString("Action Status: Has acted"));
                } else {
                    l.add(new TextComponentString("Action Status: Waiting to act"));
                }
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget((width - 8) / 2, 76, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Current Dimension: Earth"));
            } else if (selectedLander != null) {
                if (selectedLander instanceof EntityLander) {
                    l.add(new TextComponentString("Current Dimension: Moon"));
                } else if (selectedLander instanceof EntityEarthLandingSystem) {
                    l.add(new TextComponentString("Current Dimension: Earth"));
                }
            }
        }, 0xffffff));

        mainGroup.addWidget(new AdvancedTextWidget(4, 100, (l) -> {
            if (selectedRocket != null) {
                List<MissionConfiguration> missions = selectedRocket.getRocketConfiguration().getMissions();
                for (int i = 0; i < missions.size(); i++) {
                    MissionConfiguration mission = missions.get(i);
                    l.add(new TextComponentString("Mission " + (i + 1) + ": " + dimensionNames.get(mission.dimension) +
                            ", " + mission.destinationType.name()));
                }
                if (missions.isEmpty()) {
                    l.add(new TextComponentString("No missions"));
                }
            }
        }, 0xffffff));

        /*// Debug stuff
        mainGroup.addWidget(new LabelWidget(4, 32 + 64, "[DEBUG]", 0xff0000));
        mainGroup.addWidget(new AdvancedTextWidget(4, 36 + 72, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Launch Result: " + selectedRocket.getLaunchResult()));
            }
        }, 0xff9999));*/
        builder.widget(mainGroup);
        return builder;
    }
}
