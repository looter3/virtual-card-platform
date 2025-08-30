package com.virtualcard.common.persistence.repository;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
/*-
@Repository
@Log4j2
public class UserRepository_DELETE extends AbstractJooqRepository {

	public UserRepository_DELETE(final DSLContext dsl) {
		super(dsl);
	}

	public Mono<Void> insertUserFromDTO(final UserDTO dto) {
		return insertUser(dto.getId(), dto.getUsername(), dto.getPassword(), dto.getRole());
	}

	private Mono<Void> insertUser(final int id, final String username, final String password, final String role) {
		return Mono.fromRunnable(() -> dsl.insertInto(USER)
			.set(USER.ID, id)
			.set(USER.USERNAME, username)
			.set(USER.PASSWORD, password)
			.set(USER.ROLE, role)
			.execute());
	}

	public Mono<UserDTO> findUserById(final int id) {
		return Mono.fromCallable(() -> dsl.selectFrom(USER)
			.where(USER.ID.eq(id))
			.fetchOneInto(UserDTO.class));
	}

	public Mono<UserDTO> findUserByUsername(final String username) {
		return Mono.fromCallable(() -> dsl.selectFrom(USER)
			.where(USER.USERNAME.eq(username))
			.fetchOneInto(UserDTO.class));
	}

}
*/
