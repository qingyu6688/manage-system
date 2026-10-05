package com.example.managesystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public final class FileTransactions {
    private FileTransactions() { }

    public static void onRollback(Runnable cleanup) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) { runCleanup(cleanup); }
            }
        });
    }

    public static void afterCommit(Runnable cleanup) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cleanup.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { runCleanup(cleanup); }
        });
    }

    private static void runCleanup(Runnable cleanup) {
        try {
            cleanup.run();
        } catch (RuntimeException exception) {
            // 数据库已经完成事务，物理清理失败由运维根据日志重试，不能伪装成数据库回滚。
            log.error("FILE_CLEANUP_FAILED 文件清理失败，请核对存储目录并重试", exception);
        }
    }
}
