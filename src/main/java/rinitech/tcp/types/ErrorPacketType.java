package rinitech.tcp.types;

public enum ErrorPacketType implements MinorPacketType
{
	UserPasswordRequired,
	UserPasswordInvalid,
	UserNotFound,
	UnsupportedVersion,
	UserAlreadyExists,
	PacketDataIncorrect,
	AuthDataIncorrect,
	MessageDataIncorrect,
	UserDontConnected,
	RoomNotFound,
	RoomAlreadyExists,
	RoomDataIncorrect,
	RoomDontExists,
	AccessDenied,
	HeartbeatTimeout,
}
