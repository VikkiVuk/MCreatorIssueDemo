
package net.mcreator.demoworkspace.world.inventory;

import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import net.mcreator.demoworkspace.procedures.TestGUIWhileThisGUIIsOpenTickProcedure;
import net.mcreator.demoworkspace.init.DemoworkspaceModMenus;
import net.mcreator.demoworkspace.DemoworkspaceMod;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

public class TestGUIMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	public final static HashMap<String, Object> guistate = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private boolean bound = false;
	private Supplier<Boolean> boundItemMatcher = null;
	private Entity boundEntity = null;
	private BlockEntity boundBlockEntity = null;

	public TestGUIMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(DemoworkspaceModMenus.TEST_GUI.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
		this.internal = new ItemStackHandler(0);
		BlockPos pos = null;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			access = ContainerLevelAccess.create(world, pos);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.bound) {
			if (this.boundItemMatcher != null)
				return this.boundItemMatcher.get();
			else if (this.boundBlockEntity != null)
				return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
			else if (this.boundEntity != null)
				return this.boundEntity.isAlive();
		}
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		return ItemStack.EMPTY;
	}

	public Map<Integer, Slot> get() {
		return customSlots;
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static record TestGUIOtherMessage(int mode, int x, int y, int z, HashMap<String, String> textstate) implements CustomPacketPayload {

		public static final ResourceLocation ID = new ResourceLocation(DemoworkspaceMod.MODID, "test_gui_buttons");

		public TestGUIOtherMessage(FriendlyByteBuf buffer) {
			this(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), mapwork.readTextState(buffer));
		}

		@Override
		public void write(final FriendlyByteBuf buffer) {
			buffer.writeInt(mode);
			buffer.writeInt(x);
			buffer.writeInt(y);
			buffer.writeInt(z);
			mapwork.writeTextState(textstate, buffer);
		}
		public static class mapwork {
			public static void writeTextState(HashMap<String, String> map, FriendlyByteBuf buffer) {
				buffer.writeInt(map.size());
				for (Map.Entry<String, String> entry : map.entrySet()) {
					buffer.writeUtf(entry.getKey());
					buffer.writeUtf(entry.getValue());
				}
			}

			public static HashMap<String, String> readTextState(FriendlyByteBuf buffer) {
				int size = buffer.readInt();
				HashMap<String, String> map = new HashMap<>();
				for (int i = 0; i < size; i++) {
					String key = buffer.readUtf();
					String value = buffer.readUtf();
					map.put(key, value);
				}
				return map;
			}
		}

		@Override
		public ResourceLocation id() {
			return ID;
		}

		public static void handleData(final TestGUIOtherMessage message, final PlayPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.workHandler().submitAsync(() -> {
					Player entity = context.player().get();
					int mode = message.mode;
					int x = message.x;
					int y = message.y;
					int z = message.z;
					HashMap<String, String> textstate = message.textstate;
					handleOtherAction(entity, mode, x, y, z, textstate);
				}).exceptionally(e -> {
					context.packetHandler().disconnect(Component.literal(e.getMessage()));
					return null;
				});
			}
		}

		public static void handleOtherAction(Player entity, int mode, int x, int y, int z, HashMap<String, String> textstate) {
			Level world = entity.level();
			HashMap guistate = TestGUIMenu.guistate;
			for (Map.Entry<String, String> entry : textstate.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				guistate.put(key, value);
			}
			// security measure to prevent arbitrary chunk generation
			if (!world.hasChunkAt(new BlockPos(x, y, z)))
				return;
			if (mode == 0) {
				TestGUIWhileThisGUIIsOpenTickProcedure.execute();
			}
		}

		@SubscribeEvent
		public static void registerMessage(FMLCommonSetupEvent event) {
			DemoworkspaceMod.addNetworkMessage(TestGUIOtherMessage.ID, TestGUIOtherMessage::new, TestGUIOtherMessage::handleData);
		}
	}
}
