package rinitech.tcp.packets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rinitech.tcp.errors.PacketException;
import rinitech.tcp.packets.json.BasePackage;
import rinitech.tcp.types.*;

public class MCPPacket
{
	private final MajorPacketType majorPacketType;
	private final MinorPacketType minorPacketType;
	private final BasePackage data;

	public MCPPacket(MajorPacketType majorPacketType, MinorPacketType minorPacketType, BasePackage data)
	{
		this.majorPacketType = majorPacketType;
		this.minorPacketType = minorPacketType;
		this.data = data;
	}

	public static MCPPacket parse(String json)
	{
		JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

		String type = jsonObject.get("type").getAsString();
		String[] types = type.split("\\.");

		MajorPacketType majorPacketType = TypeParser.getMajorPacketType(types[0]);
		MinorPacketType minorPacketType = TypeParser.getMinorPacketType(types[0], types[1]);

		if (majorPacketType == null || minorPacketType == null)
			throw new PacketException("Invalid packet type");

		BasePackage data;

		if (minorPacketType instanceof HandshakePacketType)
			data = new Gson().fromJson(jsonObject, PacketTypeUtils.getPacketByType((HandshakePacketType) minorPacketType).getClass());
		else if (minorPacketType instanceof AuthenticationPacketType)
			data = new Gson().fromJson(jsonObject, PacketTypeUtils.getPacketByType((AuthenticationPacketType) minorPacketType).getClass());
		else if (minorPacketType instanceof RoomPacketType)
			data = new Gson().fromJson(jsonObject, PacketTypeUtils.getPacketByType((RoomPacketType) minorPacketType).getClass());
		else if (minorPacketType instanceof MessagePacketType)
			data = new Gson().fromJson(jsonObject, PacketTypeUtils.getPacketByType((MessagePacketType) minorPacketType).getClass());
		else if (minorPacketType instanceof ErrorPacketType)
			data = new Gson().fromJson(jsonObject, PacketTypeUtils.getPacketByType((ErrorPacketType) minorPacketType).getClass());
		else
			throw new PacketException("Invalid packet type");


		return new MCPPacket(majorPacketType, minorPacketType, data);
	}

	public MajorPacketType getMajorPacketType()
	{
		return majorPacketType;
	}

	public MinorPacketType getMinorPacketType()
	{
		return minorPacketType;
	}

	public BasePackage getData()
	{
		return data;
	}

	public String toJson()
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", majorPacketType.toString() + "." + minorPacketType.toString());
		jsonObject.add("data", new Gson().toJsonTree(data).getAsJsonObject().get("data"));
		return jsonObject.toString() + "\n";
	}
}
