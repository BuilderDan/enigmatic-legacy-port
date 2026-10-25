package com.aizistral.enigmaticlegacy.platform.network;

/**
 * Port-only. Which side receives a packet.
 */
public enum PacketDirection {
	/** Sent by the server, handled by the client (upstream {@code packets.clients}). */
	TO_CLIENT,
	/** Sent by the client, handled by the server (upstream {@code packets.server}). */
	TO_SERVER;
}
