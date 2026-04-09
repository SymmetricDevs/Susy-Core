package supersymmetry.common.tileentities;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import supersymmetry.client.particle.ParticleFlareSmoke;

public class TileEntityFlare extends TileEntity implements ITickable {

    private float red = 1.0f;
    private float green = 0.0f;
    private float blue = 0.0f;

    public void setColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("flareRed",   red);
        compound.setFloat("flareGreen", green);
        compound.setFloat("flareBlue",  blue);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("flareRed")) {
            red   = compound.getFloat("flareRed");
            green = compound.getFloat("flareGreen");
            blue  = compound.getFloat("flareBlue");
        }
    }

    @Override
    public void update() {
        if (world.isRemote) {
            Minecraft.getMinecraft().effectRenderer.addEffect(
                    new ParticleFlareSmoke(
                            world,
                            pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.2,
                            pos.getY() + 0.1,
                            pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.2,
                            red, green, blue
                    )
            );
        }
    }
}
