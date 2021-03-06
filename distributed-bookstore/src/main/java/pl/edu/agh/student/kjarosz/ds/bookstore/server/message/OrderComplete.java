package pl.edu.agh.student.kjarosz.ds.bookstore.server.message;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
public class OrderComplete {
    private final boolean success;
}
