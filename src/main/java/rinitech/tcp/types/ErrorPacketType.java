package rinitech.tcp.types;

public enum ErrorPacketType implements MinorPacketType
{
	UserPasswordRequired,
	UserPasswordInvalid,
	UserNotFound,
	UserAlreadyExists,
	PacketDataIncorrect,
	AuthDataIncorrect,
	MessageDataIncorrect,
	UserDontConnected,
	RoomNotFound,
	RoomAlreadyExists,
	RoomDataIncorrect,
	RoomDontExists,
	AccessDenied
}
