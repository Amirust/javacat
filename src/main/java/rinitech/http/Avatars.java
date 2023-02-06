package rinitech.http;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import rinitech.database.types.User;
import rinitech.tcp.Server;
import rinitech.tcp.ServerClient;
import rinitech.tcp.Utils;

import java.io.File;
import java.util.List;
import java.security.*;


public class Avatars extends ServerResource
{
	@Get
	public FileRepresentation getAvatar() {
		String username = getAttribute("username");
		String token = getRequest().getHeaders().getFirstValue("Authorization");
		ServerClient serverClient = Server.getClients().stream().findFirst().filter(client -> client.getAccessToken().equals(token)).orElse(null);
		if (serverClient == null || !serverClient.getUsername().equals(username)) {
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return null;
		}
		User user = rinitech.http.Server.db.getUser(username);
		if (user == null || user.avatar == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		String filename = user.avatar;
		File file = new File("avatars/" + filename);
		if (!file.exists()) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		if (filename.endsWith(".png")) return new FileRepresentation(file, MediaType.IMAGE_PNG);
		else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return new FileRepresentation(file, MediaType.IMAGE_JPEG);
		else if (filename.endsWith(".gif")) return new FileRepresentation(file, MediaType.IMAGE_GIF);
		else if (filename.endsWith(".bmp")) return new FileRepresentation(file, MediaType.IMAGE_BMP);

		getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
		return null;
	}

	@Post
	public Representation setAvatar(Representation entity) throws Exception {
		String username = getAttribute("username");
		String token = getRequest().getHeaders().getFirstValue("Authorization");
		ServerClient serverClient = Server.getClients().stream().findFirst().filter(client -> client.getAccessToken().equals(token)).orElse(null);
		if (serverClient == null || !serverClient.getUsername().equals(username)) {
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return new StringRepresentation("Unauthorized");
		}
		User user = rinitech.http.Server.db.getUser(username);
		if (user == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return new StringRepresentation("User not found");
		}

		if (entity != null) {
			if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(1000240);

				RestletFileUpload upload = new RestletFileUpload(factory);
				List<FileItem> items = upload.parseRepresentation(entity);
				try {
					for (FileItem fi : items) {
						if (!fi.getFieldName().equals("avatar")) continue;
						String fileName = fi.getName();
						System.out.println("HTTP: Set avatar for " + username + " to " + fileName);
						if (fileName != null) {
							fileName += username;
							String filename = Utils.byteArrayToHexString(MessageDigest.getInstance("MD5").digest(fileName.getBytes()));
							filename += fi.getName().substring(fi.getName().lastIndexOf('.'));
							File file = new File("avatars/" + filename);
							fi.write(file);

							rinitech.http.Server.db.updateUserAvatar(username, filename);
							setStatus(Status.SUCCESS_OK);
							return new StringRepresentation("Avatar updated");
						}
					}
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
			}
		}
		return new StringRepresentation("Bad request");
	}
}
