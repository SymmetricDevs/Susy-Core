package supersymmetry.common.world.weather;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import supersymmetry.common.world.WorldProviderPlanet;

/**
 * Handles all client-visible and server-side effects of planet weather.
 *
 * Effects per type:
 * CLEAR — nothing
 * FOG — blindness potion effect (visual only, no damage)
 * RAIN — rain particles, minor slow
 * SNOWSTORM — snow particles, slow, places snow layers on exposed surfaces
 * HAILSTORM — hail particles, slow, 1 dmg/t outdoors
 * DUST_STORM — dust particles, heavy slow, blindness, erodes loose surface blocks
 * THUNDERSTORM — rain + lightning particles, occasional lightning strikes
 * HURRICANE — extreme slow (near-zero), 2 dmg/t outdoors, erodes
 * TORNADO — teleports/flings player, 4 dmg/t outdoors, heavy erosion
 */
public class WeatherEventHandler {

    /** How often (ticks) to apply block effects per player. */
    private static final int BLOCK_EFFECT_INTERVAL = 40;

    /** Damage source used for all weather damage. */
    public static final DamageSource WEATHER_DAMAGE = new DamageSource("susy_weather").setDamageBypassesArmor();

    private int blockEffectTimer = 0;

    // -------------------------------------------------------------------------
    // World tick — block effects
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        World world = event.world;
        if (world.isRemote) return;
        if (!(world.provider instanceof WorldProviderPlanet)) return;

        WeatherType weather = ((WorldProviderPlanet) world.provider).getWeatherManager().getCurrentWeather();

        if (++blockEffectTimer >= BLOCK_EFFECT_INTERVAL) {
            blockEffectTimer = 0;
            if (weather.placesBlocks || weather.erodeSurface) {
                applyBlockEffects(world, weather);
            }
        }

