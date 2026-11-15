package com.npl.na_ponta_do_lapis.service.openfinance;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OpenFinanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(OpenFinanceScheduler.class);
    private final OpenFinanceSyncService syncService;

    public OpenFinanceScheduler(OpenFinanceSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Executa a sincronização automática periódica (às 03:00 e às 15:00 todos os dias)
     * para todos os usuários com Open Finance conectado.
     * O lock garante execução única entre múltiplas réplicas.
     */
    @Scheduled(cron = "${app.openfinance.cron:0 0 3,15 * * *}")
    @SchedulerLock(name = "openFinanceSyncPeriodica", lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M")
    public void executarSincronizacaoPeriodica() {
        log.info("[OpenFinanceScheduler] Iniciando rotina periódica de sincronização do Open Finance");
        syncService.sincronizarTodosUsuariosAutomatico();
        log.info("[OpenFinanceScheduler] Rotina periódica de sincronização finalizada");
    }
}

