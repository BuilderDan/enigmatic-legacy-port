package com.aizistral.enigmaticlegacy.neoforge.devmixin;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Window;

/**
 * Port-only, dev clients only: opens the window hidden, moves it to the first monitor that is not
 * the primary one, then shows it without taking focus, so test runs never cover the main screen.
 */
@Mixin(Window.class)
public class DevMixinWindow {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J", remap = false))
	private void beforeCreateWindow(CallbackInfo info) {
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void afterInit(CallbackInfo info) {
		long window = ((Window) (Object) this).handle();
		long primary = GLFW.glfwGetPrimaryMonitor();
		PointerBuffer monitors = GLFW.glfwGetMonitors();

		for (int i = 0; monitors != null && i < monitors.limit(); i++) {
			long monitor = monitors.get(i);
			GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);

			if (monitor == primary || mode == null) {
				continue;
			}

			int[] x = new int[1], y = new int[1], width = new int[1], height = new int[1];
			GLFW.glfwGetMonitorPos(monitor, x, y);
			GLFW.glfwGetWindowSize(window, width, height);
			GLFW.glfwSetWindowPos(window, x[0] + Math.max(0, (mode.width() - width[0]) / 2),
					y[0] + Math.max(0, (mode.height() - height[0]) / 2));
			break;
		}

		GLFW.glfwShowWindow(window);
	}
}
