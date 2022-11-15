package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.*;

public class CommitDateTime implements ThingWeAreInterestedIn<ZonedDateTime> {
    @Override
    public String getName() {
        return "date time";
    }

    @Override
    public ZonedDateTime getThing(RevCommit commit) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(commit.getCommitTime()), commit.getCommitterIdent().getZoneId());
    }
}
