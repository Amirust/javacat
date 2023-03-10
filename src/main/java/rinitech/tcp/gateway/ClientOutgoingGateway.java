package rinitech.tcp.gateway;

import rinitech.tcp.Client;
import rinitech.tcp.Utils;
import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.Handshake;
import rinitech.tcp.packets.json.HandshakeData;
import rinitech.tcp.packets.json.Login;
import rinitech.tcp.packets.json.LoginData;
import rinitech.tcp.types.AuthenticationPacketType;
import rinitech.tcp.types.HandshakePacketType;
import rinitech.tcp.types.MajorPacketType;

public class ClientOutgoingGateway
{
	public static void sendHandshake(Client client)
	{
		Handshake handshake = new Handshake();
		handshake.data = new HandshakeData();
		handshake.data.publicKey = Utils.byteArrayToHexString(client.getDH().generatePublicKey());
		handshake.data.version = "2.0.0";
		MCPPacket packet = new MCPPacket(MajorPacketType.Handshake, HandshakePacketType.Handshake, handshake);
		client.send(packet, false);
	}

	public static void sendLogin(Client client, String username, String password) {
		Login login = new Login();
		login.data = new LoginData();
		login.data.username = username;
		login.data.password = password;
		MCPPacket packet = new MCPPacket(MajorPacketType.Authentication, AuthenticationPacketType.Login, login);
		client.send(packet, true);
	}
}
