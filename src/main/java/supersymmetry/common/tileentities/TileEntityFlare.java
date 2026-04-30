package supersymmetry.common.tileentities;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.codehaus.groovy.runtime.InvokerHelper;

import supersymmetry.client.renderer.particles.SusyParticleFlareSmoke;

public class TileEntityFlare extends TileEntity implements ITickable {

    private float red = 0.0f;
    private float green = 0.0f;
    private float blue = 0.0f;

    private UUID target;
    private String flareType;
    private String faction;

    private int tickCounter = 0;
    private static final int SPAWN_INTERVAL = 1200; // hard coded for now, maybe later replace with config

    public void setColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setTarget(UUID target) {
        this.target = target;
        markDirty();
    }

    public UUID getTarget() {
        return target;
    }

    public void setFlareType(String type) {
        this.flareType = type;
        markDirty();
    }

    public void setFlareFaction(String faction) {
        this.faction = faction;
        markDirty();
    }

    public String getFlareFaction() {
        return faction;
    }

    private void spawnMobsForType() {
        if (target == null || flareType == null) return;

        net.minecraft.entity.player.EntityPlayer player = world.getPlayerEntityByUUID(target);

        if (player == null) return;

        if (player.getDistanceSq(pos) > 200 * 200) return;

        try {
            InvokerHelper.invokeMethod(
                    this,
                    "callGroovySpawn",
                    new Object[] { flareType, player });
        } catch (Throwable t) {
            System.out.println("Groovy spawn handler missing or failed");
            t.printStackTrace();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setFloat("flareRed", red);
        compound.setFloat("flareGreen", green);
        compound.setFloat("flareBlue", blue);

        if (target != null) {
            compound.setString("targetUUID", target.toString());
        }
        if (flareType != null) {
            compound.setString("flareType", flareType);
        }

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("flareRed")) {
            red = compound.getFloat("flareRed");
            green = compound.getFloat("flareGreen");
            blue = compound.getFloat("flareBlue");
        }

        if (compound.hasKey("targetUUID")) {
            target = UUID.fromString(compound.getString("targetUUID"));
        }
        if (compound.hasKey("flareType")) {
            flareType = compound.getString("flareType");
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnClientParticles() {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                new SusyParticleFlareSmoke(
                        world,
                        pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.2,
                        pos.getY() + 0.1,
                        pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.2,
                        red, green, blue));
    }

    @Override
    public void update() {
        if (target != null) { // skip outright if the flare was just placed down as decoration.
            if (!world.isRemote) {
                tickCounter++;
                if (tickCounter >= SPAWN_INTERVAL) {
                    tickCounter = 0;

                    if (flareType != null) {
                        spawnMobsForType();
                    }
                }
            }
        }

        if (world.isRemote) {
            spawnClientParticles();
        }
    }
}
