package net.notcoded.wayfix.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import net.notcoded.wayfix.config.ModClothConfig;
import net.notcoded.wayfix.util.WindowHelper;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenManager.class)
public class ScreenManagerMixin {
    @Shadow @Final private Long2ObjectMap<Monitor> monitors;

    @Inject(method = {"onMonitorChange", "<init>"}, at = @At("RETURN"))
    private void handleConfigAdditions(CallbackInfo ci) {
        if(!WindowHelper.canUseWindowHelper()) this.wayfix$refreshMonitors();
    }

    @Unique
    private void wayfix$refreshMonitors() {
        this.monitors.forEach((aLong, monitor1) ->
                ModClothConfig.monitors.put(GLFW.glfwGetMonitorName(aLong), aLong)
        );
    }
}
