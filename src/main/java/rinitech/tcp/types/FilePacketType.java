package rinitech.tcp.types;

public enum FilePacketType implements MinorPacketType
{
	RequestImageUpload,
	ImageUploadAccepted,
	ImageUploadRejected,
	ImageUploadPart,
	ImageUploadCompleted,
	RequestImageDownload,
	ImageDownloadMeta,
	ImageDownloadPart,
	ImageDownloadCompleted
}
