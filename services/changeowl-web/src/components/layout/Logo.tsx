import Image from "next/image";
import Link from "next/link";

export default function Logo() {
  return (
    <Link
      href="/"
      className="flex items-center transition-opacity hover:opacity-90 w-fit"
    >
      <Image
        src="/change-owl.png"
        alt="ChangeOwl Logo"
        width={100}
        height={100}
        priority
      />
      <span className="text-logo font-bold tracking-tight text-brand-secondary">
        Change<span className="text-brand-primary">Owl</span>
      </span>
    </Link>
  );
}