        // Random lightning strikes during thunderstorm
        if (weather == WeatherType.THUNDERSTORM && world.rand.nextInt(800) == 0) {
            spawnLightning(world);
        }
    }

    // -------------------------------------------------------------------------
    // Living update — movement, damage, potions
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        World world = player.world;
        if (world.isRemote) return;
        if (!(world.provider instanceof WorldProviderPlanet)) return;

        WeatherType weather = ((WorldProviderPlanet) world.provider).getWeatherManager().getCurrentWeather();
        if (weather == WeatherType.CLEAR) return;

        boolean isOutdoors = isOutdoors(player);

        // Movement debuff — applied regardless of cover for severe weather
        applyMovementEffect(player, weather, isOutdoors);

        // Damage — only outdoors
        if (isOutdoors && weather.dealsDamage()) {
            // Every second (20 ticks)
            if (world.getTotalWorldTime() % 20 == 0) {
                player.attackEntityFrom(WEATHER_DAMAGE, weather.damagePerTick);
            }
        }

        // Tornado: fling the player
        if (weather == WeatherType.TORNADO && isOutdoors && world.rand.nextInt(40) == 0) {
            flingPlayer(player);
        }
    }

    // -------------------------------------------------------------------------
    // Client-side particles — called from client tick
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        World world = mc.world;
        if (world == null) return;
        if (!(world.provider instanceof WorldProviderPlanet)) return;

        WeatherType weather = ((WorldProviderPlanet) world.provider).getWeatherManager().getCurrentWeather();
        if (weather == WeatherType.CLEAR) return;
        if (mc.player == null) return;

        spawnWeatherParticles(world, mc.player, weather);
    }

    // -------------------------------------------------------------------------
    // Block effects
    // -------------------------------------------------------------------------

    private void applyBlockEffects(World world, WeatherType weather) {
        for (EntityPlayer player : world.playerEntities) {
            BlockPos center = player.getPosition();
            // Sample a 9x9 area around each player
            for (int attempt = 0; attempt < 16; attempt++) {
                int dx = world.rand.nextInt(9) - 4;
                int dz = world.rand.nextInt(9) - 4;
                BlockPos surface = getSurfacePos(world, center.add(dx, 0, dz));
                if (surface == null) continue;

                if (weather.placesBlocks) {
                    placeWeatherBlock(world, weather, surface);
                }
                if (weather.erodeSurface) {
                    erodeBlock(world, surface);
                }
            }
        }
    }

    private void placeWeatherBlock(World world, WeatherType weather, BlockPos surface) {
        BlockPos above = surface.up();
        IBlockState current = world.getBlockState(above);
        if (!current.getBlock().isAir(current, world, above)) return;

        if (weather == WeatherType.SNOWSTORM || weather == WeatherType.HAILSTORM) {
            // Place snow layer if the block below can support it
            IBlockState below = world.getBlockState(surface);
            if (below.isSideSolid(world, surface, net.minecraft.util.EnumFacing.UP)) {
                world.setBlockState(above, Blocks.SNOW_LAYER.getDefaultState()
                        .withProperty(BlockSnow.LAYERS, 1), 2);
            }
        }
    }

    private void erodeBlock(World world, BlockPos surface) {
        IBlockState state = world.getBlockState(surface);
        // Only erode loose surface materials (regolith, sand, gravel, snow)
        if (isErodible(state)) {
            if (world.rand.nextInt(3) == 0) {
                world.setBlockToAir(surface);
            }
        }
    }

    private boolean isErodible(IBlockState state) {
        return state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.GRAVEL ||
                state.getBlock() == Blocks.SNOW_LAYER || state.getBlock() == Blocks.SNOW;
        // SuSy regolith blocks should be added here once accessible:
        // || state.getBlock() == SuSyBlocks.REGOLITH
    }

    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    private void applyMovementEffect(EntityPlayer player, WeatherType weather, boolean isOutdoors) {
        if (!isOutdoors && weather != WeatherType.TORNADO) return;

        float mult = weather.movementMultiplier;
        // Convert to slowness amplifier: each level reduces speed by 15%
        // mult=0.85 → 1 level, mult=0.7 → 2, mult=0.4 → 4, mult=0.0 → 6
        int amplifier = Math.round((1.0f - mult) / 0.15f);
        if (amplifier <= 0) return;

        player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, amplifier - 1, true, false));

        if (weather == WeatherType.FOG || weather == WeatherType.DUST_STORM) {
            player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 80, 0, true, false));
        }
    }

    // -------------------------------------------------------------------------
    // Particles (client)
    // -------------------------------------------------------------------------

    private void spawnWeatherParticles(World world, EntityPlayer player, WeatherType weather) {
        double px = player.posX;
        double py = player.posY;
        double pz = player.posZ;
        java.util.Random rand = world.rand;

        switch (weather.particles) {
            case RAIN:
                for (int i = 0; i < 50; i++) {
                    world.spawnParticle(EnumParticleTypes.WATER_DROP,
                            px + rand.nextGaussian() * 8, py + 6 + rand.nextDouble() * 2,
                            pz + rand.nextGaussian() * 8, 0, -0.5, 0);
                }
                break;

            case SNOW:
                for (int i = 0; i < 60; i++) {
                    world.spawnParticle(EnumParticleTypes.SNOWBALL,
                            px + rand.nextGaussian() * 8, py + 6 + rand.nextDouble() * 2,
                            pz + rand.nextGaussian() * 8,
                            rand.nextGaussian() * 0.1, -0.3, rand.nextGaussian() * 0.1);
                }
                break;

            case HAIL:
                for (int i = 0; i < 40; i++) {
                    world.spawnParticle(EnumParticleTypes.SNOWBALL,
                            px + rand.nextGaussian() * 6, py + 8 + rand.nextDouble() * 2,
                            pz + rand.nextGaussian() * 6,
                            rand.nextGaussian() * 0.2, -0.8, rand.nextGaussian() * 0.2);
                }
                break;

            case DUST:
                for (int i = 0; i < 80; i++) {
                    world.spawnParticle(EnumParticleTypes.SUSPENDED,
                            px + rand.nextGaussian() * 10, py + rand.nextDouble() * 4,
                            pz + rand.nextGaussian() * 10,
                            rand.nextGaussian() * 0.4, 0, rand.nextGaussian() * 0.4);
                }
                break;

            case THUNDER:
                for (int i = 0; i < 50; i++) {
                    world.spawnParticle(EnumParticleTypes.WATER_DROP,
                            px + rand.nextGaussian() * 8, py + 6 + rand.nextDouble() * 2,
                            pz + rand.nextGaussian() * 8, 0, -0.6, 0);
                }
                break;

            case HURRICANE:
            case TORNADO:
                for (int i = 0; i < 100; i++) {
                    double angle = rand.nextDouble() * Math.PI * 2;
                    double r = rand.nextDouble() * 10;
                    world.spawnParticle(EnumParticleTypes.SUSPENDED,
                            px + Math.cos(angle) * r, py + rand.nextDouble() * 8,
                            pz + Math.sin(angle) * r,
                            Math.cos(angle) * 0.5, rand.nextDouble() * 0.2, Math.sin(angle) * 0.5);
                }
                break;

            case FOG:
                for (int i = 0; i < 30; i++) {
                    world.spawnParticle(EnumParticleTypes.CLOUD,
                            px + rand.nextGaussian() * 12, py + rand.nextDouble() * 6,
                            pz + rand.nextGaussian() * 12, 0, 0, 0);
                }
                break;

            default:
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private boolean isOutdoors(EntityPlayer player) {
        return player.world.canSeeSky(player.getPosition());
    }

    private void flingPlayer(EntityPlayer player) {
        float angle = player.world.rand.nextFloat() * (float) Math.PI * 2;
        float strength = 1.5f + player.world.rand.nextFloat() * 1.5f;
        player.motionX += Math.cos(angle) * strength;
        player.motionZ += Math.sin(angle) * strength;
        player.motionY += 0.8f + player.world.rand.nextFloat() * 0.5f;
    }

    private void spawnLightning(World world) {
        if (world.playerEntities.isEmpty()) return;
        EntityPlayer target = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
        int dx = world.rand.nextInt(32) - 16;
        int dz = world.rand.nextInt(32) - 16;
        BlockPos strike = new BlockPos(
                target.posX + dx,
                world.getHeight((int) target.posX + dx, (int) target.posZ + dz),
                target.posZ + dz);
        net.minecraft.entity.effect.EntityLightningBolt bolt = new net.minecraft.entity.effect.EntityLightningBolt(
                world, strike.getX(), strike.getY(), strike.getZ(), false);
        world.addWeatherEffect(bolt);
    }

    /**
     * Find the highest non-air block at x/z relative to the given base position.
     */
    private BlockPos getSurfacePos(World world, BlockPos base) {
        int x = base.getX();
        int z = base.getZ();
        int y = world.getHeight(x, z);
        if (y <= 0) return null;
        return new BlockPos(x, y, z);
    }
}
