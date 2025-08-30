package com.virtualcard.common.converter;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.converters.multi.MultiReactorConverters;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         11 ago 2025
 *
 */
public class VertxWebFluxConverter {

	public static <T> Flux<T> convertMultiToFlux(final Multi<T> multi) {
		// Uses Mutiny's built-in converter — handles backpressure & cancellation correctly
		return multi.convert().with(MultiReactorConverters.toFlux());
	}

	public static <T> Mono<T> convertUniToMono(final Uni<T> uni) {
		// Same here — no manual subscription
		return uni.convert().with(UniReactorConverters.toMono());
	}

}
