package supersymmetry.api.particle;

import java.text.DecimalFormat;

public class ParticlePhysicsUtils {

    public static String getSIPrefix(double number,int power) {
        int index =(int) Math.log10(Math.abs(number)) + power;

        return switch (index) {
            case -30, -29, -28 -> "q";
            case -27, -26, -25 -> "r";
            case -24, -23, -22 -> "y";
            case -21, -20, -19 -> "z";
            case -18, -17, -16 -> "a";
            case -15, -14, -13 -> "f";
            case -12, -11, -10 -> "p";
            case -9, -8, -7 -> "n";
            case -6, -5, -4 -> "u";
            case -3, -2, -1 -> "m";
            case 0, 1, 2 -> "";
            case 3, 4, 5 -> "k";
            case 6, 7, 8 -> "M";
            case 9, 10, 11 -> "G";
            case 12, 13, 14 -> "T";
            case 15, 16, 17 -> "P";
            case 18, 19, 20 -> "E";
            case 21, 22, 23 -> "Z";
            case 24, 25, 26 -> "Y";
            case 27, 28, 29 -> "R";
            case 30, 31, 32 -> "R";
            default -> "";
        };

    }

    public static String getSIFormat(double number,int power,String unit) {

        String prefix = getSIPrefix(number, power);

        if (prefix.equals("q")) {
            number *= Math.pow(10, 30+power);
        }
        if (prefix.equals("r")) {
            number *= Math.pow(10, 27+power);
        }
        if (prefix.equals("y")) {
            number *= Math.pow(10, 24+power);
        }
        if (prefix.equals("z")) {
            number *= Math.pow(10, 21+power);
        }
        if (prefix.equals("a")) {
            number *= Math.pow(10, 18+power);
        }
        if (prefix.equals("f")) {
            number *= Math.pow(10, 15+power);
        }
        if (prefix.equals("p")) {
            number *= Math.pow(10, 12+power);
        }
        if (prefix.equals("n")) {
            number *= Math.pow(10, 9+power);
        }
        if (prefix.equals("u")) {
            number *= Math.pow(10, 6+power);
        }
        if (prefix.equals("m")) {
            number *= Math.pow(10, 3+power);
        }
        if (prefix.equals("")) {
            number *= Math.pow(10, 0+power);
        }
        if (prefix.equals("k")) {
            number *= Math.pow(10, -3+power);
        }
        if (prefix.equals("M")) {
            number *= Math.pow(10, -6+power);
        }
        if (prefix.equals("G")) {
            number *= Math.pow(10, -9+power);
        }
        if (prefix.equals("T")) {
            number *= Math.pow(10, -12+power);
        }

        if (prefix.equals("P")) {
            number *= Math.pow(10, -15+power);
        }
        if (prefix.equals("E")) {
            number *= Math.pow(10, -18+power);
        }
        if (prefix.equals("Z")) {
            number *= Math.pow(10, -21+power);
        }
        if (prefix.equals("Y")) {
            number *= Math.pow(10, -24+power);
        }
        if (prefix.equals("R")) {
            number *= Math.pow(10, -27+power);
        }
        if (prefix.equals("Q")) {
            number *= Math.pow(10, -30+power);
        }

        DecimalFormat df = new DecimalFormat("#.###");

        return df.format(number)+ " "+ prefix+unit;
    }

    public static String getEnergyWithUnit(double energy) {
        if (energy == 0) return "< 1 keV";
        return getSIFormat(energy, 6, "eV");
    }

}
