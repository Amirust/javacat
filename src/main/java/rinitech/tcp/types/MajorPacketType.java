package rinitech.tcp.types;


public enum MajorPacketType
{
	Handshake,
	Heartbeat,
	Authentication,
	Message,
	User,
	Room,
	Error
}