package supersymmetry.client.renderer.handler;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

import static supersymmetry.api.util.SuSyUtility.susyId;

public class BreathingApparatusModel extends ModelBiped {
    public BreathingApparatusModel(String name, EntityEquipmentSlot slot) {
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
            case FEET -> {
                this.bipedLeftLeg.addChild(modelForPart(name, "left_foot"));
                this.bipedRightLeg.addChild(modelForPart(name, "right_foot"));
            }
            case CHEST -> {
                this.bipedBody.addChild(modelForPart(name, "chest"));
                ModelRenderer leftArm = modelForPart(name, "left_arm", 12.125F);
                leftArm.offsetX = 0.0625F;
                this.bipedLeftArm.addChild(leftArm);
                ModelRenderer rightArm = modelForPart(name, "right_arm", 12.125F);
                rightArm.offsetX = -0.0625F;
                this.bipedRightArm.addChild(rightArm);
            }
            case LEGS -> {
                this.bipedBody.addChild(modelForPart(name, "belt", 17.5F));
                this.bipedLeftLeg.addChild(modelForPart(name, "left_leg", 16.5F));
                this.bipedRightLeg.addChild(modelForPart(name, "right_leg", 16.5F));
            }
            case HEAD -> this.bipedHead.addChild(modelForPart(name, "head"));
        }
    }

    public ResourceLocation modelLocationFromPart(String armor, String model) {
        return susyId("models/armor/" + armor + "_" + model + ".obj");
    }

    public OBJModelRender modelForPart(String armor, String model) {
        return modelForPart(armor, model, 17);
    }

    public OBJModelRender modelForPart(String armor, String model, float size) {
        return new OBJModelRender(this, modelLocationFromPart(armor, model), size);
    }
}
