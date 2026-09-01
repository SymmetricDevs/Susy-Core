package supersymmetry.common.rocketry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class SuccessCalculation {

    public static final double ESCAPE_VELOCITY_CONSTANT = 1138.92;
    public static final double AUGMENTATION_CONSTANT = 768000;
    // A 50% success blueprint takes just under 5 hours at this rate to bring to 80%
    // for a starting-tier AFS

    public static double augmentSuccess(double success, long augmentation) {
        success = Math.max(0.0001, success);
        double inverseSigmoid = Math.log(success / (1 - success));
        inverseSigmoid += success * Math.log(augmentation / AUGMENTATION_CONSTANT + 1);
        return 1 / (1 + Math.exp(-inverseSigmoid));
    }

    public enum LaunchResult {
        LAUNCHES,
        CRASHES,
        EXPLODES
    }

    public record AFSStats(double success, double mass, double fuelMass, double deltaV, double dragCoefficient,
                           double firstSepAltitude, double firstSepTime, double secondSepAltitude,
                           double secondSepTime, double thirdSepAltitude, double thirdSepTime,
                           double burnoutSpeed, double burnoutHorizontalSpeed) {

        public static AFSStats none() {
            return new AFSStats(-1, 0,  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public boolean isNone() {
            return this.success == -1;
        }

        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setDouble("success", success);
            tag.setDouble("mass", mass);
            tag.setDouble("fuelMass", fuelMass);
            tag.setDouble("deltaV", deltaV);
            tag.setDouble("dragCoefficient", dragCoefficient);
            tag.setDouble("firstSepAltitude", firstSepAltitude);
            tag.setDouble("firstSepTime", firstSepTime);
            tag.setDouble("secondSepAltitude", secondSepAltitude);
            tag.setDouble("secondSepTime", secondSepTime);
            tag.setDouble("thirdSepAltitude", thirdSepAltitude);
            tag.setDouble("thirdSepTime", thirdSepTime);
            tag.setDouble("burnoutSpeed", burnoutSpeed);
            tag.setDouble("burnoutHorizontalSpeed", burnoutHorizontalSpeed);
            return tag;
        }

        public static AFSStats deserializeNBT(NBTTagCompound nbt) {
            return new AFSStats(nbt.getDouble("success"), nbt.getDouble("mass"), nbt.getDouble("fuelMass"),
                    nbt.getDouble("deltaV"), nbt.getDouble("dragCoefficient"), nbt.getDouble("firstSepAltitude"),
                    nbt.getDouble("firstSepTime"), nbt.getDouble("secondSepAltitude"), nbt.getDouble("secondSepTime"),
                    nbt.getDouble("thirdSepAltitude"), nbt.getDouble("thirdSepTime"), nbt.getDouble("burnoutSpeed"),
                    nbt.getDouble("burnoutHorizontalSpeed"));
        }

        public void writeToBuffer(PacketBuffer buf) {
            buf.writeDouble(success);
            buf.writeDouble(mass);
            buf.writeDouble(fuelMass);
            buf.writeDouble(deltaV);
            buf.writeDouble(dragCoefficient);
            buf.writeDouble(firstSepAltitude);
            buf.writeDouble(firstSepTime);
            buf.writeDouble(secondSepAltitude);
            buf.writeDouble(secondSepTime);
            buf.writeDouble(thirdSepAltitude);
            buf.writeDouble(thirdSepTime);
            buf.writeDouble(burnoutSpeed);
            buf.writeDouble(burnoutHorizontalSpeed);
        }

        public static AFSStats readFromBuffer(PacketBuffer buf) {
            return new AFSStats(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }
}
