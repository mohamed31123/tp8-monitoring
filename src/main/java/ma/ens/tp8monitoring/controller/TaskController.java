package ma.ens.tp8monitoring.controller;

import io.micrometer.core.instrument.MeterRegistry;
import ma.ens.tp8monitoring.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final MeterRegistry meterRegistry;

    public TaskController(TaskService taskService, MeterRegistry meterRegistry) {
        this.taskService = taskService;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Endpoint principal : déclenche un traitement simulé
     */
    @GetMapping("/run")
    public ResponseEntity<Map<String, Object>> runTask() {
        logger.info("Requête reçue sur /api/run");
        meterRegistry.counter("tp8.api.run.calls").increment();

        String result = taskService.executeTask();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", result,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Endpoint simulant une tâche lourde (latence élevée)
     */
    @GetMapping("/heavy")
    public ResponseEntity<Map<String, Object>> heavyTask() {
        logger.warn("Tâche lourde déclenchée — latence attendue élevée");
        meterRegistry.counter("tp8.api.heavy.calls").increment();

        String result = taskService.executeHeavyTask();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", result,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Endpoint simulant une erreur métier (pour tester les alertes)
     */
    @GetMapping("/fail")
    public ResponseEntity<Map<String, Object>> failTask() {
        logger.error("Simulation d'erreur déclenchée sur /api/fail");
        meterRegistry.counter("tp8.api.errors.simulated").increment();

        return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Erreur simulée pour les tests de supervision",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Endpoint de statut applicatif custom
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> appStatus() {
        logger.debug("Consultation du statut applicatif");
        return ResponseEntity.ok(Map.of(
                "application", "tp8-monitoring",
                "version", "1.0.0",
                "uptime", "running",
                "javaVersion", System.getProperty("java.version")
        ));
    }
}
