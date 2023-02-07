package rinitech.tcp.types;

public enum UserPacketType implements MinorPacketType
{
	Create,
	Created,
	Delete,
	Deleted,
	Update,
	Updated,
	GetInfo,
	Info
}
