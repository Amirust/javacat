package rinitech;

import rinitech.config.Config;
import rinitech.config.Parser;
import rinitech.database.DatabaseAdapter;
import rinitech.database.implementations.NitriteAdapter;
import rinitech.tcp.Server;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class Main
{
	final ExecutorService executor;

	public Main(ExecutorService executor)
	{
		this.executor = executor;
	}

	public static void main(String[] args)
	{
		Config config = new Config();
		config.http = Parser.parse("miricat.http");
		config.port = Integer.parseInt(Objects.requireNonNull(Parser.parse("miricat.port")));
		config.rootUsername = Parser.parse("root.username");
		config.rootPassword = Parser.parse("root.password");
		config.dbpath = Parser.parse("db.path");
		config.dbUser = Parser.parse("db.user");
		config.dbPass = Parser.parse("db.password");
		config.heartbeatRate = Integer.parseInt(Objects.requireNonNull(Parser.parse("miricat.heartbeat")));
		config.httpAvatars = Boolean.parseBoolean(Parser.parse("http.avatars"));
		config.httpImages = Boolean.parseBoolean(Parser.parse("http.images"));
		config.httpRegistration = Boolean.parseBoolean(Parser.parse("http.registration"));

		DatabaseAdapter db = new NitriteAdapter(config.dbpath, config.dbUser, config.dbPass);
		try {
			Server server = new Server(config, db,config.port);
			rinitech.http.Server httpServer = new rinitech.http.Server(config, db);
			Runtime.getRuntime().addShutdownHook(new Thread(() ->
			{
				System.out.println("Shutting down server...");
				try {
					server.stop();
					httpServer.stop();
					db.close();
				} catch (Exception ignored) {}
				System.out.println("Server stopped.\n");
			}));
			new Thread(() -> {
				try { httpServer.start(); } catch (Exception e) { e.printStackTrace(); }
			}).start();
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}