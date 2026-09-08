package supersymmetry.common.metatileentities.multi.rocket;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import gregtech.api.gui.widgets.LabelWidget;
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
import supersymmetry.common.entities.EntityBlueprintRocket;
import supersymmetry.common.mui.widget.ConditionalWidget;
import supersymmetry.common.rocketry.RocketConfiguration.*;
import supersymmetry.common.rocketry.SuccessCalculation.LaunchResult;

public class MetaTileEntityMissionControl extends MultiblockWithDisplayBase implements IDataStickIntractable {

    private UUID selectedRocketUuid;
    // private MetaTileEntityGroundStation groundStation;
    private final Map<Integer, String> dimensionNames = Map.of(
            0, "Earth",
            800, "Moon");

    public MetaTileEntityMissionControl(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {}

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

        // Not Focus Rn
        /*
         * String metaName = tag.getString("MetaTileEntity");
         * if (metaName == null || !metaName.equals("susy:ground_station")) {
         * return;
         * }
         * if (tag.getInteger("Dimension") != this.getWorld().provider.getDimension()) {
         * return;
         * }
         * 
         * int[] pos = tag.getIntArray("Position");
         * BlockPos blockPos = new BlockPos(pos[0], pos[1], pos[2]);
         * 
         * TileEntity tileEntity = this.getWorld().getTileEntity(blockPos);
         * if (!(tileEntity instanceof IGregTechTileEntity gtTileEntity)) {
         * return;
         * }
         * if (!(gtTileEntity.getMetaTileEntity() instanceof MetaTileEntityGroundStation groundStation)) {
         * return;
         * }
         * 
         * this.groundStation = groundStation;
         */

        UUID rocketUuid = tag.getUniqueId("RocketUUID");
        if (rocketUuid != null) {
            this.selectedRocketUuid = rocketUuid;
        }
    }

    @Override
    public boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        return false;
    }

    private EntityBlueprintRocket getRocket() {
        if (this.selectedRocketUuid == null) {
            return null;
        }

        if (!(this.getWorld() instanceof WorldServer worldServer)) {
            return null;
        }
        return (EntityBlueprintRocket) worldServer.getEntityFromUuid(this.selectedRocketUuid);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        int width = 320;
        int height = 210;
        EntityBlueprintRocket selectedRocket = getRocket();

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.image(4, 4, width - 8, height - 8, GuiTextures.DISPLAY);
        builder.image(width - 24, height - 24, 16, 16, GuiTextures.GREGTECH_LOGO_XMAS);

        ConditionalWidget mainGroup = new ConditionalWidget(4, 4, width - 8, height - 8, () -> true);

        // TODO: switch to lang
        mainGroup.addWidget(new AdvancedTextWidget(4, 4, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Selected Rocket: " + selectedRocket.getName()));
            } else {
                l.add(new TextComponentString("No Rocket Selected"));
            }
        }, 0xe38a0e));
        mainGroup.addWidget(new AdvancedTextWidget(4, 8 + 8, (l) -> {
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

        mainGroup.addWidget(new AdvancedTextWidget(4, 12 + 16, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Fuel: " + 0 + " kg"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget(4, 16 + 24, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Cargo: " + selectedRocket.getCargoMass() + " kg"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget((width - 8) / 3, 12 + 16, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Height: " + selectedRocket.getPosition().getY() + " m"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget((width - 8) / 3, 16 + 24, (l) -> {
            if (selectedRocket != null) {
                double totalVelocity = Math.sqrt(Math.pow(selectedRocket.motionX, 2) +
                        Math.pow(selectedRocket.motionY, 2) + Math.pow(selectedRocket.motionZ, 2));
                l.add(new TextComponentString("Velocity: " + Math.round(totalVelocity * 20) + " m/s"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget(4, 20 + 32, (l) -> {
            if (selectedRocket != null) {
                if (selectedRocket.hasActed()) {
                    l.add(new TextComponentString("Action Status: Has acted"));
                } else {
                    l.add(new TextComponentString("Action Status: Waiting to act"));
                }
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget(4, 24 + 40, (l) -> {
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

        // Debug stuff
        mainGroup.addWidget(new LabelWidget(4, 32 + 64, "[DEBUG]", 0xff0000));
        mainGroup.addWidget(new AdvancedTextWidget(4, 36 + 72, (l) -> {
            if (selectedRocket != null) {
                l.add(new TextComponentString("Launch Result: " + selectedRocket.getLaunchResult()));
            }
        }, 0xff9999));

        builder.widget(mainGroup);

        return builder;
    }
}
