package ispd.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CollectionUtils {
    ;

    public static <T> List<T> filledList (final T fillValue, final long count) {
        return Stream.generate(() -> fillValue)
            .limit(count)
            .collect(Collectors.toList());
    }
}
