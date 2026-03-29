# TP8 — Supervision et Journalisation avec Spring Boot

> **Stack :** Spring Boot 3.2.4 · Spring Boot Actuator · Micrometer · Prometheus · Grafana · Logback

---

## Objectif

Mettre en place une chaîne complète d'observabilité pour une application Spring Boot :
- Collecte de métriques via **Spring Boot Actuator**
- Export vers **Prometheus** pour la centralisation
- Visualisation dynamique dans **Grafana**
- Journalisation structurée avec **Logback**

---

## Structure du projet

```
src/main/java/ma/ens/tp8monitoring
 ├── Tp8MonitoringApplication.java
 ├── controller/
 │     └── TaskController.java        # Endpoints : /api/run, /api/heavy, /api/fail, /api/status
 ├── service/
 │     └── TaskService.java           # Logique métier + métriques custom (Timer, Gauge)
 ├── config/
 │     └── LoggingConfig.java         # Banner de démarrage + config logs
 └── metrics/
       └── MetricsConfig.java         # Enregistrement JVM Memory, Threads, CPU
```

---

## Dépendances Maven

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## Configuration (`application.properties`)

```properties
# Actuator
management.endpoints.web.exposure.include=*
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=always
management.endpoint.prometheus.enabled=true
management.prometheus.metrics.export.enabled=true
management.info.env.enabled=true

# Informations de l'application
info.app.name=TP8 Spring Monitoring
info.app.version=1.0.0
info.app.description=Supervision et Logging avec Actuator + Prometheus + Grafana

# Logging
logging.level.root=INFO
logging.level.ma.ens.tp8monitoring=DEBUG
logging.file.name=logs/tp8-monitoring.log
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{40} - %msg%n
```

---

## Étape 1 — Démarrage

```bash
mvn spring-boot:run
```

---

## Étape 2 — Endpoints Actuator

| Endpoint | URL |
|----------|-----|
| Health Check | http://localhost:8080/actuator/health |
| Informations | http://localhost:8080/actuator/info |
| Métriques | http://localhost:8080/actuator/metrics |
| Export Prometheus | http://localhost:8080/actuator/prometheus |

### `/actuator/health`

![Actuator Health](https://github.com/user-attachments/assets/d62b33d3-8c12-444a-80ed-787627a47307)

### `/actuator/info`

![Actuator Info](https://github.com/user-attachments/assets/c854bbaf-350c-4ec2-ae80-8741e6dc6ae4)

### `/actuator/metrics`

![Actuator Metrics](https://github.com/user-attachments/assets/e73ccea5-0711-4058-af5f-e210b3bc85e4)

---

## Étape 3 — Journalisation (Logging)

```properties
logging.level.root=INFO
logging.level.ma.ens.tp8monitoring=DEBUG
logging.file.name=logs/tp8-monitoring.log
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{40} - %msg%n
```

Observer les logs en temps réel :

```bash
tail -f logs/tp8-monitoring.log
```

> **Note :** Le fichier `tp8-monitoring.log` enregistre toutes les activités et événements de l'application lors de son exécution. Chaque ligne contient la date, le thread, le niveau de log, la classe source et le message.

![Logs en direct](https://github.com/user-attachments/assets/18b002d7-f58b-43a0-bce0-7c2939c16925)

---

## Étape 4 — Export Prometheus

L'endpoint `/actuator/prometheus` expose toutes les métriques au format texte lisible par Prometheus :

```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 3.42E7

http_server_requests_seconds_count{method="GET",status="200",uri="/api/run"} 5.0
tp8_tasks_duration_seconds_count{type="standard"} 3.0
tp8_api_run_calls_total 5.0
```

![Prometheus Endpoint](https://github.com/user-attachments/assets/a60424b1-c38c-4f84-871f-76225e66ad24)

---

## Étape 5 — Configuration Prometheus

Modifier `prometheus.yml` :

```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: 'tp8-spring-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

Lancer Prometheus :

```bash
./prometheus --config.file=prometheus.yml
```

Accès : http://localhost:9090

### Exemples de requêtes PromQL

```promql
# Requêtes par seconde
rate(http_server_requests_seconds_count[1m])

# Mémoire heap utilisée
jvm_memory_used_bytes{area="heap"}

# Nombre d'appels sur /api/run
tp8_api_run_calls_total
```

---

## Étape 6 — Règles d'alertes Prometheus

```yaml
groups:
  - name: tp8-alertes
    rules:
      - alert: ApplicationIndisponible
        expr: up{job="tp8-spring-app"} == 0
        for: 10s
        labels:
          severity: critical
        annotations:
          summary: "Application TP8 indisponible"

      - alert: LatenceElevee
        expr: rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m]) > 1
        for: 30s
        labels:
          severity: warning
        annotations:
          summary: "Latence HTTP supérieure à 1 seconde"

      - alert: MemoireHeapCritique
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.8
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Mémoire heap JVM > 80%"
```

![Règle d'alerte Prometheus](https://github.com/user-attachments/assets/c4f6a650-28fa-465b-9fe1-8e69cca1f3ee)

---

## Étape 7 — Visualisation Grafana

1. Lancer Grafana → http://localhost:3000 (login : `admin / admin`)
2. **Configuration** → **Data Sources** → **Add data source** → choisir **Prometheus** → URL : `http://localhost:9090`
3. **Dashboards** → **Import** → entrer l'ID **`4701`** (Spring Boot Statistics)

Grafana affiche :
- Requêtes HTTP/minute
- Temps moyen de réponse
- Utilisation CPU et mémoire JVM
- Nombre d'erreurs HTTP

---

## Étape 8 — Docker Compose (optionnel)

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - ./alert-rules.yml:/etc/prometheus/alert-rules.yml
    ports:
      - "9090:9090"
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

```bash
docker-compose up -d
```

---

## Métriques custom implémentées

| Métrique | Type | Description |
|----------|------|-------------|
| `tp8.api.run.calls` | Counter | Nombre d'appels sur `/api/run` |
| `tp8.api.heavy.calls` | Counter | Nombre d'appels sur `/api/heavy` |
| `tp8.api.errors.simulated` | Counter | Nombre d'erreurs simulées |
| `tp8.tasks.duration` | Timer | Durée des tâches (standard / heavy) |
| `tp8.tasks.active` | Gauge | Nombre de tâches en cours d'exécution |

---

## Récapitulatif des URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080/api/run | — |
| Actuator | http://localhost:8080/actuator | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / admin |
