package io.github.marcusdunn.csc480_group_project;

record Pair<F, S>(F first, S second) {
    static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }
}
