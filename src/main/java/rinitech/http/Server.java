package rinitech.http;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import rinitech.config.Config;
import rinitech.database.DatabaseAdapter;


public class Server {
	private final Config config;
	public static DatabaseAdapter db;
	private final Component component;

	public Server(Config config, DatabaseAdapter db) {
		this.config = config;
		Server.db = db;
		this.component = new Component();
	}

	public void start() throws Exception {
		int port = config.http.split(":").length == 3 ? Integer.parseInt(config.http.split(":")[2]) : 80;
		component.getServers().add(Protocol.HTTP, port);
		component.getDefaultHost().attach(new Application()
		{
			@Override
			public Restlet createInboundRoot()
			{
				Router router = new Router(getContext());
				router.attach("/register", Registration.class);
				router.attach("/images/{filename}", Images.class);
				// TODO: Avatars
				return router;
			}
		});
//		Context.getCurrentLogger().setLevel(java.util.logging.Level.OFF);

		component.start();
	}

	public void stop() throws Exception {
		component.stop();
	}
}