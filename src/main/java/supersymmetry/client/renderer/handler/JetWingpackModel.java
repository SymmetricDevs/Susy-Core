package supersymmetry.client.renderer.handler;

import static supersymmetry.api.util.SuSyUtility.susyId;

import net.minecraft.client.model.ModelBiped;

public class JetWingpackModel extends ModelBiped {

    public static final JetWingpackModel INSTANCE = new JetWingpackModel();
    private final OBJModelRender objModel;

    public JetWingpackModel() {
        this.textureWidth = 128;
        this.textureHeight = 128;

        this.bipedHead.cubeList.clear();
        this.bipedHeadwear.cubeList.clear();
        this.bipedBody.cubeList.clear();
        this.bipedRightArm.cubeList.clear();
        this.bipedLeftArm.cubeList.clear();
        this.bipedLeftLeg.cubeList.clear();
        this.bipedRightLeg.cubeList.clear();

        this.objModel = new OBJModelRender(this, susyId("models/armor/jet_wingpack.obj"));
        this.bipedBody.addChild(objModel);
    }
}
