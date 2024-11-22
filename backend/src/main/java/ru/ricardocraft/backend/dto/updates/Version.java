package ru.ricardocraft.backend.dto.updates;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
public class Version implements Comparable<Version> {

    private long[] data;
    private String name = "original";
    private String original;

    public static Version of(String string) {
        String tmp = string.replaceAll("[^.0-9]", "."); // Replace any non-digit character to .
        String[] list = tmp.split("\\.");
        return new Version(Arrays.stream(list)
                .filter(e -> !e.isEmpty()) // Filter ".."
                .mapToLong(Long::parseLong).toArray(), string);
    }

    private Version(long[] data, String str) {
        this.data = data;
        this.original = str;
    }

    @Override
    public int compareTo(Version some) {
        int result = 0;
        if (data.length == some.data.length) {
            for (int i = 0; i < data.length; ++i) {
                result = Long.compare(data[i], some.data[i]);
                if (result != 0) return result;
            }
        } else if (data.length < some.data.length) {
            for (int i = 0; i < data.length; ++i) {
                result = Long.compare(data[i], some.data[i]);
                if (result != 0) return result;
            }
            for (int i = data.length; i < some.data.length; ++i) {
                if (some.data[i] > 0) return -1;
            }
        } else {
            for (int i = 0; i < some.data.length; ++i) {
                result = Long.compare(data[i], some.data[i]);
                if (result != 0) return result;
            }
            for (int i = some.data.length; i < data.length; ++i) {
                if (data[i] > 0) return 1;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return original;
    }
}