import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '../lib/api/http';

interface AsyncState<T> {
  data: T | null;
  error: string | null;
  loading: boolean;
}

export function useAsync<T>(loader: () => Promise<T>, deps: unknown[] = []) {
  const [state, setState] = useState<AsyncState<T>>({
    data: null,
    error: null,
    loading: true,
  });

  const reload = useCallback(async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const data = await loader();
      setState({ data, error: null, loading: false });
    } catch (err) {
      const message =
        err instanceof ApiError
          ? `${err.status}: ${err.message}`
          : err instanceof Error
            ? err.message
            : 'Nieznany błąd';
      setState({ data: null, error: message, loading: false });
    }
  }, deps);

  useEffect(() => {
    void reload();
  }, [reload]);

  return { ...state, reload };
}
