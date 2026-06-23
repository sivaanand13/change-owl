import { ArtifactFeed } from '@/components/feed/ArtifactFeed';
import { Suspense } from 'react';

export default function Home() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <ArtifactFeed />
    </Suspense>
  );
}
