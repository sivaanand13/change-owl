import { LinkSafetyModalProps, Streamdown } from 'streamdown';

type Props = {
  content: string;
  className?: string;
};

function CustomLinkModal({ url, isOpen, onClose, onConfirm }: LinkSafetyModalProps) {
  if (!isOpen) return null;

  return (
    <div
      className="absolute inset-0 z-50 flex items-center justify-center bg-foreground/20 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="mx-4 w-full max-w-md rounded-card bg-brand-surface p-card-padding shadow-card border border-border-subtle"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-card-title font-bold text-foreground">External Link</h2>

        <p className="mt-2 text-card-body text-text-muted">
          You&apos;re about to visit an external website:
        </p>

        <code className="mt-4 block rounded-md bg-surface-interactive p-3 text-sm break-all text-foreground">
          {url}
        </code>

        <div className="mt-6 flex justify-end gap-3">
          <button
            onClick={onClose}
            className="rounded-md border border-border-subtle px-4 py-2 text-sm font-medium text-text-muted transition-colors hover:bg-surface-interactive"
          >
            Cancel
          </button>

          <button
            onClick={() => {
              onConfirm();
              onClose();
            }}
            className="rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-brand-secondary"
          >
            Continue
          </button>
        </div>
      </div>
    </div>
  );
}

export function MessageRenderer({ content, className }: Props) {
  return (
    <div className={className}>
      <Streamdown
        linkSafety={{
          enabled: true,
          renderModal: (props) => <CustomLinkModal {...props} />,
        }}
        className={className}
      >
        {content}
      </Streamdown>
    </div>
  );
}
