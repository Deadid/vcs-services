package org.serhii.makhov.vcsservices.client.github.resource;

public record Repository(String name, Owner owner, boolean fork) {
}
