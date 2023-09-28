package supersymmetry.asm;

public class StockLoaderBridgeClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] bytecode) {
        return defineClass(name, bytecode, 0, bytecode.length);
    }
}
