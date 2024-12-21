package ru.ricardocraft.client.utils.helper;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class VerifyHelper {

    public static final IntPredicate POSITIVE = i -> i > 0;

    public static final IntPredicate NOT_NEGATIVE = i -> i >= 0;

    public static final LongPredicate L_NOT_NEGATIVE = l -> l >= 0;

    public static final Predicate<String> NOT_EMPTY = s -> !s.isEmpty();

    private static final Pattern SERVERID_PATTERN = Pattern.compile("-?[0-9a-f]{1,40}");


    private VerifyHelper() {
    }

    public static <K, V> V getMapValue(Map<K, V> map, K key, String error) {
        return verify(map.get(key), Objects::nonNull, error);
    }

    public static boolean isValidIDName(String name) {
        return !name.isEmpty() && name.length() <= 255 && name.chars().allMatch(VerifyHelper::isValidIDNameChar);
    }

    public static boolean isValidIDNameChar(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '-' || ch == '_';
    }

    public static boolean isValidServerID(CharSequence serverID) {
        return SERVERID_PATTERN.matcher(serverID).matches();
    }

    public static <K, V> void putIfAbsent(Map<K, V> map, K key, V value, String error) {
        verify(map.putIfAbsent(key, value), Objects::isNull, error);
    }

    public static <T> T verify(T object, Predicate<T> predicate, String error) {
        if (predicate.test(object))
            return object;
        throw new IllegalArgumentException(error);
    }

    public static void verifyIDName(String name) {
        verify(name, VerifyHelper::isValidIDName, String.format("Invalid name: '%s'", name));
    }

    public static int verifyInt(int i, IntPredicate predicate, String error) {
        if (predicate.test(i))
            return i;
        throw new IllegalArgumentException(error);
    }

    public static long verifyLong(long l, LongPredicate predicate, String error) {
        if (predicate.test(l))
            return l;
        throw new IllegalArgumentException(error);
    }

    public static String verifyServerID(String serverID) {
        return verify(serverID, VerifyHelper::isValidServerID,
                String.format("Invalid server ID: '%s'", serverID));
    }

}
