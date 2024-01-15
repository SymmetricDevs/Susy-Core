package supersymmetry.api.stockinteraction;

import cam72cam.immersiverailroading.entity.*;
import cam72cam.immersiverailroading.items.ItemRollingStock;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.entity.ModdedEntity;
import cam72cam.mod.item.ItemStack;
import gregtech.client.utils.RenderBufferHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class StockHelperFunctions {
    // TODO: Remove redundant code, general refactor of existing code
    public static final String[] ClassNameMap =
        {
            "any",
            "locomotive",
            "tank",
            "freight",
            "coupleable",
        };

    public static final Class[] ClassMap =
        {
            EntityRollingStock.class,
            Locomotive.class,
            FreightTank.class,
            Freight.class,
            EntityCoupleableRollingStock.class,
        };

    public static final HashMap<String, Class> StrHashMap;
    static {
        StrHashMap = new HashMap<>();
        for(int i = 0; i < ClassNameMap.length; i++)
            StrHashMap.put(ClassNameMap[i], ClassMap[i]);
    }

    public static byte CycleFilter(byte current, boolean up)
    {
        return CycleFilter(current, up, (byte)ClassMap.length);
    }

    public static byte CycleFilter(byte current, boolean up, byte len)
    {
        byte addition = (byte)(up ? 1 : -1);
        current += addition + len;
        current %= len;
        return current;
    }

    //not used anymore since classes arent loaded from items, but from a preset class array
    public static ItemStack StackToBadStack(net.minecraft.item.ItemStack stack)
    {
        ItemStack irStack = new ItemStack(stack);
        return irStack;
    }

    //not used anymore since classes arent loaded from items, but from a preset class array
    public Class GetClass(net.minecraft.item.ItemStack stack)
    {
        return GetClass(StackToBadStack(stack));
    }

    //not used anymore since classes arent loaded from items, but from a preset class array
    public static Class GetClass(ItemStack stack)
    {
        ItemRollingStock.Data data = new ItemRollingStock.Data(stack);
        EntityRollingStockDefinition def = data.def;
        try
        {
            Field privateField = EntityRollingStockDefinition.class.getDeclaredField("type");
            privateField.setAccessible(true);
            Class stockClass = (Class)(privateField.get(def));
            return stockClass;
        }
        catch (NoSuchFieldException | SecurityException | IllegalAccessException | NullPointerException e)
        {
            return null;
        }
    }

    //for splitting an object into classes (class selection UI was planned, not class filters are ints)
    public static List<String> DecomposeClass(Object obj)
    {
        return DecomposeClass(obj.getClass());
    }

    public static List<String> DecomposeClass(Class clazz)
    {
        List<String> strList = new ArrayList<String>();
        String first = clazz.getSimpleName();
        first += ListInterfaces(clazz);
        strList.add(first);

        Class superClass = clazz.getSuperclass();
        while(superClass != null)
        {
            String nxt = superClass.getSimpleName();
            nxt += ListInterfaces(superClass);
            strList.add(nxt);
            superClass = superClass.getSuperclass();
        }

        return strList;
    }

    public static String ListInterfaces(Class clazz)
    {
        String str = " implements ";
        if (clazz.getInterfaces().length == 0)
        {
            return "";
        }
        for (Class c : clazz.getInterfaces())
        {
            str += c.getSimpleName() + ", ";
        }
        return (str.substring(0, str.length() - 2));
    }

    //mostly unused since filter stored as int
    public static boolean IsInFilter(Object stockEntity, String filter) {
        return GetClassFromString(filter).isInstance(stockEntity);
    }

    //mostly unused since filter stored as int
    public static Class GetClassFromString(String filter) {
        return StrHashMap.get(filter);
    }

    public static List<EntityRollingStock> getStocksInArea(World world, AxisAlignedBB box)
    {
        //get entities in box and filter for only wanted ones
        List<ModdedEntity> entities = world.getEntitiesWithinAABB(ModdedEntity.class, box);
        List<EntityRollingStock> stocks = entities
                .stream()
                .map(moddedEntity -> moddedEntity.getSelf())
                .filter(EntityRollingStock.class::isInstance)
                .map(EntityRollingStock.class::cast)
                .collect(Collectors.toList());
        return stocks;

    }

    public static AxisAlignedBB GetBox(Vec3i pos, EnumFacing facing, double width, double depth) {
        return GetBox(fromVec3i(pos), facing, width, depth);
    }

    public static AxisAlignedBB GetBox(Vector3f pos, EnumFacing facing, double width, double depth)
    {
        //get base position and facing directions, as well as both non facing directions
        Vector3f one = new Vector3f(1, 1, 1);
        Vector3f facingDir = fromVec3i(facing.getDirectionVec());
        Vector3f nonFacingDirs = Vector3f.sub(one, facingDir, null);

        double halfwidth = 0.5 * width;

        //get the position of the front of the block
        Vector3f facingDirHalved = copyVec(facingDir);
        facingDirHalved.scale(0.5f);
        Vector3f blockFront = Vector3f.add(pos, facingDirHalved, null);

        //get the vectors to add for local left down, and local up right
        Vector3f localLeftDown = copyVec(nonFacingDirs);
        localLeftDown.scale((float)halfwidth * -1.0f);
        Vector3f localRightUp = copyVec(nonFacingDirs);
        localRightUp.scale((float)halfwidth);

        //calculate the vector to add for the far corner
        Vector3f farVec = copyVec(facingDir);
        farVec.scale((float) depth);

        //calculate both corners and use to make bounding box
        Vector3f cornerA = Vector3f.add(blockFront, localLeftDown, null);
        Vector3f cornerB = Vector3f.add(blockFront, localRightUp, null);
        cornerB = Vector3f.add(cornerB, farVec, null);
        AxisAlignedBB box = new AxisAlignedBB(cornerA.getX(), cornerA.getY(), cornerA.getZ(), cornerB.getX(), cornerB.getY(), cornerB.getZ());
        return box;
    }

    public static Vector3f copyVec(Vector3f vec) {
        return new Vector3f(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3f fromVec3i(Vec3i pos)
    {
        return new Vector3f(pos.getX(), pos.getY(), pos.getZ());
    }

    public static void renderBoundingBox(AxisAlignedBB boundingBox) {
        // TODO: If anyone reads this that actually knows how to do opengl, please fix this
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        RenderBufferHelper.renderCubeFrame(bufferBuilder, boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ, 1F,0F,0F,0.4F);

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.color(1, 1, 1, 1);
    }
}
