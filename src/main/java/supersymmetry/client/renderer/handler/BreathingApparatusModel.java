package supersymmetry.client.renderer.handler;

import net.minecraft.client.model.ModelBiped;

import static supersymmetry.api.util.SuSyUtility.susyId;

public class BreathingApparatusModel extends ModelBiped {
    private final OBJModelRender objModel;

    public BreathingApparatusModel(String loc) {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.bipedHead.cubeList.clear();
        this.bipedHeadwear.cubeList.clear();
        this.bipedBody.cubeList.clear();
        this.bipedRightArm.cubeList.clear();
        this.bipedLeftArm.cubeList.clear();
        this.bipedLeftLeg.cubeList.clear();
        this.bipedRightLeg.cubeList.clear();

        this.objModel = new OBJModelRender(this, susyId("models/armor/" + loc + ".obj"));
        this.bipedHead.addChild(objModel);
    }
}
