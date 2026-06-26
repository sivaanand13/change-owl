import { Streamdown } from 'streamdown';

type Props = {
  content: string;
  className?: string;
};

export function MessageRenderer({ content, className }: Props) {
  return <Streamdown className={className}>{content}</Streamdown>;
}
