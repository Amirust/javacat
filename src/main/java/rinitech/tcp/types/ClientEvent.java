package rinitech.tcp.types;

public enum ClientEvent
{
	HandshakeSuccess,
	AuthenticationSuccess,
	All,
	RoomJoined,
	RoomLeft,
	RoomCreated,
	RoomDeleted,
	RoomUpdated,
	RoomList,
	TextMessage,
	ImageMessage,
	Error
}
