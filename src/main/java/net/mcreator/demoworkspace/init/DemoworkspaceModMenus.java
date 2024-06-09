
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.demoworkspace.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.Registries;

import net.mcreator.demoworkspace.world.inventory.TestGUIMenu;
import net.mcreator.demoworkspace.DemoworkspaceMod;

public class DemoworkspaceModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, DemoworkspaceMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<TestGUIMenu>> TEST_GUI = REGISTRY.register("test_gui", () -> IMenuTypeExtension.create(TestGUIMenu::new));
}
