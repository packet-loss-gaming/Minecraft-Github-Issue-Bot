package gg.packetloss.issuebot;

import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

import java.util.List;

@CommandContainer
public class IssueCommands {
    private IssueReportingComponent component;

    public IssueCommands(IssueReportingComponent component) {
        this.component = component;
    }

    @Command(name = "report", desc = "Report a problem")
    public void warp(CommandSender sender,
                     @Arg(desc = "Destination warp", variable = true) List<String> issueText) {
        if (component.isOnCooldown(sender)) {
            sender.sendMessage(ChatColor.RED + "Slow down champ, you've reported too many issues, too quickly.");
            return;
        }

        component.createIssue(sender, Joiner.on(' ').join(issueText)).thenAccept((response) -> {
            if (response.statusCode() == 201) {
                sender.sendMessage(ChatColor.YELLOW + "Issue reported successfully. Thanks!");
            } else {
                sender.sendMessage(ChatColor.RED + "Issue report failed. Try again later.");
            }
        });
    }
}
