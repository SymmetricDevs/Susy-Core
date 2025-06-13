package supersymmetry.client.renderer.handler;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

import static supersymmetry.api.util.SuSyUtility.susyId;

public class SimpleBreathingApparatusModel extends ModelBiped {
    public SimpleBreathingApparatusModel(String name, EntityEquipmentSlot slot) {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.bipedHead.cubeList.clear();
        this.bipedHeadwear.cubeList.clear();
        this.bipedBody.cubeList.clear();
        this.bipedRightArm.cubeList.clear();
        this.bipedLeftArm.cubeList.clear();
        this.bipedLeftLeg.cubeList.clear();
        this.bipedRightLeg.cubeList.clear();

        addChildren(name, slot);
    }

    public void addChildren(String name, EntityEquipmentSlot slot) {
        switch (slot) {
            case CHEST -> this.bipedBody.addChild(modelForPart(name, "tank"));
            case HEAD -> this.bipedHead.addChild(modelForPart(name, "mask"));
        }
    }

    public ResourceLocation modelLocationFromPart(String armor, String model) {
        return susyId("models/armor/" + armor + "_" + model + ".obj");
    }

    public OBJModelRender modelForPart(String armor, String model) {
        return new OBJModelRender(this, modelLocationFromPart(armor, model), 17);
    }

}
