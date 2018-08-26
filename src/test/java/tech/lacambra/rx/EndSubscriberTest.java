package tech.lacambra.rx;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Java6Assertions.assertThat;

class EndSubscriberTest {

  @Test
  void whenSubscribeToIt_thenShouldConsumeAll() throws InterruptedException {

    // given
    var publisher = new SubmissionPublisher<>();
    var subscriber = new EndSubscriber<>();
    publisher.subscribe(subscriber);
    List<String> items = List.of("1", "x", "2", "x", "3", "x");

    // when
    assertThat(publisher.getNumberOfSubscribers()).isEqualTo(1);
    items.forEach(publisher::submit);
    publisher.close();

    // then
    await().atMost(1000, TimeUnit.MILLISECONDS)
        .until(
            () -> assertThat(subscriber.consumedElements)
                .containsExactlyElementsOf(items)
        );
  }

  @Test
  public void whenSubscribeAndTransformElements_thenShouldConsumeAll()
      throws InterruptedException {

    // given
    SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
    TransformProcessor<String, Integer> transformProcessor = new TransformProcessor<>(Integer::parseInt);

    EndSubscriber<Integer> subscriber = new EndSubscriber<>();
    List<String> items = List.of("1", "2", "3");
    List<Integer> expectedResult = List.of(1, 2, 3);

    // when
    publisher.subscribe(transformProcessor);
    transformProcessor.subscribe(subscriber);
    items.forEach(publisher::submit);
    publisher.close();

    // then
    await().atMost(1000, TimeUnit.MILLISECONDS)
        .until(() ->
            assertThat(subscriber.consumedElements)
                .containsExactlyElementsOf(expectedResult)
        );
  }

}