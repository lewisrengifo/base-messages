import React, { useState } from 'react';
import Layout, { PageId } from './components/Layout';
import ConnectionPage from './pages/ConnectionPage';
import ContactsPage from './pages/ContactsPage';
import TemplatesPage from './pages/TemplatesPage';
import CampaignsPage from './pages/CampaignsPage';
import TemplateBuilderPage from './pages/TemplateBuilderPage';
import CampaignBuilderPage from './pages/CampaignBuilderPage';
import CampaignAnalyticsPage from './pages/CampaignAnalyticsPage';
import LoginPage from './pages/LoginPage';
import { AuthProvider } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { CheckCircle2 } from 'lucide-react';

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

function AppContent() {
  const [activePage, setActivePage] = useState<PageId>('campaigns');
  const [lastSubmittedTemplateName, setLastSubmittedTemplateName] = useState<string>('');

  const renderPage = () => {
    switch (activePage) {
      case 'connection':
        return <ConnectionPage />;
      case 'contacts':
        return <ContactsPage />;
      case 'templates':
        return <TemplatesPage onNavigate={setActivePage} />;
      case 'campaigns':
        return <CampaignsPage onNavigate={setActivePage} />;
      case 'template-builder':
        return (
          <TemplateBuilderPage
            onNavigate={setActivePage}
            onTemplateSubmitted={setLastSubmittedTemplateName}
          />
        );
      case 'campaign-builder':
        return <CampaignBuilderPage onNavigate={setActivePage} />;
      case 'campaign-analytics':
        return <CampaignAnalyticsPage onNavigate={setActivePage} />;
      case 'campaign-sent':
        return <CampaignSentPage onBack={() => setActivePage('campaigns')} onNavigate={setActivePage} />;
      case 'submission-sent':
        return (
          <SubmissionSentPage
            onBack={() => setActivePage('templates')}
            onNavigate={setActivePage}
            templateName={lastSubmittedTemplateName}
          />
        );
      default:
        return <CampaignsPage onNavigate={setActivePage} />;
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <LoginGate>
        <Layout activePage={activePage} onNavigate={setActivePage}>
          {renderPage()}
        </Layout>
      </LoginGate>
    </div>
  );
}

/**
 * Component that shows LoginPage if not authenticated,
 * otherwise shows children
 */
function LoginGate({ children }: { children: React.ReactNode }) {
  const [showLogin, setShowLogin] = React.useState(true);

  if (showLogin) {
    return <LoginPage onLogin={() => setShowLogin(false)} />;
  }

  return <>{children}</>;
}

function CampaignSentPage({ onBack, onNavigate }: { onBack: () => void, onNavigate: (page: PageId) => void }) {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center space-y-12">
      <div className="relative">
        <div className="w-32 h-32 bg-emerald-500/10 rounded-full flex items-center justify-center text-emerald-600">
          <CheckCircle2 size={64} strokeWidth={1.5} />
        </div>
        <div className="absolute -inset-4 bg-emerald-500/5 rounded-full blur-2xl -z-10 animate-pulse" />
      </div>

      <div className="max-w-xl space-y-4">
        <h1 className="text-4xl font-extrabold tracking-tight">Campaign Launched!</h1>
        <p className="text-muted-foreground text-lg leading-relaxed">
          Your broadcast campaign has been successfully queued. Delivery will begin according to your schedule.
        </p>
      </div>

      <div className="flex flex-wrap justify-center gap-4 pt-4">
        <Button 
          onClick={onBack}
          className="h-14 px-10 font-bold rounded-xl shadow-xl shadow-primary/20 active:scale-95 transition-all"
        >
          View Campaign History
        </Button>
        <Button 
          variant="secondary" 
          onClick={() => onNavigate('campaign-builder')}
          className="h-14 px-10 font-bold rounded-xl active:scale-95"
        >
          Create New Campaign
        </Button>
      </div>

      <div className="w-full max-w-4xl grid grid-cols-1 md:grid-cols-3 gap-6 pt-12">
        <Card className="bg-muted/30 border-none border-b-4 border-b-emerald-500/20 rounded-2xl text-left">
          <CardContent className="p-6">
            <h3 className="technical-label mb-4 text-emerald-600">Queued</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              Your messages are being prepared for delivery. This includes variable resolution and rate-limit optimization.
            </p>
          </CardContent>
        </Card>
        <Card className="bg-muted/30 border-none border-b-4 border-b-primary/20 rounded-2xl text-left">
          <CardContent className="p-6">
            <h3 className="technical-label mb-4 text-primary">Delivering</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              Messages are actively being sent to recipients. Real-time metrics will begin appearing in your dashboard.
            </p>
          </CardContent>
        </Card>
        <Card className="bg-muted/30 border-none border-b-4 border-b-amber-500/20 rounded-2xl text-left">
          <CardContent className="p-6">
            <h3 className="technical-label mb-4 text-amber-600">Completed</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              All messages have been processed. Review the final delivery report for detailed engagement analytics.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function SubmissionSentPage({ onBack, onNavigate, templateName }: { onBack: () => void, onNavigate: (page: PageId) => void, templateName?: string }) {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center space-y-12">
      <div className="relative">
        <div className="w-32 h-32 bg-primary/10 rounded-full flex items-center justify-center text-primary">
          <CheckCircle2 size={64} strokeWidth={1.5} />
        </div>
        <div className="absolute -inset-4 bg-primary/5 rounded-full blur-2xl -z-10 animate-pulse" />
      </div>

      <div className="max-w-xl space-y-4">
        <h1 className="text-4xl font-extrabold tracking-tight">Submission Sent</h1>
        <p className="text-muted-foreground text-lg">
          Your template <span className="font-bold text-primary">{templateName || 'your template'}</span> has been sent to Meta for approval. This usually takes 24-48 hours.
        </p>
      </div>

      <div className="flex flex-wrap justify-center gap-4 pt-4">
        <Button 
          onClick={onBack}
          className="h-14 px-10 font-bold rounded-xl shadow-xl shadow-primary/20 active:scale-95 transition-all"
        >
          Go to Template Manager
        </Button>
        <Button 
          variant="secondary" 
          onClick={() => onNavigate('template-builder')}
          className="h-14 px-10 font-bold rounded-xl active:scale-95"
        >
          Create Another Template
        </Button>
      </div>

      <div className="w-full max-w-4xl grid grid-cols-1 md:grid-cols-3 gap-6 pt-12">
        <Card className="bg-muted/30 border-none border-b-4 border-b-primary/20 rounded-2xl text-left">
          <CardContent className="p-6">
            <h3 className="technical-label mb-4 text-primary">Pending</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              Meta's automated and manual review systems check for policy compliance. You cannot edit the template during this phase.
            </p>
          </CardContent>
        </Card>
        <Card className="bg-muted/30 border-none border-b-4 border-b-emerald-500/20 rounded-2xl text-left">
          <CardContent className="p-6">
            <h3 className="technical-label mb-4 text-emerald-600">Approved</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              Once approved, your template is ready for use in active campaigns. You will receive a system notification immediately.
            </p>
          </CardContent>
        </Card>
        <Card className="bg-muted/30 border-none border-b-4 border-b-destructive/20 rounded-2xl text-left">
          <CardContent className="p-6">
            <h3 className="technical-label mb-4 text-destructive">Rejected</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              If rejected, Meta provides a specific reason. You can then edit the content and re-submit for review.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
