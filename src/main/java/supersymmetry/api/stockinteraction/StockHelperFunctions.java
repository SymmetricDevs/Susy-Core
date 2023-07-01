package supersymmetry.api.stockinteraction;

import cam72cam.immersiverailroading.entity.*;
import cam72cam.immersiverailroading.items.ItemRollingStock;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.entity.CustomEntity;
import cam72cam.mod.entity.ModdedEntity;
import cam72cam.mod.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector3f;

import javax.vecmath.Vector3d;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class StockHelperFunctions
{
    public static ItemStack StackToBadStack(net.minecraft.item.ItemStack stack)
    {
        ItemStack irStack = new ItemStack(stack);
        return irStack;
    }

    public Class GetClass(net.minecraft.item.ItemStack stack)
    {
        return GetClass(StackToBadStack(stack));
    }

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

    public static boolean IsInFilter(Object stockEntity, String filter) {
        return GetClassFromString(filter).isInstance(stockEntity);
    }

    public static Class GetClassFromString(String filter) {
        switch(filter) {
            case "":
                return EntityRollingStock.class;
            case "locomotive":
                return Locomotive.class;
            case "tank":
                return FreightTank.class;
            case "freight":
                return Freight.class;
            case "coupleable":
                return EntityCoupleableRollingStock.class;
        }
        return null;
    }

    public static List<EntityRollingStock> GetAnyStockInArea(EnumFacing facing, IStockInteractor interactor, World world)
    {
        return GetStockInArea(EntityRollingStock.class, facing, interactor, world);
    }

    public static <T extends EntityRollingStock> List<T> GetStockInArea(Class<T> checkClass, EnumFacing facing, IStockInteractor interactor, World world)
    {
        //get base position and facing directions, as well as both non facing directions
        Vector3f pos = fromVec3i(interactor.GetMetaTileEntity().getPos());
        Vector3f one = new Vector3f(1, 1, 1);
        Vector3f facingDir = fromVec3i(facing.getDirectionVec());
        Vector3f nonFacingDirs = Vector3f.sub(one, facingDir, null);

        double width = interactor.GetInteractionArea().x;
        double depth = interactor.GetInteractionArea().z;
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

        //get entities in box and filter for only wanted ones
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, box);
        List<T> stocks = new ArrayList<T>();
        entities.forEach(ent ->
        {
            if(ent instanceof ModdedEntity)
            {
                //get the CustomEntity : Entity off of the modded entity, then check if its the right class
                CustomEntity customEntity = ((ModdedEntity)ent).getSelf();
                if(customEntity.getClass().isAssignableFrom(checkClass))
                {
                    stocks.add((T)customEntity);
                }
            }
        });
        return stocks;
    }


    public static Vector3f copyVec(Vector3f vec) {
        return new Vector3f(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3f fromVec3i(Vec3i pos)
    {

        return new Vector3f(pos.getX(), pos.getY(), pos.getZ());
    }
}
