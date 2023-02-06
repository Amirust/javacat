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
import rinitech.tcp.Server;
import rinitech.tcp.ServerClient;
import rinitech.tcp.Utils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.security.*;


public class Images extends ServerResource
{

	@Get
	public FileRepresentation getImage() throws Exception {
		String token = getRequest().getHeaders().getFirstValue("Authorization");
		ServerClient serverClient = Server.serverClients.stream().findFirst().filter(client -> client.getAccessToken().equals(token)).orElse(null);
		if (serverClient == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		String filename = getAttribute("filename");
		File file = new File("images/" + filename);
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
	public Representation uploadImage(Representation entity) throws Exception {
		String token = getRequest().getHeaders().getFirstValue("Authorization");
		ServerClient serverClient = Server.serverClients.stream().findFirst().filter(client -> client.getAccessToken().equals(token)).orElse(null);
		if (serverClient == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		Representation result = null;
		if (entity != null) {
			if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(1000240);

				RestletFileUpload upload = new RestletFileUpload(factory);
				List<FileItem> items = upload.parseRepresentation(entity);
				try {
					for (FileItem fi : items) {
						if (!fi.getFieldName().equals("image")) continue;
						String fileName = fi.getName();
						System.out.println("HTTP: Uploading " + fileName + " by " + serverClient.username);
						if (fileName != null) {
							fileName += System.currentTimeMillis();
							String filename = Utils.byteArrayToHexString(MessageDigest.getInstance("MD5").digest(fileName.getBytes()));
							filename += fi.getName().substring(fi.getName().lastIndexOf('.'));
							File file = new File("images/" + filename);
							fi.write(file);
							result = new StringRepresentation(filename);
						}
					}
					setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}
		return result;
	}

}
