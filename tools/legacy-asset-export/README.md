# Legacy molecule and alloy resources

## Exporting generated data

The exporter copies generated GTCEu molecule and alloy drawings from the newer
`gregtech-molecule-drawings` checkout into Susy-Core's 1.12 resource tree. It
rewrites the material namespace from `gtceu` to the legacy `gregtech` namespace
and writes explicit indexes, since the 1.12 resource manager cannot enumerate
resources. At runtime, the loaders resolve indexed IDs against the 1.12
material registry and skip materials that do not exist in that version.

From the `Susy-Core` directory, run:

```text
gradle -p tools/legacy-asset-export exportLegacyAssets
```

By default, the source is expected at the neighboring
`../gregtech-molecule-drawings/src/generated/resources/assets` path. Override
`upstreamAssets` or `targetAssets` with Gradle project properties when needed.
Other source namespaces are listed in
`src/main/resources/assets/susy/unmapped-source-assets.txt` for later review.

## Adding a custom chemical drawing

Hand-authored files live in `assets/susy/molecules/custom/` or
`assets/susy/alloys/custom/`. Their entries go in `custom-index.json`; the
exporter only rewrites generated root files and `index.json`, so custom files
and indexes remain intact. Load order is generated first, custom second, so a
custom entry with the same material ID overrides the generated drawing.

Use the material's full registry ID. For example, a Susy material named
`my_chemical` has ID `susy:my_chemical`; a GregTech material uses
`gregtech:<material_name>`. The material must already be registered in the
1.12 material manager.

For a molecule, add
`src/main/resources/assets/susy/molecules/custom/my_chemical.json`:

```json
{
  "contents": [
    {"type": "atom", "index": 0, "element": "C", "x": 0, "y": 0},
    {"type": "atom", "index": 1, "element": "O", "x": 1, "y": 0},
    {"type": "bond", "a": 0, "b": 1, "lines": ["solid", "solid"]}
  ]
}
```

Add this entry to
`src/main/resources/assets/susy/molecules/custom-index.json`:

```json
{
  "molecules": [
    {"material": "susy:my_chemical", "file": "custom/my_chemical.json"}
  ]
}
```

For an alloy, add
`src/main/resources/assets/susy/alloys/custom/my_alloy.json`:

```json
{
  "components": [
    ["gregtech:iron", 3],
    ["gregtech:carbon", 1]
  ]
}
```

Then add `{ "alloys": [{"material":"susy:my_alloy", "file":"custom/my_alloy.json"}] }`
to `src/main/resources/assets/susy/alloys/custom-index.json`. Component
amounts are positive counts. Use `{ "derive": true }` to use the registered
material's composition recursively instead.

Keep the `custom` subdirectory in the index path. Generated files use the
directory root, so matching filenames cannot overwrite hand-authored files.
