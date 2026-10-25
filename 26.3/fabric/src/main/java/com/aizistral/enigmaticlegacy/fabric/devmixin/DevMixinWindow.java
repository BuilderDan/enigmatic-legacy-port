package com.aizistral.enigmaticlegacy.fabric.devmixin;

import java.nio.IntBuffer;

import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Window;

/**
 * Port-only, dev clients only: moves the window to the first display that is not the primary
 * one, so test runs never cover the main screen. 26.3 uses SDL instead of GLFW: the window is
 * created hidden, moved, then shown without activation, so it never takes focus or the mouse.
 */
@Mixin(Window.class)
public class DevMixinWindow {
	@ModifyArg(method = "createWindow", at = @At(value = "INVOKE",
			target = "Lcom/mojang/renderpearl/api/device/GpuBackend;createWindow(Ljava/lang/String;IIJ)J"), index = 3)
	private long createHidden(long flags) {
		SDLHints.SDL_SetHint("SDL_WINDOW_ACTIVATE_WHEN_SHOWN", "0");
		SDLHints.SDL_SetHint("SDL_WINDOW_ACTIVATE_WHEN_RAISED", "0");
		return flags | SDLVideo.SDL_WINDOW_HIDDEN;
	}

	@Inject(method = "<init>*", at = @At("TAIL"))
	private void afterInit(CallbackInfo info) {
		long window = ((Window) (Object) this).handle();
		int primary = SDLVideo.SDL_GetPrimaryDisplay();
		IntBuffer displays = SDLVideo.SDL_GetDisplays();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			for (int i = 0; displays != null && i < displays.limit(); i++) {
				int display = displays.get(i);
				SDL_Rect bounds = SDL_Rect.malloc(stack);

				if (display == primary || !SDLVideo.SDL_GetDisplayBounds(display, bounds)) {
					continue;
				}

				IntBuffer width = stack.mallocInt(1), height = stack.mallocInt(1);
				SDLVideo.SDL_GetWindowSize(window, width, height);
				SDLVideo.SDL_SetWindowPosition(window, bounds.x() + Math.max(0, (bounds.w() - width.get(0)) / 2),
						bounds.y() + Math.max(0, (bounds.h() - height.get(0)) / 2));
				break;
			}
		}

		SDLVideo.SDL_ShowWindow(window);
	}
}
