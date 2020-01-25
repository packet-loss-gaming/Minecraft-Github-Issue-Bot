package gg.packetloss.issuebot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.ComponentCommandRegistrar;
import com.sk89q.commandbook.component.session.SessionComponent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@ComponentInformation(friendlyName = "Issue Reporting", desc = "Report issues to a github repo.")
@Depend(components = {SessionComponent.class})
public class IssueReportingComponent extends BukkitComponent implements Listener {
    @InjectComponent
    private SessionComponent sessions;

    private LocalConfiguration config;

    @Override
    public void enable() {
        this.config = configure(new LocalConfiguration());

        ComponentCommandRegistrar registrar = CommandBook.getComponentRegistrar();
        registrar.registerTopLevelCommands((commandManager, registration) -> {
            registration.register(commandManager, IssueCommandsRegistration.builder(), new IssueCommands(this));
        });
    }

    @Override
    public void reload() {

        super.reload();
        configure(config);
    }

    private static class LocalConfiguration extends ConfigurationBase {
        @Setting("api.key")
        public String apiKey = "";
        @Setting("repo-url")
        public String repositoryUrl = "";
    }

    private IssueReportingSession getSession(CommandSender sender) {
        return sessions.getSession(IssueReportingSession.class, sender);
    }

    public boolean isOnCooldown(CommandSender sender) {
        return !getSession(sender).canReportIssues();
    }

    private final String NEW_PARAGRAPH = "\n\n";

    private String getBody(CommandSender sender, String issueText) {
        StringBuilder bodyText = new StringBuilder();

        // Add reporter name and UUID
        bodyText.append("Reporter: ").append(sender.getName());
        if (sender instanceof Player) {
            bodyText.append(" (").append(((Player) sender).getUniqueId()).append(')');
        }
        bodyText.append('\n');

        // Add location information
        if (sender instanceof Player) {
            Location location = ((Player) sender).getLocation();
            bodyText.append("Where: ").append(location.getWorld().getName());
            bodyText.append(" at ").append(location.getBlockX());
            bodyText.append(", ").append(location.getBlockY());
            bodyText.append(", ").append(location.getBlockZ());
            bodyText.append(NEW_PARAGRAPH);
        }

        bodyText.append(issueText);
        return bodyText.toString();
    }

    public CompletableFuture<HttpResponse<String>> createIssue(CommandSender sender, String issueText) {
        getSession(sender).reportedIssue();

        String title = issueText.substring(0, Math.min(30, issueText.length()));
        String body = getBody(sender, issueText);

        JsonObject object = new JsonObject();
        object.add("title", new JsonPrimitive(title));
        object.add("body", new JsonPrimitive(body));

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(object.toString()))
                .uri(URI.create(config.repositoryUrl + "/issues"))
                .setHeader("User-Agent", "Minecraft Issue Report Bot")
                .setHeader("Authorization", "token " + config.apiKey)
                .header("Content-Type", "text/json").build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}