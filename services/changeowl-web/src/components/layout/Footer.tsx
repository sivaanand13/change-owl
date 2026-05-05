import { useTranslations } from "next-intl";

export default function Footer() {
  const t = useTranslations("Footer");

  return (
    <footer className="w-full border-t border-slate-200 bg-brand-background py-12 dark:border-slate-800 text-small">
      <div className="container mx-auto px-4">
        <div className="flex flex-col items-center justify-between gap-6 md:flex-row">
          <div className="text-center md:text-left">
            <p className="font-semibold text-brand-secondary">ChangeOwl</p>
            <p className="text-text-muted mt-1">{t("description")}</p>
          </div>

          <div className="flex gap-8 font-medium text-text-muted">
            <a
              href="https://www.linkedin.com/in/siva-anand-sivakumar/"
              className="hover:text-brand-primary"
            >
              {t("links.linkedin")}
            </a>
            <a
              href="https://github.com/sivaanand13/"
              className="hover:text-brand-primary"
            >
              {t("links.github")}
            </a>
            <a
              href="https://github.com/sivaanand13/change-owl"
              className="hover:text-brand-primary"
            >
              {t("links.source")}
            </a>
          </div>

          <p className="text-text-muted uppercase tracking-widest">
            &copy; {new Date().getFullYear()} ChangeOwl
          </p>
        </div>
      </div>
    </footer>
  );
}
