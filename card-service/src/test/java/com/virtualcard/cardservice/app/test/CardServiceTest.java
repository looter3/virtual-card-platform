package com.virtualcard.cardservice.app.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.jooq.exception.DataChangedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jooq.generated.enums.CardStatus;
import com.jooq.generated.tables.pojos.CardDTO;
import com.virtualcard.cardservice.repository.CardRepository;
import com.virtualcard.cardservice.service.CardService;
import com.virtualcard.common.test.AbstractMySQLTestContainerTest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author lex_looter
 *
 *         8 giu 2025
 *
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardServiceTest extends AbstractMySQLTestContainerTest {

	@Autowired
	private CardRepository repository;

	@Autowired
	private CardService service;

	@Test
	void getValidCard_shouldFilterBlockedCard() {
		final CardDTO blockedCard = new CardDTO("123", "Alice", BigDecimal.valueOf(100), LocalDateTime.now(), CardStatus.BLOCKED, 1);

		repository.insertCardFromDTO(blockedCard);

		StepVerifier.create(service.getValidCard(blockedCard.getId()))
			.expectNextCount(0) // no card returned
			.verifyComplete();
	}

	@Test
	void getValidCoveredCard_shouldReturnCard_whenBalanceSufficient() {
		final CardDTO card = createAndReturnCard("", BigDecimal.valueOf(100));

		StepVerifier.create(service.getValidCoveredCard(card.getId(), BigDecimal.valueOf(50)))
			.expectNext(card)
			.verifyComplete();
	}

	@Test
	void getValidCoveredCard_shouldNotReturnCard_whenBalanceInsufficient() {
		final CardDTO card = createAndReturnCard("", BigDecimal.valueOf(50));

		StepVerifier.create(service.getValidCoveredCard(card.getId(), BigDecimal.valueOf(100)))
			.expectNextCount(0) // no card returned
			.verifyComplete(); // balance too low
	}

	@Test
	void updateBalance_shouldCallRepositoryWithCorrectVersion() {
		final CardDTO card = createAndReturnCard("Dana", new BigDecimal("50.00"));

		StepVerifier.create(
				service.updateBalance(card.getId(), new BigDecimal("75.00"))
					.then(Mono.defer(() -> repository.getCard(card.getId()).map(CardDTO::getVersion))) // Reactive check
		)
			.expectNext(1) // Ensure version was incremented
			.verifyComplete();
	}

	@Test
	void concurrentUpdates_shouldTriggerOptimisticLocking() throws InterruptedException {
		final CardDTO card = createAndReturnCard("Alice", new BigDecimal("50.00"));

		// Define two tasks to update the same record in parallel
		final AtomicReference<Throwable> errorHolder = new AtomicReference<>();

		final Runnable update1 = () -> {
			try {
				repository.updateBalanceByCardId(card.getId(), new BigDecimal("60.00"))
					.block();
			} catch (final Throwable e) {
				errorHolder.set(e); // Capture exception for assertion later
			}
		};

		final Runnable update2 = () -> {
			try {
				repository.updateBalanceByCardId(card.getId(), new BigDecimal("70.00"))
					.block();
			} catch (final Throwable e) {
				errorHolder.set(e); // Capture exception for assertion later
			}
		};

		// Run both updates in separate threads
		final Thread thread1 = new Thread(update1);
		final Thread thread2 = new Thread(update2);

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		// Assert that at least one thread encountered a DataChangedException
		assertNotNull(errorHolder.get());
		assertTrue(errorHolder.get() instanceof DataChangedException);
	}

	private CardDTO createAndReturnCard(final String cardHoalder, final BigDecimal initialAmount) {
		final String id = UUID.randomUUID().toString();
		repository.insertCardWithId(id, cardHoalder, initialAmount).block();
		return repository.getCard(id).block();
	}

}
