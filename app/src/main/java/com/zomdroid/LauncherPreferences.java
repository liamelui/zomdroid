package com.zomdroid;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;

public class LauncherPreferences {
    private static LauncherPreferences singleton;
    transient private SharedPreferences sharedPreferences;
    transient private Gson gson;

    private float renderScale = 1.f;
    private Renderer renderer = Renderer.GL4ES;
    private VulkanDriver vulkanDriver = VulkanDriver.SYSTEM_DEFAULT;
    private boolean isDebug = false;

    LauncherPreferences() {
        if (isKgslSupported()) {
            this.renderer = Renderer.ZINK_ZFA;
            this.vulkanDriver = VulkanDriver.FREEDRENO;
        }
    }

    private static boolean isKgslSupported() {
        File kgsl = new File("/dev/kgsl-3d0");
        return kgsl.exists();
    }

    public static void init(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(C.shprefs.NAME, MODE_PRIVATE);
        String json = sharedPreferences.getString(C.shprefs.keys.LAUNCHER_PREFS, null);
        LauncherPreferences launcherPreferences;
        Gson gson = new Gson();
        if (json == null) {
            launcherPreferences = new LauncherPreferences();
        } else {
            Type type = new TypeToken<LauncherPreferences>(){}.getType();
            launcherPreferences = gson.fromJson(json, type);
        }
        launcherPreferences.sharedPreferences = sharedPreferences;
        launcherPreferences.gson = gson;
        singleton = launcherPreferences;
    }

    @Nullable
    public static LauncherPreferences getSingleton() {
        return singleton;
    }

    @NonNull
    public static LauncherPreferences requireSingleton() {
        if (singleton == null) {
            throw new RuntimeException("LauncherPreferences is not initialized");
        }
        return singleton;
    }

    public void saveToDisk() {
        String json = gson.toJson(this);
        this.sharedPreferences
                .edit()
                .putString(C.shprefs.keys.LAUNCHER_PREFS, json)
                .apply();
    }

    public float getRenderScale() {
        return renderScale;
    }

    public void setRenderScale(float renderScale) {
        this.renderScale = Math.clamp(renderScale, 0.25f, 1.f);
        saveToDisk();
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
        saveToDisk();
    }

    public VulkanDriver getVulkanDriver() {
        return vulkanDriver;
    }

    public void setVulkanDriver(VulkanDriver vulkanDriver) {
        this.vulkanDriver = vulkanDriver;
        saveToDisk();
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
        saveToDisk();
    }

    public enum Renderer {
        ZINK_ZFA("libzfa.so"),
        ZINK_OSMESA("libOSMesa.so"),
        GL4ES("libgl4es.so");

        final String libName;
        Renderer(String libName) {
            this.libName = libName;
        }
    }

    public enum VulkanDriver {
        SYSTEM_DEFAULT(null),
        FREEDRENO("libvulkan_freedreno.so");

        final String libName;
        VulkanDriver(String libName) {
            this.libName = libName;
        }
    }
}
