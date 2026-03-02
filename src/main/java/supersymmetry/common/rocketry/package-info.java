/**
 * Rocketry and lander spawn queue system.
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <h3>Basic Spawn (no inventory):</h3>
 * 
 * <pre>
 * {@code
 * // Queue a lander to spawn in 5 seconds (100 ticks) at coordinates (100, 256, 200) in the overworld
 * LanderSpawnManager.queueLanderSpawn(world, 0, 100, 256, 200, 100);
 * }
 * </pre>
 * 
 * <h3>Spawn with BlockPos:</h3>
 * 
 * <pre>
 * 
 * {
 *     &#64;code
 *     BlockPos landingPadPos = new BlockPos(100, 64, 200);
 *     // Queue a lander to spawn in 10 seconds (200 ticks)
 *     LanderSpawnManager.queueLanderSpawn(world, 0, landingPadPos, 200);
 * }
 * </pre>
 * 
 * <h3>Asteroid Harvesting Mission (with inventory):</h3>
 * 
 * <pre>
 * 
 * {
 *     &#64;code
 *     // Create an inventory with harvested resources
 *     ItemStackHandler harvestedCargo = new ItemStackHandler(36);
 *     harvestedCargo.setStackInSlot(0, new ItemStack(Items.IRON_ORE, 64));
 *     harvestedCargo.setStackInSlot(1, new ItemStack(Items.GOLD_ORE, 32));
 * 
 *     // Queue lander to return from asteroid mining in 30 seconds (600 ticks)
 *     BlockPos landingPadPos = new BlockPos(100, 64, 200);
 *     LanderSpawnManager.queueLanderSpawnWithInventory(world, 0, landingPadPos, 600, harvestedCargo);
 * }
 * </pre>
 * 
 * <h3>Cross-Dimensional Spawn:</h3>
 * 
 * <pre>
 * 
 * {
 *     &#64;code
 *     // Queue a lander to spawn on the Moon (dimension -28) in 15 seconds
 *     int moonDimension = -28;
 *     LanderSpawnManager.queueLanderSpawn(world, moonDimension, 0, 256, 0, 300);
 * }
 * </pre>
 * 
 * <h3>Check Queue Status:</h3>
 * 
 * <pre>
 * {@code
 * if (LanderSpawnManager.hasQueuedSpawns(world)) {
 *     int queueSize = LanderSpawnManager.getQueueSize(world);
 *     System.out.println("There are " + queueSize + " landers queued to spawn");
 * }
 * }
 * </pre>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Persistent storage - survives server restarts</li>
 * <li>Cross-dimensional spawning support</li>
 * <li>Inventory support for cargo missions</li>
 * <li>Arbitrary tick delays</li>
 * <li>Multiple simultaneous queued spawns</li>
 * </ul>
 * 
 * @see LanderSpawnManager
 * @see LanderSpawnQueue
 * @see LanderSpawnEntry
 */
package supersymmetry.common.rocketry;
