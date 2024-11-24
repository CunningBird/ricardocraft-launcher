package ru.ricardocraft.client.core.hasher;

import java.util.Collection;

public final class FileNameMatcher {
    // Instance
    private final String[] update;
    private final String[] verify;
    private final String[] exclusions;

    public FileNameMatcher(String[] update, String[] verify, String[] exclusions) {
        this.update = update;
        this.verify = verify;
        this.exclusions = exclusions;
    }

    private static boolean anyMatch(String[] entries, Collection<String> path) {
        //return path.stream().anyMatch(e -> Arrays.stream(entries).anyMatch(p -> p.endsWith(e)));
        String jpath = String.join("/", path);
        for (String e : entries) {
            /*String[] split = e.split("/");
            //int index = 0;
            //for(String p : path)
            //{
            //    if(index>=split.length)
                {
                    return true;
                }
                if(!p.equals(split[index])) {
                    break;
                }
                index++;
            }*/
            if (jpath.startsWith(e)) return true;
        }
        return false;
    }

    public boolean shouldUpdate(Collection<String> path) {
        return (anyMatch(update, path) || anyMatch(verify, path)) && !anyMatch(exclusions, path);
    }


}