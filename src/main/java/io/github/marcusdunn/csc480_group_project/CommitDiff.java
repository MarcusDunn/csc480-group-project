package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommitDiff implements ThingWeAreInterestedIn<String> {
    private static final Logger logger = Logger.getLogger(CommitDiff.class.getName());
    private final Git git;

    public CommitDiff(Git git) {
        this.git = git;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getThing(RevCommit commit) {
        try {
            final var currentTreeParser = new CanonicalTreeParser();
            currentTreeParser.reset(git.getRepository().newObjectReader(), commit.getTree());


            final var parentTreeParser = new CanonicalTreeParser();
            final var parent = getParent(commit);
            if (parent.isEmpty()) {
                return null;
            } else {
                parentTreeParser.reset(git.getRepository().newObjectReader(), parent.get());
                return git
                        .diff()
                        .setOldTree(currentTreeParser)
                        .setOldTree(parentTreeParser)
                        .call()
                        .stream()
                        .map(diffEntry -> diffEntry.getNewPath() + " " + diffEntry.getChangeType())
                        .toList()
                        .toString();
            }

        } catch (GitAPIException | IOException e) {
            logger.log(Level.SEVERE, e, () -> "Failed to get diff for commit " + commit);
            return null;
        }
    }

    private static Optional<RevTree> getParent(RevCommit commit) {
        try {
            return Optional.of(commit.getParent(0).getTree());
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
}
