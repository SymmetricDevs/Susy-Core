package supersymmetry.api.guns.attachment;

import lombok.Getter;

@Getter
public enum AttachmentSlot {

    TOP("optics"),
    UNDERSIDE("underside"),
    MUZZLE("muzzle"),
    LATERAL("lateral"),
    STOCK("stock");

    private final String serializedName;

    AttachmentSlot(String serializedName) {
        this.serializedName = serializedName;
    }
}
