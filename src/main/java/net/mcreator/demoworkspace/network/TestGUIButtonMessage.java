
package net.mcreator.demoworkspace.network;

import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import net.mcreator.demoworkspace.world.inventory.TestGUIMenu;
import net.mcreator.demoworkspace.procedures.ButtonPressProcedure;
import net.mcreator.demoworkspace.DemoworkspaceMod;

import java.util.Map;
import java.util.HashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public record TestGUIButtonMessage(int buttonID, int x, int y, int z, HashMap<String, String> textstate) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation(DemoworkspaceMod.MODID, "test_gui_buttons");

	public TestGUIButtonMessage(FriendlyByteBuf buffer) {
		this(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), mapwork.readTextState(buffer));
	}

	@Override
	public void write(final FriendlyByteBuf buffer) {
		buffer.writeInt(buttonID);
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

	public static void handleData(final TestGUIButtonMessage message, final PlayPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.workHandler().submitAsync(() -> {
				Player entity = context.player().get();
				int buttonID = message.buttonID;
				int x = message.x;
				int y = message.y;
				int z = message.z;
				HashMap<String, String> textstate = message.textstate;
				handleButtonAction(entity, buttonID, x, y, z, textstate);
			}).exceptionally(e -> {
				context.packetHandler().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z, HashMap<String, String> textstate) {
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
		if (buttonID == 0) {

			ButtonPressProcedure.execute();
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		DemoworkspaceMod.addNetworkMessage(TestGUIButtonMessage.ID, TestGUIButtonMessage::new, TestGUIButtonMessage::handleData);
	}

}
