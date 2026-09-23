package supersymmetry.common.rocketry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                           List<Double> sepAltitudes, List<Double> sepTimes,
                           double burnoutSpeed, double burnoutHorizontalSpeed) {

        public static AFSStats none() {
            return new AFSStats(-1, 0, 0, 0, 0, Collections.emptyList(), Collections.emptyList(), 0, 0);
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
            tag.setInteger("seps", sepAltitudes.size());
            for (int i = 0; i < sepAltitudes.size(); i++) {
                tag.setDouble("sepAltitudes" + i, sepAltitudes.get(i));
                tag.setDouble("sepTimes" + i, sepTimes.get(i));
            }
            tag.setDouble("burnoutSpeed", burnoutSpeed);
            tag.setDouble("burnoutHorizontalSpeed", burnoutHorizontalSpeed);
            return tag;
        }

        public static AFSStats deserializeNBT(NBTTagCompound nbt) {
            List<Double> sepAltitudes = new ArrayList<>();
            List<Double> sepTimes = new ArrayList<>();
            for (int i = 0; i < nbt.getInteger("seps"); i++) {
                sepAltitudes.add(nbt.getDouble("sepAltitudes" + i));
                sepTimes.add(nbt.getDouble("sepTimes" + i));
            }

            return new AFSStats(nbt.getDouble("success"), nbt.getDouble("mass"), nbt.getDouble("fuelMass"),
                    nbt.getDouble("deltaV"), nbt.getDouble("dragCoefficient"), sepAltitudes, sepTimes,
                     nbt.getDouble("burnoutSpeed"),
                    nbt.getDouble("burnoutHorizontalSpeed"));
        }

        public void writeToBuffer(PacketBuffer buf) {
            buf.writeInt(sepAltitudes.size());
            for (int i = 0; i < sepAltitudes.size(); i++) {
                buf.writeDouble(sepAltitudes.get(i));
                buf.writeDouble(sepTimes.get(i));
            }
            buf.writeDouble(success);
            buf.writeDouble(mass);
            buf.writeDouble(fuelMass);
            buf.writeDouble(deltaV);
            buf.writeDouble(dragCoefficient);
            buf.writeDouble(burnoutSpeed);
            buf.writeDouble(burnoutHorizontalSpeed);
        }

        public static AFSStats readFromBuffer(PacketBuffer buf) {
            List<Double> sepAltitudes = new ArrayList<>();
            List<Double> sepTimes = new ArrayList<>();
            int len = buf.readInt();
            for (int i = 0; i < len; i++) {
                sepAltitudes.add(buf.readDouble());
            }
            return new AFSStats(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), sepAltitudes, sepTimes, buf.readDouble(), buf.readDouble());
        }
    }
}
