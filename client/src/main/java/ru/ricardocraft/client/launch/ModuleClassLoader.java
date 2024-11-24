package ru.ricardocraft.client.launch;

import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.JVMHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.ricardocraft.client.JavaFXApplication.disablePackageDelegateSupport;
import static ru.ricardocraft.client.JavaFXApplication.layer;

public class ModuleClassLoader extends URLClassLoader {
    private final ClassLoader SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();
    private final List<ClassLoaderControl.ClassTransformer> transformers = new ArrayList<>();
    private final Map<String, Class<?>> classMap = new ConcurrentHashMap<>();
    private final Map<String, Module> packageToModule = new HashMap<>();
    public String nativePath;

    private final List<String> packages = new ArrayList<>();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ModuleClassLoader(URL[] urls, ClassLoader parent) {
        super("LAUNCHER", urls, parent);
        packages.add("pro.gravit.launcher.");
        packages.add("pro.gravit.utils.");
    }

    public void initializeWithLayer(ModuleLayer layer) {
        for (var m : layer.modules()) {
            for (var p : m.getPackages()) {
                packageToModule.put(p, m);
            }
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name != null && !disablePackageDelegateSupport) {
            for (String pkg : packages) {
                if (name.startsWith(pkg)) {
                    return SYSTEM_CLASS_LOADER.loadClass(name);
                }
            }
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        var clazz = findClass(null, name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }

    @Override
    protected Class<?> findClass(String moduleName, String name) {
        Class<?> clazz;
        {
            clazz = classMap.get(name);
            if (clazz != null) {
                return clazz;
            }
        }
        if (name != null && !transformers.isEmpty()) {
            boolean needTransform = false;
            for (ClassLoaderControl.ClassTransformer t : transformers) {
                if (t.filter(moduleName, name)) {
                    needTransform = true;
                    break;
                }
            }
            if (needTransform) {
                String rawClassName = name.replace(".", "/").concat(".class");
                try (InputStream input = getResourceAsStream(rawClassName)) {
                    byte[] bytes = IOHelper.read(input);
                    for (ClassLoaderControl.ClassTransformer t : transformers) {
                        bytes = t.transform(moduleName, name, null, bytes);
                    }
                    clazz = defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    return null;
                }
            }
        }
        if (clazz == null && layer != null && name != null) {
            var pkg = getPackageFromClass(name);
            var module = packageToModule.get(pkg);
            if (module != null) {
                try {
                    clazz = module.getClassLoader().loadClass(name);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }
        if (clazz == null) {
            try {
                clazz = super.findClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        if (clazz != null) {
            classMap.put(name, clazz);
            return clazz;
        } else {
            return null;
        }
    }

    @Override
    public String findLibrary(String name) {
        return nativePath.concat(IOHelper.PLATFORM_SEPARATOR).concat(JVMHelper.NATIVE_PREFIX).concat(name).concat(JVMHelper.NATIVE_EXTENSION);
    }

    private static String getPackageFromClass(String clazz) {
        int index = clazz.lastIndexOf(".");
        if (index >= 0) {
            return clazz.substring(0, index);
        }
        return clazz;
    }
}
