import { useEffect, useState } from 'react';
import { Spinner } from './Spinner';

const STAGES = [
  { key: 'fetch',    label: 'Fetching account data',         duration: 1500 },
  { key: 'analyze',  label: 'Analyzing transaction history',  duration: 2500 },
  { key: 'score',    label: 'Computing risk score',           duration: 3000 },
  { key: 'decide',   label: 'Generating decision',            duration: 2000 },
];

interface Props {
  active: boolean;
}

export default function AiProgressPanel({ active }: Props) {
  const [currentStage, setCurrentStage] = useState(0);

  useEffect(() => {
    if (!active) {
      setCurrentStage(0);
      return;
    }
    setCurrentStage(0);
    const timers: ReturnType<typeof setTimeout>[] = [];
    let elapsed = 0;
    STAGES.forEach((stage, i) => {
      elapsed += stage.duration;
      timers.push(
        setTimeout(() => {
          setCurrentStage(i + 1);
        }, elapsed)
      );
    });
    return () => timers.forEach(clearTimeout);
  }, [active]);

  if (!active) return null;

  return (
    <div className="card border-blue-200 bg-gradient-to-br from-blue-50/80 to-white animate-fade-in">
      <div className="card-body">
        <div className="flex items-center gap-3 mb-5">
          <Spinner size="md" />
          <div>
            <div className="text-sm font-semibold text-brand-700">
              AI is analyzing your application
            </div>
            <div className="text-xs text-slate-500 mt-0.5">
              This may take 10–30 seconds
            </div>
          </div>
        </div>

        <div className="space-y-3">
          {STAGES.map((stage, i) => {
            const done = i < currentStage;
            const running = i === currentStage;
            return (
              <div key={stage.key} className="flex items-center gap-3">
                <div
                  className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold transition-all duration-300 ${
                    done
                      ? 'bg-gradient-to-br from-brand-500 to-brand-700 text-white scale-100'
                      : running
                      ? 'bg-brand-100 text-brand-700 ring-2 ring-brand-300 ring-offset-1 scale-105'
                      : 'bg-slate-100 text-slate-400 scale-100'
                  }`}
                >
                  {done ? '✓' : i + 1}
                </div>
                <div
                  className={`text-sm transition-all duration-300 ${
                    done
                      ? 'text-slate-500 line-through'
                      : running
                      ? 'text-slate-900 font-semibold'
                      : 'text-slate-400'
                  }`}
                >
                  {stage.label}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}