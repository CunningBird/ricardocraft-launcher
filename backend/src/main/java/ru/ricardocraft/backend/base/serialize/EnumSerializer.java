package ru.ricardocraft.backend.base.serialize;

import ru.ricardocraft.backend.base.helper.VerifyHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class EnumSerializer<E extends Enum<?> & EnumSerializer.Itf> {
    private final Map<Integer, E> map = new HashMap<>(16);


    public EnumSerializer(Class<E> clazz) {
        for (E e : clazz.getEnumConstants())
            VerifyHelper.putIfAbsent(map, e.getNumber(), e, "Duplicate number for enum constant " + e.name());
    }

    public static void write(HOutput output, Itf itf) throws IOException {
        output.writeVarInt(itf.getNumber());
    }

    public E read(HInput input) throws IOException {
        int n = input.readVarInt();
        return getMapValue(map, n, "Unknown enum number: " + n);
    }

    private <K, V> V getMapValue(Map<K, V> map, K key, String error) {
        return VerifyHelper.verify(map.get(key), Objects::nonNull, error);
    }


    @FunctionalInterface
    public interface Itf {

        int getNumber();
    }
}
