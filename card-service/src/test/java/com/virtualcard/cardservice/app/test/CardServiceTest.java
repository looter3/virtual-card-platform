package com.virtualcard.cardservice.app.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.virtualcard.cardservice.entity.Card;
import com.virtualcard.cardservice.mapper.CardMapper;
import com.virtualcard.cardservice.repository.ReactiveCardRepository;
import com.virtualcard.cardservice.service.CardService;
import com.virtualcard.common.dto.CardDTO;
import com.virtualcard.common.enums.CardStatus;
import com.virtualcard.common.test.AbstractPostgresReactiveTestContainer;

import jakarta.persistence.OptimisticLockException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardServiceTest extends AbstractPostgresReactiveTestContainer {

	@Autowired
	private Mutiny.SessionFactory sessionFactory;

	@Autowired
	private ReactiveCardRepository repository;

	@Autowired
	private CardService service;

	@Autowired
	private CardMapper mapper;

	/**
	 * Tests the behavior of the `getValidCard` method to ensure it filters out blocked cards.
	 *
	 * The test case:
	 * 1. Inserts a card with the status set to BLOCKED into the database using the {@code repository.insertCardFromDTO} method.
	 * 2. Attempts to retrieve the card using the {@code service.getValidCard} method with the card's ID.
	 * 3. Asserts that the returned result is empty and no card is emitted by the Mono since the card is blocked.
	 *
	 * Verifies that the `getValidCard` method properly identifies and excludes blocked cards from the results.
	 */
	@Test
	void getValidCard_shouldFilterBlockedCard() {

		final CardDTO blockedCard = createAndReturnCard(BigDecimal.valueOf(100), CardStatus.BLOCKED);

		StepVerifier.create(service.getValidCard(blockedCard.getCode()))
			.expectNextCount(0) // no card returned
			.verifyComplete();
	}

	/**
	 * Tests the behavior of retrieving a valid card with sufficient balance to cover a specified amount.
	 *
	 * The test case verifies that when a card has a balance greater than or equal to the required amount,
	 * the {@code getValidCoveredCard} method of the service returns the respective card.
	 *
	 * Preconditions:
	 * - A card with a defined balance is created and persisted in the repository.
	 *
	 * Expected behavior:
	 * - The method emits the card through its Mono stream and completes successfully when the card's balance
	 * is sufficient to meet the required amount.
	 */
	@Test
	void getValidCoveredCard_shouldReturnCard_whenBalanceSufficient() {
		final CardDTO card = createAndReturnCard(BigDecimal.valueOf(100), CardStatus.ACTIVE);

		StepVerifier.create(service.getValidCoveredCard(card.getCode(), BigDecimal.valueOf(50)))
			.expectNextMatches(c -> c.getCode().equals(card.getCode()) &&
					c.getBalance().compareTo(card.getBalance()) == 0)
			.verifyComplete();

	}

	/**
	 * Verifies that the `getValidCoveredCard` method does not return a card
	 * when the balance is insufficient to cover the specified amount.
	 *
	 * The test case simulates a scenario where:
	 * - A card is created with an initial balance of 50.
	 * - The `getValidCoveredCard` method is called with the card's ID and a required balance of 100.
	 *
	 * Expected behavior:
	 * - No card is returned since the card's balance is less than the required amount.
	 * - The Reactor `StepVerifier` confirms that no card is emitted.
	 */
	@Test
	void getValidCoveredCard_shouldNotReturnCard_whenBalanceInsufficient() {
		final CardDTO card = createAndReturnCard(BigDecimal.valueOf(50), CardStatus.ACTIVE);

		StepVerifier.create(service.getValidCoveredCard(card.getCode(), BigDecimal.valueOf(100)))
			.expectNextCount(0) // no card returned
			.verifyComplete(); // balance too low
	}

	/**
	 * Validates that the `updateBalance` method in the service correctly updates the balance of a card
	 * and increments its version, ensuring that the underlying repository is called with the correct values.
	 *
	 * This test creates a card with an initial balance, updates its balance, and verifies that the
	 * card's version number is incremented as expected after the update. This ensures the repository
	 * is tracking optimistic locking through versioning.
	 *
	 * Steps:
	 * 1. Create and persist a card with an initial balance using the `createAndReturnCard` helper method.
	 * 2. Invoke the `updateBalance` method to update the card's balance to a new value.
	 * 3. Fetch the updated card and assert that its version has been incremented (e.g., from 0 to 1).
	 *
	 * Assertions:
	 * - It verifies that the card version is incremented appropriately after the balance update.
	 */
	@Test
	void updateBalance_shouldCallRepositoryWithCorrectVersion() {
		final CardDTO card = createAndReturnCard(new BigDecimal("50.00"), CardStatus.ACTIVE);

		StepVerifier.create(
				service.updateBalance(card.getId(), new BigDecimal("75.00"))
					.then(Mono.defer(() -> Mono.fromCompletionStage(repository.findById(card.getId())
						.map(Card::getVersion)
						.subscribeAsCompletionStage()))) // Reactive check
		)
			.expectNext(1) // Ensure version was incremented
			.verifyComplete();
	}

	/**
	 * Tests that concurrent updates on the same record trigger an optimistic locking failure.
	 *
	 * The purpose of this test is to verify that a DataChangedException is thrown
	 * when two threads attempt to update the balance of the same card concurrently,
	 * ensuring the system enforces optimistic locking based on the version mechanism.
	 *
	 * The method simulates two separate threads performing updates on a card record in parallel.
	 * An exception is captured when a conflict arises due to concurrent updates on the same entity.
	 *
	 * @throws InterruptedException if the thread execution is interrupted while waiting for
	 *                                  the concurrent tasks to complete.
	 */
	@Test
	void concurrentUpdates_shouldTriggerOptimisticLocking() throws InterruptedException {
		final CardDTO card = createAndReturnCard(new BigDecimal("50.00"), CardStatus.ACTIVE);

		// Define two tasks to update the same record in parallel
		final AtomicReference<Throwable> errorHolder = new AtomicReference<>();

		final Runnable update1 = () -> {
			try {
				Mono.fromCompletionStage(repository.updateCardBalance(card.getId(), new BigDecimal("60.00")).subscribeAsCompletionStage())
					.block();
			} catch (final Throwable e) {
				errorHolder.set(e); // Capture exception for assertion later
			}
		};

		final Runnable update2 = () -> {
			try {
				Mono.fromCompletionStage(repository.updateCardBalance(card.getId(), new BigDecimal("70.00")).subscribeAsCompletionStage())
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

		// Assert that at least one thread encountered a OptimisticLockException
		assertNotNull(errorHolder.get());
		assertTrue(errorHolder.get() instanceof OptimisticLockException);
	}

	/**
	 * Creates a new card with the given cardholder name and initial balance,
	 * stores it in the repository, and retrieves it using its unique identifier.
	 *
	 * @param initialAmount the initial monetary balance to set on the card
	 * @return the newly created card as a {@link CardDTO} instance
	 */
	private CardDTO createAndReturnCard(final BigDecimal initialAmount, final CardStatus status) {
		final String code = UUID.randomUUID().toString();
		final Card card = new Card();
		card.setUserId(1L);
		card.setCode(code);
		card.setBalance(initialAmount);
		card.setCreatedAt(Instant.now());
		card.setStatus(status);

		return repository.save(card) // returns Uni<Card>
			.flatMap(savedCard -> repository.findByCode(savedCard.getCode()))
			.map(mapper::entityToDTO)
			.await().indefinitely(); // block only for test setup
	}

	@Override
	protected SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

}
