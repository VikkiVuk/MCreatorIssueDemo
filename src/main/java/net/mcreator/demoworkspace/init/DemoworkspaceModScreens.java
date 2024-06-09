
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.demoworkspace.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.demoworkspace.client.gui.TestGUIScreen;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DemoworkspaceModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(DemoworkspaceModMenus.TEST_GUI.get(), TestGUIScreen::new);
	}
}
