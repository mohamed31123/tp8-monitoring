package ma.ens.tp8monitoring.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final MeterRegistry meterRegistry;

    // Compteur interne des tâches exécutées (exposé comme gauge)
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    public TaskService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // Gauge : nombre de tâches actives en temps réel
        meterRegistry.gauge("tp8.tasks.active", activeTasks);
    }

    /**
     * Tâche standard — simule un traitement de ~200ms
     */
    public String executeTask() {
        Timer timer = meterRegistry.timer("tp8.tasks.duration", "type", "standard");

        return timer.record(() -> {
            activeTasks.incrementAndGet();
            logger.info("[TaskService] Démarrage de la tâche standard");

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.error("[TaskService] Interruption pendant la tâche", e);
                Thread.currentThread().interrupt();
            }

            meterRegistry.counter("tp8.tasks.completed", "type", "standard").increment();
            logger.info("[TaskService] Tâche standard terminée avec succès");
            activeTasks.decrementAndGet();

            return "Tâche exécutée en ~200ms";
        });
    }

    /**
     * Tâche lourde — simule un traitement de ~1200ms
     */
    public String executeHeavyTask() {
        Timer timer = meterRegistry.timer("tp8.tasks.duration", "type", "heavy");

        return timer.record(() -> {
            activeTasks.incrementAndGet();
            logger.warn("[TaskService] Démarrage d'une tâche lourde");

            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                logger.error("[TaskService] Interruption pendant la tâche lourde", e);
                Thread.currentThread().interrupt();
            }

            meterRegistry.counter("tp8.tasks.completed", "type", "heavy").increment();
            logger.warn("[TaskService] Tâche lourde terminée — durée élevée détectée");
            activeTasks.decrementAndGet();

            return "Tâche lourde exécutée en ~1200ms";
        });
    }
}
