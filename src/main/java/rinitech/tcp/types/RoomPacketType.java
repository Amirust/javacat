package rinitech.tcp.types;

public enum RoomPacketType implements MinorPacketType
{
	Create,
	Created,
	Join,
	Joined,
	Leave,
	Left,
	Delete,
	Deleted,
	Update,
	Updated,
	RequireList,
	List,
}
