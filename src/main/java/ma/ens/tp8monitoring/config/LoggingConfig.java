package ma.ens.tp8monitoring.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du système de journalisation.
 * Affiche un récapitulatif des paramètres au démarrage.
 */
@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @PostConstruct
    public void logStartup() {
        logger.info("============================================================");
        logger.info("  TP8 - Supervision & Monitoring - Spring Boot");
        logger.info("  Application démarrée avec succès");
        logger.info("  Actuator  : http://localhost:8080/actuator");
        logger.info("  Prometheus: http://localhost:8080/actuator/prometheus");
        logger.info("  Health    : http://localhost:8080/actuator/health");
        logger.info("  API Run   : http://localhost:8080/api/run");
        logger.info("  API Heavy : http://localhost:8080/api/heavy");
        logger.info("  API Status: http://localhost:8080/api/status");
        logger.info("============================================================");
    }
}
