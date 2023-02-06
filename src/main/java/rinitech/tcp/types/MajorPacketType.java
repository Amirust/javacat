package rinitech.tcp.types;


public enum MajorPacketType
{
	Handshake,
	Heartbeat,
	Authentication,
	Message,
	Room,
	Error
}