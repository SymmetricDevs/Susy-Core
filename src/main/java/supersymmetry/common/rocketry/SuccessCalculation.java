package supersymmetry.common.rocketry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class SuccessCalculation {

    public static final double ESCAPE_VELOCITY_CONSTANT = 1138.92;
    public static final double AUGMENTATION_CONSTANT = 19480;
    // A 50% success blueprint takes just under 5 hours at this rate to bring to 80% for a starting-tier AFS

    public static double augmentSuccess(double success, long augmentation) {
        success = Math.max(0.0001, success);
        double inverseSigmoid = Math.log(success / (1 - success));
        inverseSigmoid += success * Math.log(augmentation / AUGMENTATION_CONSTANT + 1);
        return 1 / (1 + Math.exp(-inverseSigmoid));
    }

    public enum LaunchResult {
        DOES_NOT_LAUNCH,
        LAUNCHES,
        CRASHES,
        EXPLODES
    }

    public record AFSStats(double success,
                           double mass, double fuelMass, double deltaV, double escapeVelocity, double cargoCapacity,
                           double radialInstability, double thrust, double oblateness) {

        public static AFSStats none() {
            return new AFSStats(-1, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public boolean isNone() {
            return this.success == -1;
        }

        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setDouble("success", success);
            tag.setDouble("mass", mass);
            tag.setDouble("fuelMass", mass);
            tag.setDouble("deltaV", deltaV);
            tag.setDouble("escapeVelocity", escapeVelocity);
            tag.setDouble("cargoCapacity", cargoCapacity);
            tag.setDouble("radialInstability", radialInstability);
            tag.setDouble("thrust", thrust);
            tag.setDouble("oblateness", oblateness);
            return tag;
        }

        public static AFSStats deserializeNBT(NBTTagCompound nbt) {
            return new AFSStats(nbt.getDouble("success"), nbt.getDouble("mass"), nbt.getDouble("fuelMass"),
                    nbt.getDouble("deltaV"), nbt.getDouble("escapeVelocity"),
                    nbt.getDouble("cargoCapacity"), nbt.getDouble("radialInstability"),
                    nbt.getDouble("thrust"), nbt.getDouble("oblateness"));
        }

        public void writeToBuffer(PacketBuffer buf) {
            buf.writeDouble(success);
            buf.writeDouble(mass);
            buf.writeDouble(fuelMass);
            buf.writeDouble(deltaV);
            buf.writeDouble(escapeVelocity);
            buf.writeDouble(cargoCapacity);
            buf.writeDouble(radialInstability);
            buf.writeDouble(thrust);
            buf.writeDouble(oblateness);
        }

        public static AFSStats readFromBuffer(PacketBuffer buf) {
            return new AFSStats(buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }
}
