package com.zomdroid;

import android.system.ErrnoException;
import android.system.Os;
import android.view.Surface;

import com.zomdroid.input.InputNativeInterface;
import com.zomdroid.game.GameInstance;

import java.util.ArrayList;

public class GameLauncher {
    public static void launch(GameInstance gameInstance) throws ErrnoException {

/*        // for debug
        Os.setenv("MESA_DEBUG", "1", false);
        Os.setenv("MESA_LOG_LEVEL", "debug", false);
        Os.setenv("ZINK_DEBUG", "validation", false);
        Os.setenv("mesa_glthread", "false", false);
        Os.setenv("GALLIUM_THREAD", "0", false);
        Os.setenv("VK_LOADER_DEBUG", "all", false);
        Os.setenv("VK_DEBUG", "all", false);
        Os.setenv("GALLIUM_DEBUG", "all", false);
        Os.setenv("VK_LOADER_LAYERS_ENABLE", "VK_LAYER_KHRONOS_validation", false);
        Os.setenv("BOX64_LOG", "3", false);
        Os.setenv("BOX64_DYNAREC", "0", false);*/

/*        Os.setenv("LIBGL_NOERROR", "1", false);
        Os.setenv("LIBGL_LOGSHADERERROR", "1", false);
        Os.setenv("ZINK_DEBUG", "spirv", false);*/

        Os.setenv("BOX64_LOG", "1", false);
        Os.setenv("BOX64_SHOWBT", "1", false);

        Os.setenv("BOX64_LD_LIBRARY_PATH", gameInstance.getLdLibraryPathForEmulation(), false);

        Os.setenv("GALLIUM_DRIVER", "zink", false);

        Os.setenv("ZOMDROID_CACHE_DIR", AppStorage.requireSingleton().getCachePath(), false);
        Os.setenv("ZOMDROID_RENDERER", LauncherPreferences.requireSingleton().getRenderer().name(), false);
        switch (LauncherPreferences.requireSingleton().getRenderer()) {
            case ZINK_ZFA:
            case ZINK_OSMESA:
                Os.setenv("ZOMDROID_VULKAN_DRIVER_NAME", LauncherPreferences.requireSingleton().getVulkanDriver().libName, false);
                break;
        }

        initZomdroidWindow();
        InputNativeInterface.sendJoystickConnected();

        ArrayList<String> jvmArgs = gameInstance.getJvmArgsAsList();
        jvmArgs.add("-Dorg.lwjgl.opengl.libname=" + LauncherPreferences.requireSingleton().getRenderer().libName);
        jvmArgs.add("-Dzomdroid.renderer=" + LauncherPreferences.requireSingleton().getRenderer().name());

        ArrayList<String> args = gameInstance.getArgsAsList();
/*        args.add("-debug");
        args.add("-debuglog=Shader");*/

        String javaHomePath = AppStorage.requireSingleton().getHomePath() + "/" + C.deps.JRE;
        String ldLibraryPath = AppStorage.requireSingleton().getLibraryPath() + ":/system/lib64:"
                + javaHomePath + "/lib:" + javaHomePath + "/lib/server:" + gameInstance.getJavaLibraryPath();
        GameLauncher.startGame(gameInstance.getGamePath(), ldLibraryPath, jvmArgs.toArray(new String[0]),
                gameInstance.getMainClassName(), args.toArray(new String[0]));
    }


    public static native int initZomdroidWindow();
    public static native void destroyZomdroidWindow();
    public static native int setSurface(Surface surface, int width, int height);
    public static native void destroySurface();
    static native void startGame(String gameDirPath, String libraryDirPath, String[] jvmArgs, String mainClassName, String[] args);
}
