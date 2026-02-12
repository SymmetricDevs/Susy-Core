package supersymmetry.common.command;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandMultiblock extends CommandBase {

    @Override
    public String getName() {
        return "multiblock";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/multiblock <x1> <y1> <z1> <x2> <y2> <z2>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 6) {
            sender.sendMessage(new TextComponentString("Usage: " + getUsage(sender)));
            return;
        }

        try {
            int x1 = Integer.parseInt(args[0]);
            int y1 = Integer.parseInt(args[1]);
            int z1 = Integer.parseInt(args[2]);
            int x2 = Integer.parseInt(args[3]);
            int y2 = Integer.parseInt(args[4]);
            int z2 = Integer.parseInt(args[5]);

            World world = sender.getEntityWorld();

            // Normalize coordinates
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            // Generate the multiblock pattern
            String pattern = generateMultiblockPattern(world, minX, minY, minZ, maxX, maxY, maxZ);

            // Send to player
            sender.sendMessage(new TextComponentString("§aMultiblock pattern generated! Check console/logs."));
            System.out.println("=== MULTIBLOCK PATTERN ===");
            System.out.println(pattern);
            System.out.println("=========================");

        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString("§cInvalid coordinates! Use integers only."));
        }
    }

    private String generateMultiblockPattern(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        StringBuilder pattern = new StringBuilder();

        Map<String, Character> blockToChar = new HashMap<>();
        Map<String, String> blockPredicates = new HashMap<>();
        char currentChar = 'A';

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        // Store the 3D block structure [Y][Z][X] for proper aisle ordering
        char[][][] structure = new char[sizeY][sizeZ][sizeX];

        // Scan all blocks
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    TileEntity te = world.getTileEntity(pos);

                    String blockKey = getBlockKey(state, te);

                    if (!blockToChar.containsKey(blockKey)) {
                        blockToChar.put(blockKey, currentChar);
                        blockPredicates.put(String.valueOf(currentChar), getBlockPredicate(state, te));
                        currentChar++;
                    }

                    int relX = x - minX;
                    int relY = y - minY;
                    int relZ = z - minZ;
                    structure[relY][relZ][relX] = blockToChar.get(blockKey);  // Changed indexing order
                }
            }
        }

        // Generate the pattern code
        pattern.append("@Override\n");
        pattern.append("protected BlockPattern createStructurePattern() {\n");
        pattern.append("    return FactoryBlockPattern.start(RIGHT, FRONT, UP)\n");

        // Generate aisles - each aisle is a horizontal Z-slice at a given Y level
        for (int y = 0; y < sizeY; y++) {
            pattern.append("        .aisle(");
            for (int z = 0; z < sizeZ; z++) {
                pattern.append("\"");
                for (int x = 0; x < sizeX; x++) {
                    pattern.append(structure[y][z][x]);  // Changed indexing order
                }
                pattern.append("\"");
                if (z < sizeZ - 1) {
                    pattern.append(", ");
                }
            }
            pattern.append(")\n");
        }

        // Generate where clauses
        for (Map.Entry<String, String> entry : blockPredicates.entrySet()) {
            pattern.append("        .where('").append(entry.getKey()).append("', ");
            pattern.append(entry.getValue()).append(")\n");
        }

        pattern.append("        .build();\n");
        pattern.append("}\n");

        return pattern.toString();
    }

    private String getBlockKey(IBlockState state, TileEntity te) {
        StringBuilder key = new StringBuilder();
        key.append(state.getBlock().getRegistryName());
        key.append("@").append(state.getBlock().getMetaFromState(state));

        if (te != null) {
            key.append("#TE:").append(te.getClass().getSimpleName());
        }

        return key.toString();
    }

    private String getBlockPredicate(IBlockState state, TileEntity te) {
        String registryName = state.getBlock().getRegistryName().toString();
        int meta = state.getBlock().getMetaFromState(state);

        // Handle air blocks
        if (state.getBlock().getRegistryName().toString().equals("minecraft:air")) {
            return "any()";
        }

        // Handle GregTech MetaTileEntities
        if (te != null && te.getClass().getName().contains("gregtech")) {
            String teName = te.getClass().getSimpleName();

            // Try to detect if it's a controller
            if (teName.toLowerCase().contains("controller")) {
                return "selfPredicate()";
            }

            // Try to detect common hatches/buses
            if (teName.toLowerCase().contains("hatch") || teName.toLowerCase().contains("bus")) {
                return "autoAbilities()";
            }

            return "states(/* MetaTileEntity: " + teName + " */)";
        }

        // Generate standard block predicate
        return "states(" + registryName + ".getDefaultState()) // Meta: " + meta;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP level 2 required
    }
}
