import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';
import Footer from '@/components/layout/Footer';
import Header from '@/components/layout/Header';
import { NextIntlClientProvider } from 'next-intl';
import StatusTicker from '@/components/ticker/StatusTicker';
import { NuqsAdapter } from 'nuqs/adapters/next/app';
import { Suspense } from 'react';
import { SearchAssistant } from '@/components/assistant/SearchAssistant';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  title: 'ChangeOwl',
  description: 'Technical Intelligence Platform',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col bg-brand-surface">
        <NuqsAdapter>
          <NextIntlClientProvider>
            <StatusTicker />
            <Header />
            <main className="grow">{children}</main>
          </NextIntlClientProvider>
          <SearchAssistant />
          <Footer />
        </NuqsAdapter>
      </body>
    </html>
  );
}
