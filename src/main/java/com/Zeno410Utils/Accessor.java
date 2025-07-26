package com.Zeno410Utils;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import java.lang.reflect.Field;

public class Accessor<ObjectType, FieldType> {
    public static final Zeno410Logger logger = new Zeno410Logger("Accessor");
    private Field field;
    private String fieldName;

    public Accessor(String _fieldName) {
        this.fieldName = _fieldName;
    }

    private Field field(ObjectType example) {
        Class classObject = example.getClass();
        if (this.field == null) {
            try {
                this.setField(classObject);
            } catch (IllegalAccessException var4) {
                throw new RuntimeException(var4);
            }
        }

        return this.field;
    }

    void setField(Class classObject) throws IllegalAccessException {
        StringBuilder builder = new StringBuilder();

        this.fieldName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(classObject.getName(), this.fieldName, this.fieldName);

        if (this.fieldName == "field_75910_b") {
            this.fieldName = "biomePatternGeneratorChain";
        } else if (this.fieldName == "field_76944_d") {
            this.fieldName = "genBiomes";
        }

        do {
            Field[] fields = classObject.getDeclaredFields();

            for(int i = 0; i < fields.length; ++i) {
                if (fields[i].getName().contains(this.fieldName)) {
                    this.field = fields[i];
                    this.field.setAccessible(true);
                    return;
                }
            }

            classObject = classObject.getSuperclass();
        } while(classObject != Object.class);

        throw new RuntimeException(this.fieldName + " not found in class " + classObject.getName() + " " + builder.toString());
    }

    public FieldType get(ObjectType object) {
        try {
            return (FieldType) this.field(object).get(object);
        } catch (IllegalArgumentException var3) {
            throw new RuntimeException(var3);
        } catch (IllegalAccessException var4) {
            throw new RuntimeException(var4);
        }
    }

    public void setField(ObjectType object, FieldType fieldValue) {
        try {
            this.field(object).set(object, fieldValue);
        } catch (IllegalArgumentException var4) {
            throw new RuntimeException(var4);
        } catch (IllegalAccessException var5) {
            throw new RuntimeException(var5);
        }
    }
}
