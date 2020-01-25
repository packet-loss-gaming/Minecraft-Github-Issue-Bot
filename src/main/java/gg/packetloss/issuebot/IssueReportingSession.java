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

import com.sk89q.commandbook.component.session.PersistentSession;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class IssueReportingSession extends PersistentSession {
    private List<Long> recentIssueReports = new ArrayList<>(5);

    protected IssueReportingSession() {
        super(THIRTY_MINUTES);
    }

    private void cleanupReports() {
        recentIssueReports.removeIf((reportItem) -> {
            return System.currentTimeMillis() - reportItem > TimeUnit.MINUTES.toMillis(5);
        });
    }

    public boolean canReportIssues() {
        cleanupReports();
        return recentIssueReports.size() < 5;
    }

    public void reportedIssue() {
        recentIssueReports.add(System.currentTimeMillis());
    }
}