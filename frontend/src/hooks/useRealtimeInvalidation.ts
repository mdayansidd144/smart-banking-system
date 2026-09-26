import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { ws, type WsEvent } from '../api/ws';
export function useRealtimeInvalidation() {
  const qc = useQueryClient();

  useEffect(() => {
    const off = ws.onEvent((event: WsEvent) => {
      switch (event.type) {
        case 'account.created':
          qc.invalidateQueries({ queryKey: ['accounts'] });
          break;

        case 'account.frozen':
          qc.invalidateQueries({ queryKey: ['accounts'] });
          if (event.accountId) qc.invalidateQueries({ queryKey: ['account', event.accountId] });
          break;

        case 'money.deposited':
        case 'money.withdrawn':
        case 'money.transferred':
          qc.invalidateQueries({ queryKey: ['accounts'] });
          if (event.accountId) {
            qc.invalidateQueries({ queryKey: ['account', event.accountId] });
            qc.invalidateQueries({ queryKey: ['transactions', event.accountId] });
          }
          break;

        case 'anomaly.detected':
          qc.invalidateQueries({ queryKey: ['anomalies'] });
          qc.invalidateQueries({ queryKey: ['fraud-alerts'] });
          break;

        case 'budget.alert':
          qc.invalidateQueries({ queryKey: ['budgets'] });
          break;

        case 'bill.alert':
          qc.invalidateQueries({ queryKey: ['bills'] });
          break;
      }
    });
    return off;
  }, [qc]);
}