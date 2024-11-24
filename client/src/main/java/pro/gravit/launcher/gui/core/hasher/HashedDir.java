package pro.gravit.launcher.gui.core.hasher;

import pro.gravit.launcher.gui.core.LauncherNetworkAPI;
import pro.gravit.launcher.gui.core.serialize.HInput;
import pro.gravit.launcher.gui.core.serialize.HOutput;
import pro.gravit.launcher.gui.core.serialize.stream.EnumSerializer;
import pro.gravit.launcher.gui.utils.helper.IOHelper;
import pro.gravit.launcher.gui.utils.helper.VerifyHelper;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;

public final class HashedDir extends pro.gravit.launcher.gui.core.hasher.HashedEntry {
    @LauncherNetworkAPI
    private final Map<String, pro.gravit.launcher.gui.core.hasher.HashedEntry> map = new HashMap<>(32);

    public HashedDir() {
    }

    public HashedDir(HInput input) throws IOException {
        int entriesCount = input.readLength(0);
        for (int i = 0; i < entriesCount; i++) {
            String name = IOHelper.verifyFileName(input.readString(255));

            // Read entry
            pro.gravit.launcher.gui.core.hasher.HashedEntry entry;
            Type type = Type.read(input);
            entry = switch (type) {
                case FILE -> new HashedFile(input);
                case DIR -> new HashedDir(input);
            };

            // Try add entry to map
            VerifyHelper.putIfAbsent(map, name, entry, String.format("Duplicate dir entry: '%s'", name));
        }
    }


    public HashedDir(Path dir, pro.gravit.launcher.gui.core.hasher.FileNameMatcher matcher, boolean allowSymlinks, boolean digest) throws IOException {
        IOHelper.walk(dir, new HashFileVisitor(dir, matcher, allowSymlinks, digest), true);
    }

    public Diff diff(HashedDir other, pro.gravit.launcher.gui.core.hasher.FileNameMatcher matcher) {
        HashedDir mismatch = sideDiff(other, matcher, new LinkedList<>(), true);
        HashedDir extra = other.sideDiff(this, matcher, new LinkedList<>(), false);
        return new Diff(mismatch, extra);
    }

    public Diff compare(HashedDir other, pro.gravit.launcher.gui.core.hasher.FileNameMatcher matcher) {
        HashedDir mismatch = sideDiff(other, matcher, new LinkedList<>(), true);
        HashedDir extra = other.sideDiff(this, matcher, new LinkedList<>(), false);
        return new Diff(mismatch, extra);
    }

    public void remove(String name) {
        map.remove(name);
    }

    public void moveTo(String elementName, HashedDir target, String targetElementName) {
        pro.gravit.launcher.gui.core.hasher.HashedEntry entry = map.remove(elementName);
        target.map.put(targetElementName, entry);
    }

    public FindRecursiveResult findRecursive(String path) {
        StringTokenizer t = new StringTokenizer(path, "/");
        HashedDir current = this;
        pro.gravit.launcher.gui.core.hasher.HashedEntry entry = null;
        String name = null;
        while (t.hasMoreTokens()) {
            name = t.nextToken();
            pro.gravit.launcher.gui.core.hasher.HashedEntry e = current.map.get(name);
            if (e == null && !t.hasMoreTokens()) {
                break;
            }
            if (e == null) {
                throw new RuntimeException(String.format("Directory %s not found", name));
            }
            if (e.getType() == Type.DIR) {
                if (!t.hasMoreTokens()) {
                    entry = e;
                    break;
                } else {
                    current = ((HashedDir) e);
                }
            } else {
                entry = e;
                break;
            }
        }
        return new FindRecursiveResult(current, entry, name);
    }

    public pro.gravit.launcher.gui.core.hasher.HashedEntry getEntry(String name) {
        return map.get(name);
    }

