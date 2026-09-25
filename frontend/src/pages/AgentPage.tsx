import { useState, useRef, useEffect } from 'react';
import { Spinner } from '../components/Spinner';
import { chatWithAgent } from '../api/endpoints';
import VoiceInput from '../components/VoiceInput';

interface Message {
  role: 'user' | 'agent';
  text: string;
  at: string;
}

const SUGGESTIONS = [
  'How many accounts do we have?',
  'What is the total bank balance?',
  'Show me open anomalies',
  'List top 5 accounts by balance',
  'How many HIGH severity anomalies?',
];

export default function AgentPage() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string | undefined>(undefined);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const send = async (text: string) => {
    const trimmed = text.trim();
    if (!trimmed || loading) return;

    setMessages((m) => [
      ...m,
      { role: 'user', text: trimmed, at: new Date().toISOString() },
    ]);
    setInput('');
    setLoading(true);

    try {
      const res = await chatWithAgent(trimmed, sessionId);
      if (!sessionId && res.sessionId) {
        setSessionId(res.sessionId);
      }
      setMessages((m) => [
        ...m,
        { role: 'agent', text: res.reply, at: res.repliedAt },
      ]);
    } catch {
      setMessages((m) => [
        ...m,
        {
          role: 'agent',
          text: 'Sorry, I could not reach the AI service. Please try again.',
          at: new Date().toISOString(),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const clearChat = () => {
    setMessages([]);
    setSessionId(undefined);
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">Ask SmartBank</h2>
          <p className="text-sm text-slate-600 mt-1">
            Ask questions in plain English — or click the mic and speak
          </p>
        </div>
        {messages.length > 0 && (
          <button onClick={clearChat} className="btn btn-ghost btn-sm">
            New Conversation
          </button>
        )}
      </div>

      <div className="card">
        <div className="card-body min-h-[400px] max-h-[560px] overflow-y-auto space-y-4">
          {messages.length === 0 && (
            <div className="text-center py-8">
              <div className="text-sm text-slate-500 mb-4">
                Try one of these questions, or click the mic to speak:
              </div>
              <div className="flex flex-wrap gap-2 justify-center">
                {SUGGESTIONS.map((s) => (
                  <button
                    key={s}
                    onClick={() => send(s)}
                    className="text-xs px-3 py-2 rounded-lg bg-blue-50 hover:bg-blue-100 text-brand-700 border border-blue-200 transition-all duration-200 hover:-translate-y-0.5"
                  >
                    {s}
                  </button>
                ))}
              </div>
            </div>
          )}

          {messages.map((m, i) => (
            <div
              key={i}
              className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[80%] rounded-2xl px-4 py-3 text-sm whitespace-pre-wrap leading-relaxed animate-fade-in ${
                  m.role === 'user'
                    ? 'bg-gradient-to-br from-brand-500 to-brand-600 text-white shadow-md shadow-brand-500/20'
                    : 'bg-blue-50 text-slate-800 border border-blue-100'
                }`}
              >
                {m.text}
              </div>
            </div>
          ))}

          {loading && (
            <div className="flex justify-start">
              <div className="bg-blue-50 border border-blue-100 rounded-2xl px-4 py-3 flex items-center gap-2 text-sm text-slate-600">
                <Spinner size="sm" />
                Thinking…
              </div>
            </div>
          )}

          <div ref={bottomRef} />
        </div>

        <div className="border-t border-blue-100 p-4">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              send(input);
            }}
            className="flex gap-3 items-stretch"
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask anything… or click the mic to speak"
              className="input flex-1"
              disabled={loading}
            />
            <VoiceInput onResult={send} disabled={loading} />
            <button
              type="submit"
              disabled={loading || !input.trim()}
              className="btn btn-primary"
            >
              Send
            </button>
          </form>
          {sessionId && (
            <div className="text-[10px] text-slate-400 mt-2 font-mono">
              Session: {sessionId.slice(0, 8)}…
            </div>
          )}
        </div>
      </div>

      <div className="text-xs text-slate-500 text-center">
        Voice input works best in Chrome and Edge. Firefox and Safari may not support it.
      </div>
    </div>
  );
}