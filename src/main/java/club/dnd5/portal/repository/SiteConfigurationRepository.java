package club.dnd5.portal.repository;

import club.dnd5.portal.model.SiteConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteConfigurationRepository  extends JpaRepository<SiteConfiguration, String>  {
}
