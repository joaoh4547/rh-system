import { type LucideIcon } from 'lucide-react';
import React from 'react';
import { createRoot } from 'react-dom/client';

export function renderIcon(IconComponent: LucideIcon, container: HTMLElement, props: any = {}) {
  const root = createRoot(container);
  root.render(React.createElement(IconComponent, {
    size: '100%',
    strokeWidth: 2,
    ...props
  }));
}

// Helper to make it easier to use from Vaadin if needed via JS execution
(window as any).LucideIcons = {
  render: (iconName: string, container: HTMLElement, props: any) => {
    import('lucide-react').then(lucide => {
      const Icon = (lucide as any)[iconName];
      if (Icon) {
        renderIcon(Icon, container, props);
      }
    });
  }
};
