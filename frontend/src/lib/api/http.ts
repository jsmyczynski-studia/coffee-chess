import { env } from '../../config/env';

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly body?: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export type ServiceTarget = 'user' | 'game' | 'analysis';

function resolveBase(target: ServiceTarget): string {
  const map: Record<ServiceTarget, string> = {
    user: env.api.user,
    game: env.api.game,
    analysis: env.api.analysis,
  };
  return map[target].replace(/\/$/, '');
}

export async function apiRequest<T>(
  target: ServiceTarget,
  path: string,
  init: RequestInit = {},
  token?: string,
): Promise<T> {
  const base = resolveBase(target);
  const url = `${base}${path.startsWith('/') ? path : `/${path}`}`;

  const headers = new Headers(init.headers);
  headers.set('Accept', 'application/json');

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  if (init.body != null && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(url, { ...init, headers });

  if (!response.ok) {
    const body = await response.text().catch(() => '');
    let message = response.statusText || 'Request failed';

    try {
      const json = JSON.parse(body) as { message?: string; error?: string };
      message = json.message ?? json.error ?? message;
    } catch {
      if (body) message = body;
    }

    throw new ApiError(message, response.status, body);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}
