import { useEffect } from 'react';

type PageTitle = string;

const PAGE_TITLES: Record<string, PageTitle> = {
  campaigns: 'Campaigns',
  'campaign-builder': 'Create Campaign',
  'campaign-analytics': 'Campaign Analytics',
  'campaign-sent': 'Campaign Launched',
  templates: 'Templates',
  'template-builder': 'Template Builder',
  'submission-sent': 'Submission Sent',
  contacts: 'Contacts',
  connection: 'Connection',
};

const BASE_TITLE = 'Base';

export function usePageTitle(activePage: string) {
  useEffect(() => {
    const pageTitle = PAGE_TITLES[activePage];
    document.title = pageTitle ? `${pageTitle} | ${BASE_TITLE}` : BASE_TITLE;
  }, [activePage]);
}
