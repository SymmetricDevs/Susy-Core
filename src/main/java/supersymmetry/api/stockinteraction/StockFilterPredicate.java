package supersymmetry.api.stockinteraction;

import cam72cam.immersiverailroading.entity.EntityRollingStock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public abstract class StockFilterPredicate {

    public abstract boolean test(EntityRollingStock entity);

    public CompoundPredicate or(StockFilterPredicate other) {
        CompoundPredicate combined = new CompoundPredicate(other);
        combined.merge(this);
        return combined;
    }

    public static boolean any(EntityRollingStock rollingStock, Collection<StockFilterPredicate> predicates) {
        return predicates.stream().anyMatch(predicate -> predicate.test(rollingStock));
    }

    public static boolean all(EntityRollingStock rollingStock, Collection<StockFilterPredicate> predicates) {
        return predicates.stream().anyMatch(predicate -> predicate.test(rollingStock));
    }

    public static class CompoundPredicate extends StockFilterPredicate {
        public List<StockFilterPredicate> subPredicates;

        public CompoundPredicate () {
            this.subPredicates = new ArrayList<>();
        }

        public CompoundPredicate (StockFilterPredicate other) {
            if(other instanceof CompoundPredicate compound) {
                this.subPredicates = compound.subPredicates;
            }
            else {
                this.subPredicates = new ArrayList<>();
                this.subPredicates.add(other);
            }
        }

        public void merge (StockFilterPredicate other) {
            if (other instanceof CompoundPredicate compound) {
                this.subPredicates.addAll(compound.subPredicates);
            }
            else {
                this.subPredicates.add(other);
            }
        }

        @Override
        public boolean test(EntityRollingStock entity) {
            return any(entity, this.subPredicates);
        }
    }

    public static class SimplePredicate extends StockFilterPredicate {
        public Predicate<EntityRollingStock> predicate;

        public SimplePredicate(Predicate<EntityRollingStock> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean test(EntityRollingStock entity) {
            return this.predicate.test(entity);
        }
    }

    public static class EntityClassPredicate extends StockFilterPredicate {
        public Class classToMatch;

        public EntityClassPredicate (Class cls) {
            this.classToMatch = cls;
        }

        @Override
        public boolean test(EntityRollingStock entity) {
            return entity.getClass() == classToMatch;
        }
    }

    public static abstract class StringMatcherPredicate extends StockFilterPredicate {

        private static String stringToMatch;

        public static String getStringToMatch() {
            return stringToMatch;
        }

        public static void setStringToMatch(String stringToMatch) {
            StringMatcherPredicate.stringToMatch = stringToMatch;
        }

        abstract String stringFromEntity (EntityRollingStock entity);

        @Override
        public boolean test(EntityRollingStock entity) {
            return getStringToMatch().equals(stringFromEntity(entity));
        }
    }

    public static class NameMatcherPredicate extends StringMatcherPredicate {
        @Override
        public String stringFromEntity(EntityRollingStock stock) {
            String name = stock.getDefinition().name();
            if (stock.tag != null && !stock.tag.isEmpty()) {
                name = stock.tag;
            }
            return name;
        }
    }

}
