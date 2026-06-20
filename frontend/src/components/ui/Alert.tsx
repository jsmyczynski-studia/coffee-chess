import type { ReactNode } from 'react';

type Tone = 'info' | 'success' | 'error' | 'warning';

interface AlertProps {
  tone?: Tone;
  title?: string;
  children: ReactNode;
}

const toneClass: Record<Tone, string> = {
  info: 'alert alert-info',
  success: 'alert alert-success',
  error: 'alert alert-error',
  warning: 'alert alert-warning',
};

export function Alert({ tone = 'info', title, children }: AlertProps) {
  return (
    <div className={toneClass[tone]} role="alert">
      {title && <strong>{title}</strong>}
      <div>{children}</div>
    </div>
  );
}
