package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * An interface used to extract data from commits.
 *
 * @param <T> the type of the thing we care about
 */
interface ThingWeAreInterestedIn<T> {
    String getName();

    T getThing(RevCommit commit);
}
