package ru.ricardocraft.backend.base.helper;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public final class VerifyHelper {

    public static final IntPredicate POSITIVE = i -> i > 0;

    public static final IntPredicate NOT_NEGATIVE = i -> i >= 0;

    public static final LongPredicate L_NOT_NEGATIVE = l -> l >= 0;

    public static final Predicate<String> NOT_EMPTY = s -> !s.isEmpty();

    private VerifyHelper() {
    }

    public static <K, V> void putIfAbsent(Map<K, V> map, K key, V value, String error) {
        verify(map.putIfAbsent(key, value), Objects::isNull, error);
    }

    public static <T> T verify(T object, Predicate<T> predicate, String error) {
        if (predicate.test(object))
            return object;
        throw new IllegalArgumentException(error);
    }

    public static int verifyInt(int i, IntPredicate predicate, String error) {
        if (predicate.test(i))
            return i;
        throw new IllegalArgumentException(error);
    }
}
