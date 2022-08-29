package works.hop.generate;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public interface Entity {

    MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    default <T> void setProperty(String name, Class<?> type, T value) throws Throwable {
        String setter = String.format("set%s%s", Character.toUpperCase(name.charAt(0)), name.substring(1));
        MethodType mt = MethodType.methodType(void.class, type);
        MethodHandle mh = lookup.findVirtual(this.getClass(), setter, mt);
        mh.invoke(this, value);
    }

    default Object getProperty(String name, Class<?> type) throws Throwable {
        String getter = String.format("get%s%s", Character.toUpperCase(name.charAt(0)), name.substring(1));
        MethodType mt = MethodType.methodType(type);
        MethodHandle mh = lookup.findVirtual(this.getClass(), getter, mt);
        return mh.invoke(this);
    }

    static <T extends Entity> T instance(Class<?> type) throws Throwable {
        MethodType noArgsConstructor = MethodType.methodType(void.class);
        MethodHandle noArgConstructorHandle = lookup.findConstructor(type, noArgsConstructor);
        return (T) noArgConstructorHandle.invoke();
    }
}
