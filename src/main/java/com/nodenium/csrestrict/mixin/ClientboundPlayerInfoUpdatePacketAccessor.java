package com.nodenium.csrestrict.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;
import java.util.EnumSet;

/** The only non-decode constructor this packet has; there's no List&lt;Entry&gt; constructor. */
@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public interface ClientboundPlayerInfoUpdatePacketAccessor {
	@Invoker("<init>")
	static ClientboundPlayerInfoUpdatePacket csrestrict$create(
			EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions,
			Collection<ServerPlayer> players) {
		throw new AssertionError("Mixin not applied");
	}
}
