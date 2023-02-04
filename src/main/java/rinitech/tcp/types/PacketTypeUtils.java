package rinitech.tcp.types;

import rinitech.tcp.packets.json.BasePackage;

public class PacketTypeUtils
{
	public static BasePackage getPacketByType(AuthenticationPacketType type)
	{
		return switch (type) {
			case Login -> new rinitech.tcp.packets.json.Login();
			case Accepted -> new rinitech.tcp.packets.json.Accepted();
			case UpdateAccessToken -> new rinitech.tcp.packets.json.UpdateAccessToken();
		};
	}

	public static BasePackage getPacketByType(HandshakePacketType type)
	{
		return switch (type) {
			case Handshake -> new rinitech.tcp.packets.json.Handshake();
		};
	}

	public static BasePackage getPacketByType(RoomPacketType type)
	{
		return switch (type) {
			case RequireList -> new rinitech.tcp.packets.json.RequireList();
			case List -> new rinitech.tcp.packets.json.List();
			case Create -> new rinitech.tcp.packets.json.Create();
			case Created -> new rinitech.tcp.packets.json.Created();
			case Join -> new rinitech.tcp.packets.json.Join();
			case Joined -> new rinitech.tcp.packets.json.Joined();
			case Leave -> new rinitech.tcp.packets.json.Leave();
			case Left -> new rinitech.tcp.packets.json.Left();
			case Delete -> new rinitech.tcp.packets.json.Delete();
			case Deleted -> new rinitech.tcp.packets.json.Deleted();
			case Update -> new rinitech.tcp.packets.json.Update();
			case Updated -> new rinitech.tcp.packets.json.Updated();
		};
	}

	public static BasePackage getPacketByType(MessagePacketType type)
	{
		return switch (type) {
			case CreateTextMessage -> new rinitech.tcp.packets.json.CreateTextMessage();
			case TextMessage -> new rinitech.tcp.packets.json.TextMessage();
			case CreateImageMessage -> new rinitech.tcp.packets.json.CreateImageMessage();
			case ImageMessage -> new rinitech.tcp.packets.json.ImageMessage();
		};
	}

	public static BasePackage getPacketByType(ErrorPacketType type)
	{
		return new rinitech.tcp.packets.json.BaseErrorPackage(type.name());
	}
}
