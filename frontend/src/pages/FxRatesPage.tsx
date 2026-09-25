import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getFxRates, convertCurrency } from '../api/fx';
import { Spinner } from '../components/Spinner';

const POPULAR_CURRENCIES = ['USD', 'INR', 'EUR', 'GBP', 'JPY', 'AED'];

function formatRate(rate: number) {
  if (rate < 1) return rate.toFixed(6);
  if (rate < 100) return rate.toFixed(4);
  return rate.toFixed(2);
}

export default function FxRatesPage() {
  const ratesQ = useQuery({ queryKey: ['fx-rates'], queryFn: getFxRates });

  // Converter state
  const [amount, setAmount] = useState('100');
  const [fromCurrency, setFromCurrency] = useState('USD');
  const [toCurrency, setToCurrency] = useState('INR');
  const [converted, setConverted] = useState<number | null>(null);
  const [rate, setRate] = useState<number | null>(null);
  const [converting, setConverting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const supported = ratesQ.data?.supported ?? POPULAR_CURRENCIES;

  // Auto-convert when inputs change
  useEffect(() => {
    const t = setTimeout(async () => {
      const num = Number(amount);
      if (!num || num <= 0 || !fromCurrency || !toCurrency) {
        setConverted(null);
        setRate(null);
        return;
      }
      setConverting(true);
      setError(null);
      try {
        const res = await convertCurrency(num, fromCurrency, toCurrency);
        setConverted(res.converted);
        setRate(res.rate);
      } catch (e: any) {
        setError(e?.response?.data?.error || e?.message || 'Conversion failed');
        setConverted(null);
      } finally {
        setConverting(false);
      }
    }, 400);   // debounce

    return () => clearTimeout(t);
  }, [amount, fromCurrency, toCurrency]);

  const swap = () => {
    setFromCurrency(toCurrency);
    setToCurrency(fromCurrency);
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto animate-fade-in">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
          Currency Exchange
        </h2>
        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
          Live FX rates — updated every 6 hours from exchangerate.host
        </p>
      </div>

      {/* Converter card */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">Convert</div>
          {ratesQ.data && (
            <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
              Base: {ratesQ.data.base} · Updated{' '}
              {new Date(ratesQ.data.rates[0]?.fetchedAt || '').toLocaleString('en-IN', {
                hour: '2-digit',
                minute: '2-digit',
                day: '2-digit',
                month: 'short',
              })}
            </span>
          )}
        </div>
        <div className="card-body space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-[1fr_auto_1fr] gap-4 items-end">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Amount
              </label>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="input"
              />
            </div>

            <button
              type="button"
              onClick={swap}
              title="Swap currencies"
              className="hidden md:flex w-10 h-10 items-center justify-center rounded-lg bg-white dark:bg-slate-800 border border-blue-200 dark:border-slate-600 hover:bg-blue-50 dark:hover:bg-slate-700 transition-all text-slate-600 dark:text-slate-300 self-end mb-1"
            >
              ⇄
            </button>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  From
                </label>
                <select
                  value={fromCurrency}
                  onChange={(e) => setFromCurrency(e.target.value)}
                  className="input"
                >
                  {supported.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                  To
                </label>
                <select
                  value={toCurrency}
                  onChange={(e) => setToCurrency(e.target.value)}
                  className="input"
                >
                  {supported.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Result */}
          <div className="pt-4 border-t border-blue-100 dark:border-slate-700">
            {converting ? (
              <div className="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
                <Spinner size="sm" /> Converting…
              </div>
            ) : error ? (
              <div className="text-sm text-rose-600 dark:text-rose-400">{error}</div>
            ) : converted !== null ? (
              <div className="flex flex-wrap items-baseline gap-3">
                <div className="text-3xl font-bold text-emerald-700 dark:text-emerald-400 tabular-nums">
                  {fromCurrency} {Number(amount).toLocaleString('en-IN')}
                </div>
                <div className="text-2xl text-slate-400">=</div>
                <div className="text-3xl font-bold text-brand-700 dark:text-brand-300 tabular-nums">
                  {toCurrency} {converted.toLocaleString('en-IN')}
                </div>
                {rate && (
                  <div className="text-xs text-slate-500 dark:text-slate-400 ml-2">
                    1 {fromCurrency} = {formatRate(rate)} {toCurrency}
                  </div>
                )}
              </div>
            ) : (
              <div className="text-sm text-slate-400">
                Enter an amount to see the conversion
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Rates table */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">Live Rates (Base: USD)</div>
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
            {ratesQ.data?.rates.length ?? 0} currencies
          </span>
        </div>
        <div className="card-body">
          {ratesQ.isLoading ? (
            <div className="flex justify-center py-8">
              <Spinner size="md" />
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
              {ratesQ.data?.rates
                .slice()
                .sort((a, b) => a.targetCurrency.localeCompare(b.targetCurrency))
                .map((r) => (
                  <div
                    key={r.id}
                    className="flex items-center justify-between p-3 rounded-lg border border-blue-100 dark:border-slate-700 bg-gradient-to-br from-white to-blue-50/40 dark:from-slate-800 dark:to-slate-800/60 hover:border-blue-300 dark:hover:border-slate-600 transition-all"
                  >
                    <div>
                      <div className="text-sm font-bold text-slate-900 dark:text-slate-100">
                        1 USD
                      </div>
                      <div className="text-xs text-slate-500 dark:text-slate-400">
                        = {r.targetCurrency}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-base font-bold text-brand-700 dark:text-brand-300 tabular-nums">
                        {formatRate(r.rate)}
                      </div>
                      <div className="text-[10px] text-slate-400">
                        {r.source === 'exchangerate.host' ? 'live' : 'seed'}
                      </div>
                    </div>
                  </div>
                ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}