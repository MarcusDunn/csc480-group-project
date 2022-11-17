package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.function.Function;

/**
 * An interface used to extract data from commits.
 *
 * @param <T> the type of the thing we care about
 */
interface ThingWeAreInterestedIn<T> {
    String getName();

    T getThing(RevCommit commit);

    default <R> ThingWeAreInterestedIn<R> map(String name, Function<T, R> mapper) {
        return new ThingWeAreInterestedIn<>() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public R getThing(RevCommit commit) {
                return mapper.apply(ThingWeAreInterestedIn.this.getThing(commit));
            }
        };
    }

    default <R> ThingWeAreInterestedIn<R> map(Function<T, R> mapper) {
        return this.map(this.getName(), mapper);
    }
}
