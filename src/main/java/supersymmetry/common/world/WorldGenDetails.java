package supersymmetry.common.world;

public class WorldGenDetails {
    public String name;
    public int averageGroundLevel;
    public PlanetBiomeInfo[] biomeList;
    public String stone;
    public String bedrock;

    public int getAverageGroundLevel() {
        return averageGroundLevel;
    }

    public void setAverageGroundLevel(int averageGroundLevel) {
        this.averageGroundLevel = averageGroundLevel;
    }

    public PlanetBiomeInfo[] getBiomeList() {
        return this.biomeList;
    }

    public void setBiomeList(PlanetBiomeInfo[] biomeList) {
        this.biomeList = biomeList;
    }

    public String getStone() {
        return this.stone;
    }

    public void setStone(String stone) {
        this.stone = stone;
    }

    public String getBedrock() {
        return this.bedrock;
    }

    public void setBedrock(String bedrock) {
        this.bedrock = bedrock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
