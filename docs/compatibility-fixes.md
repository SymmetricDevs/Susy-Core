# Compatibility and generation fixes

Susy-Core owns the following fixes when the matching mods are present:

- Catwalks render-cache lookups use concurrent maps, weak references and chunk/world unload invalidation. Lookups do not generate chunks.
- Recurrent Complex's placement-avoidance checks use transformed world coordinates and retain the intended fallback surface.
- GregTech saved-grid heights are read from pending saves or Anvil metadata without constructing the saved chunks. Unsupported loaders or unreadable metadata fall back to the original behavior.
- Cross-chunk ore indicators wait for loaded, populated destination chunks. Pending work is saved per dimension and drains within a shared tick budget.
- IvToolkit structure caches allocate columns lazily, then promote to the original dense storage for dense access.
- Missing wrench ray hits return no side instead of crashing.
- Orphan iChunUtil renderer entities return zero brightness; attached entities keep their original brightness path.
- Client packet guards run after main-thread dispatch. They validate the actual inventory destination and reject updates for a missing or obsolete client world.

Grenade state persists across saves and spawn updates. Machine layouts handle empty inputs and preserve custom background offsets.

## Settings

Generation fixes use the `general.worldgen` category in `config/susy/susy.cfg`. The `enabled` option controls all generation fixes. Individual options are `gridMetadata`, `recurrentComplex`, `deferIndicators` and `adaptiveCache`. All default to `true`. Restart after changing settings.

Catwalks and crash guards are independent of the generation switch. Queued indicators continue draining even when new deferral is disabled. Pending work is saved per dimension in `susy_pending_indicators_v1.dat` using version-1 NBT. Include these files in world backups.

Compatibility modules load only when their target mods are present.

## Limits

Deferred indicators change population timing; order-dependent generation can change future terrain. Corrected placement checks can restore structures previously skipped. The adaptive cache improves sparse allocation but temporarily holds both representations during promotion and had about 4% dense-access overhead in the recorded microbenchmark. These changes do not establish that every generation stall or native-memory growth issue is fixed. Dedicated-server and long-session validation remain separate work.

License notices for the LGPL worldgen helpers and MIT Recurrent Complex/IvToolkit code are included under `META-INF/licenses`. Corresponding source is supplied with distributed review builds.
