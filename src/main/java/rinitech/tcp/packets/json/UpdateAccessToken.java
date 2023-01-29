package rinitech.tcp.packets.json;

/**
 * UpdateAccessToken packet sends to the client when the server wants to update the access token.
 * Data contains the access token.
 * @see UpdateAccessTokenData
 */
public class UpdateAccessToken extends BasePackage
{
	public UpdateAccessTokenData data;
}

