import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Button } from './ui/Button';
import { Card } from './ui/Card';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    console.error('UI error', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Card title="Coś poszło nie tak">
          <p className="muted">Odśwież stronę lub wróć na stronę główną.</p>
          <Button onClick={() => window.location.assign('/')}>Strona główna</Button>
        </Card>
      );
    }

    return this.props.children;
  }
}
