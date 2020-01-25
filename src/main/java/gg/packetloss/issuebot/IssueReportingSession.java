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