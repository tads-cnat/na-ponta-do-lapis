package com.npl.na_ponta_do_lapis.service.openfinance;

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
     * Executa a sincronização diária de madrugada (às 03:00) para todos os usuários com Open Finance conectado.
     */
    @Scheduled(cron = "${app.openfinance.cron:0 0 3 * * *}")
    public void executarSincronizacaoNoturna() {
        log.info("[OpenFinanceScheduler] Iniciando rotina noturna de sincronização do Open Finance");
        syncService.sincronizarTodosUsuariosAutomatico();
        log.info("[OpenFinanceScheduler] Rotina noturna de sincronização finalizada");
    }
}

