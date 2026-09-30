package supersymmetry.common.faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import supersymmetry.Supersymmetry;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionZombies {

    private static final List<String> ZOMBIE_ATTACK_WHITELIST = Arrays.asList(
            "techguns:bandit", //add humanoid mobs to here, make sure you replace this once tg gets zucked
            "techguns:outcast",
            "techguns:armysoldier",
            "techguns:commando",
            "techguns:dictatordave",
            "techguns:psychosteve",
            "techguns:stormtrooper"
    );

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityZombie)) return;

        EntityZombie zombie = (EntityZombie) event.getEntity();

        for (String entityName : ZOMBIE_ATTACK_WHITELIST) {
            Class<? extends EntityLivingBase> targetClass = resolveEntityClass(entityName);
            if (targetClass == null) {
                continue;
            }

            zombie.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(
                    zombie,
                    targetClass,
                    true,
                    false
            ));
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends EntityLivingBase> resolveEntityClass(String registryName) {
        ResourceLocation rl = new ResourceLocation(registryName);
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(rl);
        if (entry == null) return null;

        Class<?> entityClass = entry.getEntityClass();
        if (entityClass == null) return null;
        if (!EntityLivingBase.class.isAssignableFrom(entityClass)) return null;

        return (Class<? extends EntityLivingBase>) entityClass;
    }
}
