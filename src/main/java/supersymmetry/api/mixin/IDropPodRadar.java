package supersymmetry.api.mixin;

import supersymmetry.common.entities.EntityDropPod;
import java.util.List;

public interface IDropPodRadar {
    List<EntityDropPod> susy$getIncomingDropPods();
}
