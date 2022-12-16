package org.serhii.makhov.vcsservices.client.github.util;

import java.util.List;

/**
 * Class that holds entities that were returned from API request and pageNumber which was requested
 */
public record PagedResponse<T>(List<T> entities, int pageNumber) {
}
