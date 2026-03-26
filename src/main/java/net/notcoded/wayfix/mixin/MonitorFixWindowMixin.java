package net.notcoded.wayfix.mixin;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import net.notcoded.wayfix.WayFix;
import net.notcoded.wayfix.config.ModClothConfig;
import net.notcoded.wayfix.util.WindowHelper;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;

@Mixin(Window.class)
public abstract class MonitorFixWindowMixin {

    @Shadow protected abstract void onMove(long window, int x, int y);

    @Shadow @Final private long window;

    @Redirect(method = "setMode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/ScreenManager;findBestMonitor(Lcom/mojang/blaze3d/platform/Window;)Lcom/mojang/blaze3d/platform/Monitor;"))
    private Monitor fixWrongMonitor(ScreenManager instance, Window window) {
        return WindowHelper.canUseWindowHelper() ? instance.findBestMonitor(window) : wayfix$getMonitor(instance);
    }

    @Unique
    private Monitor wayfix$getMonitor(ScreenManager instance) {
        String monitorName = WayFix.config.monitorName;
        long monitorID = GLFW.glfwGetPrimaryMonitor();
        if(!monitorName.trim().isEmpty()) {
            monitorID = ModClothConfig.monitors.getOrDefault(monitorName, 0L);
            if(monitorID == 0L &&
                    monitorName.toLowerCase().startsWith("dp-") &&
                    Character.isDigit(monitorName.charAt(monitorName.length() - 1))
            ) {
                ArrayList<Long> values = new ArrayList<>(ModClothConfig.monitors.values());
                values.sort(Collections.reverseOrder());

                try {
                    monitorID = values.get(Integer.parseInt(monitorName.substring(monitorName.length() - 1)) - 1);
                } catch (Exception ignored) { }
            }
        }



        if(monitorID <= 0 || instance.getMonitor(monitorID) == null) {
            WayFix.LOGGER.warn("Error occurred while trying to set monitor.");
            WayFix.LOGGER.warn("Using primary monitor instead.");
            monitorID = GLFW.glfwGetPrimaryMonitor();
        }

        return instance.getMonitor(monitorID);
    }


    // KDE Plasma ONLY
    @Inject(method = "setMode", at = @At("HEAD"))
    private void fixWrongMonitor(CallbackInfo ci) {
        if(!WindowHelper.canUseWindowHelper()) return;

        int[] pos = WindowHelper.getWindowPos();
        if(pos == null) return;

        onMove(this.window, pos[0], pos[1]);
    }
}