    @Override
    public Type getType() {
        return Type.DIR;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Map<String, pro.gravit.launcher.gui.core.hasher.HashedEntry> map() {
        return Collections.unmodifiableMap(map);
    }

    public pro.gravit.launcher.gui.core.hasher.HashedEntry resolve(Iterable<String> path) {
        pro.gravit.launcher.gui.core.hasher.HashedEntry current = this;
        for (String pathEntry : path) {
            if (current instanceof HashedDir) {
                current = ((HashedDir) current).map.get(pathEntry);
                continue;
            }
            return null;
        }
        return current;
    }

    private HashedDir sideDiff(HashedDir other, pro.gravit.launcher.gui.core.hasher.FileNameMatcher matcher, Deque<String> path, boolean mismatchList) {
        HashedDir diff = new HashedDir();
        for (Entry<String, pro.gravit.launcher.gui.core.hasher.HashedEntry> mapEntry : map.entrySet()) {
            String name = mapEntry.getKey();
            pro.gravit.launcher.gui.core.hasher.HashedEntry entry = mapEntry.getValue();
            path.add(name);

            // Should update?
            boolean shouldUpdate = matcher == null || matcher.shouldUpdate(path);

            // Not found or of different type
            Type type = entry.getType();
            pro.gravit.launcher.gui.core.hasher.HashedEntry otherEntry = other.map.get(name);
            if (otherEntry == null || otherEntry.getType() != type) {
                if (shouldUpdate || mismatchList && otherEntry == null) {
                    diff.map.put(name, entry);

                    // Should be deleted!
                    if (!mismatchList)
                        entry.flag = true;
                }
                path.removeLast();
                continue;
            }

            // Compare entries based on type
            switch (type) {
                case FILE:
                    HashedFile file = (HashedFile) entry;
                    HashedFile otherFile = (HashedFile) otherEntry;
                    if (mismatchList && shouldUpdate && !file.isSame(otherFile))
                        diff.map.put(name, entry);
                    break;
                case DIR:
                    HashedDir dir = (HashedDir) entry;
                    HashedDir otherDir = (HashedDir) otherEntry;
                    if (mismatchList || shouldUpdate) { // Maybe isn't need to go deeper?
                        HashedDir mismatch = dir.sideDiff(otherDir, matcher, path, mismatchList);
                        if (!mismatch.isEmpty())
                            diff.map.put(name, mismatch);
                    }
                    break;
                default:
                    throw new AssertionError("Unsupported hashed entry type: " + type.name());
            }

            // Remove this path entry
            path.removeLast();
        }
        return diff;
    }

    public HashedDir sideCompare(HashedDir other, pro.gravit.launcher.gui.core.hasher.FileNameMatcher matcher, Deque<String> path, boolean mismatchList) {
        HashedDir diff = new HashedDir();
        for (Entry<String, pro.gravit.launcher.gui.core.hasher.HashedEntry> mapEntry : map.entrySet()) {
            String name = mapEntry.getKey();
            pro.gravit.launcher.gui.core.hasher.HashedEntry entry = mapEntry.getValue();
            path.add(name);

            // Should update?
            boolean shouldUpdate = matcher == null || matcher.shouldUpdate(path);

            // Not found or of different type
            Type type = entry.getType();
            pro.gravit.launcher.gui.core.hasher.HashedEntry otherEntry = other.map.get(name);
            if (otherEntry == null || otherEntry.getType() != type) {
                if (shouldUpdate || mismatchList && otherEntry == null) {
                    diff.map.put(name, entry);

                    // Should be deleted!
                    if (!mismatchList)
                        entry.flag = true;
                }
                path.removeLast();
                continue;
            }

            // Compare entries based on type
            switch (type) {
                case FILE:
                    HashedFile file = (HashedFile) entry;
                    HashedFile otherFile = (HashedFile) otherEntry;
                    if (mismatchList && shouldUpdate && file.isSame(otherFile))
                        diff.map.put(name, entry);
                    break;
                case DIR:
                    HashedDir dir = (HashedDir) entry;
                    HashedDir otherDir = (HashedDir) otherEntry;
                    if (mismatchList || shouldUpdate) { // Maybe isn't need to go deeper?
                        HashedDir mismatch = dir.sideCompare(otherDir, matcher, path, mismatchList);
                        if (!mismatch.isEmpty())
                            diff.map.put(name, mismatch);
                    }
                    break;
                default:
                    throw new AssertionError("Unsupported hashed entry type: " + type.name());
            }

            // Remove this path entry
            path.removeLast();
        }
        return diff;
    }

    @Override
    public long size() {
        return map.values().stream().mapToLong(pro.gravit.launcher.gui.core.hasher.HashedEntry::size).sum();
    }

    @Override
    public void write(HOutput output) throws IOException {
        Set<Entry<String, pro.gravit.launcher.gui.core.hasher.HashedEntry>> entries = map.entrySet();
        output.writeLength(entries.size(), 0);
        for (Entry<String, pro.gravit.launcher.gui.core.hasher.HashedEntry> mapEntry : entries) {
            output.writeString(mapEntry.getKey(), 255);

            // Write hashed entry
            pro.gravit.launcher.gui.core.hasher.HashedEntry entry = mapEntry.getValue();
            EnumSerializer.write(output, entry.getType());
            entry.write(output);
        }
    }

    public void walk(CharSequence separator, WalkCallback callback) throws IOException {
        String append = "";
        walk(append, separator, callback, true);
    }

    private WalkAction walk(String append, CharSequence separator, WalkCallback callback, boolean noSeparator) throws IOException {
        for (Entry<String, pro.gravit.launcher.gui.core.hasher.HashedEntry> entry : map.entrySet()) {
            pro.gravit.launcher.gui.core.hasher.HashedEntry e = entry.getValue();
            if (e.getType() == Type.FILE) {
                if (noSeparator) {
                    WalkAction a = callback.walked(append + entry.getKey(), entry.getKey(), e);
                    if (a == WalkAction.STOP) return a;
                } else {
                    WalkAction a = callback.walked(append + separator + entry.getKey(), entry.getKey(), e);
                    if (a == WalkAction.STOP) return a;
                }
            } else {
                String newAppend;
                if (noSeparator) newAppend = append + entry.getKey();
                else newAppend = append + separator + entry.getKey();
                WalkAction a = callback.walked(newAppend, entry.getKey(), e);
                if (a == WalkAction.STOP) return a;
                a = ((HashedDir) e).walk(newAppend, separator, callback, false);
                if (a == WalkAction.STOP) return a;
            }
        }
        return WalkAction.CONTINUE;
    }

    public enum WalkAction {
        STOP, CONTINUE
    }

    @FunctionalInterface
    public interface WalkCallback {
        WalkAction walked(String path, String name, pro.gravit.launcher.gui.core.hasher.HashedEntry entry) throws IOException;
    }

    public static class FindRecursiveResult {
        public final HashedDir parent;
        public final pro.gravit.launcher.gui.core.hasher.HashedEntry entry;
        public final String name;

        public FindRecursiveResult(HashedDir parent, HashedEntry entry, String name) {
            this.parent = parent;
            this.entry = entry;
            this.name = name;
        }
    }

    public static final class Diff {

        public final HashedDir mismatch;

        public final HashedDir extra;

        private Diff(HashedDir mismatch, HashedDir extra) {
            this.mismatch = mismatch;
            this.extra = extra;
        }


        public boolean isSame() {
            return mismatch.isEmpty() && extra.isEmpty();
        }
    }

    private final class HashFileVisitor extends SimpleFileVisitor<Path> {
        private final Path dir;
        private final pro.gravit.launcher.gui.core.hasher.FileNameMatcher matcher;
        private final boolean allowSymlinks;
        private final boolean digest;
        private final Deque<String> path = new LinkedList<>();
        private final Deque<HashedDir> stack = new LinkedList<>();
        // State
        private HashedDir current = HashedDir.this;

        private HashFileVisitor(Path dir, FileNameMatcher matcher, boolean allowSymlinks, boolean digest) {
            this.dir = dir;
            this.matcher = matcher;
            this.allowSymlinks = allowSymlinks;
            this.digest = digest;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            FileVisitResult result = super.postVisitDirectory(dir, exc);
            if (this.dir.equals(dir))
                return result;

            // Add directory to parent
            HashedDir parent = stack.removeLast();
            parent.map.put(path.removeLast(), current);
            current = parent;

            // We're done
            return result;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileVisitResult result = super.preVisitDirectory(dir, attrs);
            if (this.dir.equals(dir))
                return result;

            // Verify is not symlink
            // Symlinks was disallowed because modification of it's destination are ignored by DirWatcher
            if (!allowSymlinks && attrs.isSymbolicLink())
                throw new SecurityException("Symlinks are not allowed");

            // Add child
            stack.add(current);
            current = new HashedDir();
            path.add(IOHelper.getFileName(dir));

            // We're done
            return result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // Verify is not symlink
            if (!allowSymlinks && attrs.isSymbolicLink())
                throw new SecurityException("Symlinks are not allowed");

            // Add file (may be unhashed, if exclusion)
            path.add(IOHelper.getFileName(file));
            boolean doDigest = digest && (matcher == null || matcher.shouldUpdate(path));
            current.map.put(path.removeLast(), new HashedFile(file, attrs.size(), doDigest));
            return super.visitFile(file, attrs);
        }
    }
}
