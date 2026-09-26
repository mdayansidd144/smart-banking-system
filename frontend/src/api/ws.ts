import { Client } from '@stomp/stompjs';

export type WsStatus = 'connecting' | 'connected' | 'disconnected';

export interface WsEvent {
  type: string;
  accountId: string | null;
  timestamp: string;
  data: any;
}

type StatusListener = (s: WsStatus) => void;
type EventListener = (e: WsEvent) => void;

class WsClient {
  private client: Client | null = null;
  private status: WsStatus = 'disconnected';
  private statusListeners = new Set<StatusListener>();
  private eventListeners = new Set<EventListener>();
  private subscriptions = new Map<string, any>();
  private reconnectAttempts = 0;

  connect(): void {
    if (this.client?.active) return;
    this.setStatus('connecting');

    // Native WebSocket — no SockJS
    const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const socketUrl = `${proto}//${window.location.host}/ws`;

    this.client = new Client({
      brokerURL: socketUrl,
      reconnectDelay: 0,          // we handle retry ourselves for exponential backoff
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {
        // silence — set to (msg) => console.log('[WS]', msg) for debugging
      },
    });

    this.client.onConnect = () => {
      this.reconnectAttempts = 0;
      this.setStatus('connected');

      // Global event channel
      this.subscribe('/topic/events', (e) => this.dispatch(e));

      // Per-event channels
      [
        'anomaly.detected',
        'account.frozen',
        'money.deposited',
        'money.withdrawn',
        'money.transferred',
        'account.created',
        'budget.alert',
        'bill.alert',
      ].forEach((topic) => {
        this.subscribe(`/topic/${topic}`, (e) => this.dispatch(e));
      });
    };

    this.client.onWebSocketClose = () => {
      this.setStatus('disconnected');
      this.scheduleReconnect();
    };

    this.client.onStompError = (frame) => {
      console.error('[WS] STOMP error:', frame.headers, frame.body);
      this.setStatus('disconnected');
      this.scheduleReconnect();
    };

    this.client.activate();
  }

  private scheduleReconnect() {
    this.reconnectAttempts += 1;
    const delay = Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts));
    setTimeout(() => {
      if (this.status !== 'connected') {
        this.connect();
      }
    }, delay);
  }

  private subscribe(destination: string, handler: (e: WsEvent) => void) {
    if (!this.client?.connected) return;
    if (this.subscriptions.has(destination)) return;

    const sub = this.client.subscribe(destination, (msg) => {
      try {
        handler(JSON.parse(msg.body) as WsEvent);
      } catch (err) {
        console.warn('[WS] Bad message on', destination, err);
      }
    });
    this.subscriptions.set(destination, sub);
  }

  /** Subscribe to a specific account's events */
  subscribeToAccount(accountId: string): () => void {
    const dest = `/topic/account.${accountId}`;
    this.subscribe(dest, (e) => this.dispatch(e));
    return () => {
      const sub = this.subscriptions.get(dest);
      if (sub) {
        sub.unsubscribe();
        this.subscriptions.delete(dest);
      }
    };
  }

  private dispatch(event: WsEvent) {
    this.eventListeners.forEach((l) => l(event));
  }

  // ---------- Public API ----------

  onStatus(listener: StatusListener): () => void {
    this.statusListeners.add(listener);
    listener(this.status);
    return () => {
      this.statusListeners.delete(listener);
    };
  }

  onEvent(listener: EventListener): () => void {
    this.eventListeners.add(listener);
    return () => {
      this.eventListeners.delete(listener);
    };
  }

  getStatus(): WsStatus {
    return this.status;
  }

  private setStatus(s: WsStatus) {
    this.status = s;
    this.statusListeners.forEach((l) => l(s));
  }
}

export const ws = new WsClient();
