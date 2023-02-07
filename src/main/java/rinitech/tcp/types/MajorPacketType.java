package rinitech.tcp.types;


public enum MajorPacketType
{
	Handshake,
	Heartbeat,
	Authentication,
	Message,
	File,
	User,
	Room,
	Error
}