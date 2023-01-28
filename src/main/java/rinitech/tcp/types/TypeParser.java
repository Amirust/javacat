package rinitech.tcp.types;

public class TypeParser
{
	public static MajorPacketType getMajorPacketType(String majorPacketType)
	{
		return switch (majorPacketType) {
			case "Handshake" -> MajorPacketType.Handshake;
			case "Authentication" -> MajorPacketType.Authentication;
			case "Message" -> MajorPacketType.Message;
			case "Room" -> MajorPacketType.Room;
			case "Error" -> MajorPacketType.Error;
			default -> null;
		};
	}

	public static MinorPacketType getMinorPacketType(String majorPacketType, String minorPacketType)
	{
		return switch (majorPacketType) {
			case "Handshake" -> getHandshakePacketType(minorPacketType);
			case "Authentication" -> getAuthenticationPacketType(minorPacketType);
			case "Message" -> getMessagePacketType(minorPacketType);
			case "Room" -> getRoomPacketType(minorPacketType);
			case "Error" -> getErrorPacketType(minorPacketType);
			default -> null;
		};
	}

	private static HandshakePacketType getHandshakePacketType(String minorPacketType)
	{
		if (minorPacketType.equals("Handshake")) return HandshakePacketType.Handshake;
		else return null;
	}

	private static AuthenticationPacketType getAuthenticationPacketType(String minorPacketType)
	{
		return switch (minorPacketType) {
			case "Login" -> AuthenticationPacketType.Login;
			case "Accepted" -> AuthenticationPacketType.Accepted;
			case "UpdateAccessToken" -> AuthenticationPacketType.UpdateAccessToken;
			default -> null;
		};
	}

	private static MessagePacketType getMessagePacketType(String minorPacketType)
	{
		return switch (minorPacketType) {
			case "CreateTextMessage" -> MessagePacketType.CreateTextMessage;
			case "TextMessage" -> MessagePacketType.TextMessage;
			case "CreateImageMessage" -> MessagePacketType.CreateImageMessage;
			case "ImageMessage" -> MessagePacketType.ImageMessage;
			default -> null;
		};
	}

	private static RoomPacketType getRoomPacketType(String minorPacketType)
	{
		return switch (minorPacketType) {
			case "Create" -> RoomPacketType.Create;
			case "Created" -> RoomPacketType.Created;
			case "Join" -> RoomPacketType.Join;
			case "Joined" -> RoomPacketType.Joined;
			case "Leave" -> RoomPacketType.Leave;
			case "Left" -> RoomPacketType.Left;
			case "Delete" -> RoomPacketType.Delete;
			case "Deleted" -> RoomPacketType.Deleted;
			case "Update" -> RoomPacketType.Update;
			case "Updated" -> RoomPacketType.Updated;
			case "RequireList" -> RoomPacketType.RequireList;
			case "List" -> RoomPacketType.List;
			default -> null;
		};
	}

	private static ErrorPacketType getErrorPacketType(String minorPacketType)
	{
		return switch (minorPacketType) {
			case "UserPasswordRequired" -> ErrorPacketType.UserPasswordRequired;
			case "UserPasswordInvalid" -> ErrorPacketType.UserPasswordInvalid;
			case "UserNotFound" -> ErrorPacketType.UserNotFound;
			case "UserAlreadyExists" -> ErrorPacketType.UserAlreadyExists;
			case "AuthDataIncorrect" -> ErrorPacketType.AuthDataIncorrect;
			case "MessageDataIncorrect" -> ErrorPacketType.MessageDataIncorrect;
			case "UserDontConnected" -> ErrorPacketType.UserDontConnected;
			case "RoomNotFound" -> ErrorPacketType.RoomNotFound;
			case "RoomAlreadyExists" -> ErrorPacketType.RoomAlreadyExists;
			case "RoomDataIncorrect" -> ErrorPacketType.RoomDataIncorrect;
			case "RoomDontExists" -> ErrorPacketType.RoomDontExists;
			case "AccessDenied" -> ErrorPacketType.AccessDenied;
			default -> null;
		};
	}
}
