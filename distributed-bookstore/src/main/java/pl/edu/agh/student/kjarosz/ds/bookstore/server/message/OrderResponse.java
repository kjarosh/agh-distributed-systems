package pl.edu.agh.student.kjarosz.ds.bookstore.server.message;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author Kamil Jarosz
 */
@RequiredArgsConstructor
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class OrderResponse {
    @NonNull
    private final String title;
    private final boolean success;
    private final String price;
}
