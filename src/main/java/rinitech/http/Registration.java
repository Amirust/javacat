package rinitech.http;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import com.google.gson.Gson;

public class Registration extends ServerResource
{

	@Post("json")
	public Representation post(String body)
	{
		System.out.println(body);
		RegistrationData data = new Gson().fromJson(body, RegistrationData.class);
		if (data.username == null || data.username.isEmpty() || data.password == null || data.password.isEmpty()) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("Bad Request");
		}

		if (Server.db.getUser(data.username) != null || Server.getConfig().rootUsername.equals(data.username)) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("Username already exists");
		}

		Server.db.addUser(data.username, data.password);
		System.out.println("HTTP: Created " + data.username + " user");

		return new StringRepresentation("User created");
	}
}

class RegistrationData
{
	public String username;
	public String password;
}