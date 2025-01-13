package supersymmetry.api.stockinteraction;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.entity.FreightTank;
import cam72cam.immersiverailroading.entity.LocomotiveDiesel;
import cam72cam.immersiverailroading.entity.LocomotiveSteam;
import supersymmetry.common.entities.EntityTunnelBore;

public class StockFilterPredicates {
    // TODO: Transporter erector once that is merged.

    public static StockFilterPredicate ANY = new StockFilterPredicate.SimplePredicate(entity -> true);
    public static StockFilterPredicate FREIGHT = new StockFilterPredicate.EntityClassPredicate(Freight.class);
    public static StockFilterPredicate DIESEL_LOCOMOTIVE = new StockFilterPredicate.EntityClassPredicate(LocomotiveDiesel.class);
    public static StockFilterPredicate STEAM_LOCOMOTIVE = new StockFilterPredicate.EntityClassPredicate(LocomotiveSteam.class);
    public static StockFilterPredicate LOCOMOTIVE = DIESEL_LOCOMOTIVE.or(STEAM_LOCOMOTIVE);
    public static StockFilterPredicate FREIGHT_TANK = new StockFilterPredicate.EntityClassPredicate(FreightTank.class);
    public static StockFilterPredicate TUNNEL_BORE = new StockFilterPredicate.EntityClassPredicate(EntityTunnelBore.class);

}
