package com.serviceabonnement.scheduler;

import com.serviceabonnement.client.AnalyseClient;
import com.serviceabonnement.entity.AnalytiqueTrace;
import com.serviceabonnement.repository.AnalytiqueTraceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalytiqueScheduler {

    private final AnalytiqueTraceRepository repository;
    private final AnalyseClient analyseClient;

    /**
     * Tâche planifiée pour envoyer les traces d'analyse par batch toutes les 30 minutes.
     * Si l'envoi réussit, les traces sont supprimées de la base de données.
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    @Transactional
    public void sendBatchToAnalyse() {
        List<AnalytiqueTrace> traces = repository.findAll();
        
        if (traces.isEmpty()) {
            return;
        }

        log.info("Tentative d'envoi d'un batch de {} traces à l'analyse", traces.size());

        try {
            List<java.util.Map<String, Object>> dtos = traces.stream()
                    .map(t -> {
                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        map.put("schemaVersion", 1); // Indispensable pour G8
                        map.put("timestamp", t.getTimestamp());
                        map.put("userId", t.getUserId());
                        map.put("action", t.getAction());
                        map.put("planType", t.getPlanType());
                        return map;
                    })
                    .collect(Collectors.toList());

            analyseClient.sendEvents(dtos);
            
            // Suppression après succès de l'envoi
            repository.deleteAll(traces);
            log.info("Batch d'analyse envoyé avec succès. {} traces supprimées.", traces.size());
            
        } catch (Exception e) {
            log.error("Échec de l'envoi du batch d'analyse (sera réessayé au prochain cycle): {}", e.getMessage());
        }
    }
}
