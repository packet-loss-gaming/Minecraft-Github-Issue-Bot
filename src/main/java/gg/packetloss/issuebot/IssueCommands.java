/*
 * This file is part of the Minecraft Github Issue Bot.
 *
 * Minecraft Github Issue Bot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minecraft Github Issue Bot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Minecraft Github Issue Bot.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.packetloss.issuebot;

import com.google.common.base.Joiner;
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
    public void report(CommandSender sender,
                       @Arg(desc = "A description of the issue encountered", variable = true) List<String> issueText) {
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
