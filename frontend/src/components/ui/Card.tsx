import type { ReactNode } from 'react';

interface CardProps {
  title?: string;
  subtitle?: string;
  children: ReactNode;
  className?: string;
}

export function Card({ title, subtitle, children, className = '' }: CardProps) {
  return (
    <section className={`card ${className}`.trim()}>
      {(title || subtitle) && (
        <header className="card-header">
          {title && <h1 className="card-title">{title}</h1>}
          {subtitle && <p className="card-subtitle">{subtitle}</p>}
        </header>
      )}
      {children}
    </section>
  );
}
