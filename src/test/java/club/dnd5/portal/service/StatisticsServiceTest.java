package club.dnd5.portal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticsServiceTest {
	private static final int ENTITIES = StatisticsService.COUNTED_ENTITIES.size();

	private final AtomicLong now = new AtomicLong(1_000_000L);
	private EntityManager entityManager;
	private TypedQuery<Long> query;
	private StatisticsService service;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		entityManager = mock(EntityManager.class);
		query = mock(TypedQuery.class);
		when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(10L);
		service = new StatisticsService(entityManager, now::get);
	}

	@Test
	void sumsCountsOfAllMaterialTables() {
		assertThat(service.countAllMaterials()).isEqualTo(10L * ENTITIES);

		for (Class<?> entity : StatisticsService.COUNTED_ENTITIES) {
			verify(entityManager).createQuery("SELECT COUNT(e) FROM " + entity.getName() + " e", Long.class);
		}
	}

	@Test
	void returnsCachedValueUntilTtlExpires() {
		service.countAllMaterials();
		when(query.getSingleResult()).thenReturn(20L);

		now.addAndGet(StatisticsService.CACHE_TTL.toMillis() - 1);
		assertThat(service.countAllMaterials()).isEqualTo(10L * ENTITIES);
		verify(query, times(ENTITIES)).getSingleResult();
	}

	@Test
	void recountsAfterTtlExpires() {
		service.countAllMaterials();
		when(query.getSingleResult()).thenReturn(20L);

		now.addAndGet(StatisticsService.CACHE_TTL.toMillis());
		assertThat(service.countAllMaterials()).isEqualTo(20L * ENTITIES);
		verify(query, times(ENTITIES * 2)).getSingleResult();
	}
}
