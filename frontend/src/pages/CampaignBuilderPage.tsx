import React, { useEffect, useMemo, useState } from 'react';
import { ArrowLeft, Megaphone, Users, FileText, Calendar, Send, CheckCircle2, Search, ChevronRight, Loader2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { cn } from '@/lib/utils';
import { PageId } from '../components/Layout';
import { listContacts } from '@/api/contacts';
import { listTemplates } from '@/api/templates';
import { createCampaign } from '@/api/campaigns';
import type { Contact, Template } from '@/api/types';
import { ApiClientError } from '@/api/client';

interface CampaignBuilderPageProps {
  onNavigate: (page: PageId) => void;
}

export default function CampaignBuilderPage({ onNavigate }: CampaignBuilderPageProps) {
  const [step, setStep] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Template state
  const [templates, setTemplates] = useState<Template[]>([]);
  const [templatesLoading, setTemplatesLoading] = useState(true);
  const [templatesError, setTemplatesError] = useState<string | null>(null);
  const [templateSearch, setTemplateSearch] = useState('');
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);

  // Audience state
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [contactsLoading, setContactsLoading] = useState(true);
  const [contactsError, setContactsError] = useState<string | null>(null);
  const [audienceType, setAudienceType] = useState<'all' | 'manual'>('all');
  const [selectedContactIds, setSelectedContactIds] = useState<number[]>([]);
  const [audienceSearch, setAudienceSearch] = useState('');

  // Schedule state
  const [scheduleType, setScheduleType] = useState<'immediate' | 'scheduled'>('immediate');
  const [scheduleDate, setScheduleDate] = useState('');
  const [scheduleTime, setScheduleTime] = useState('');
  const [campaignName, setCampaignName] = useState('');

  useEffect(() => {
    const fetchTemplates = async () => {
      setTemplatesLoading(true);
      setTemplatesError(null);
      try {
        const response = await listTemplates({ status: 'APPROVED', limit: 200 });
        setTemplates(response.data);
      } catch (err) {
        setTemplatesError(err instanceof Error ? err.message : 'Failed to load templates');
      } finally {
        setTemplatesLoading(false);
      }
    };

    const fetchContacts = async () => {
      setContactsLoading(true);
      setContactsError(null);
      try {
        const response = await listContacts({ limit: 200 });
        setContacts(response.data);
      } catch (err) {
        setContactsError(err instanceof Error ? err.message : 'Failed to load contacts');
      } finally {
        setContactsLoading(false);
      }
    };

    fetchTemplates();
    fetchContacts();
  }, []);

  const filteredTemplates = useMemo(() => {
    const query = templateSearch.trim().toLowerCase();
    if (!query) return templates;
    return templates.filter((t) => t.name.toLowerCase().includes(query));
  }, [templates, templateSearch]);

  const filteredContacts = useMemo(() => {
    const query = audienceSearch.trim().toLowerCase();
    if (!query) return contacts;
    return contacts.filter((contact) =>
      contact.name.toLowerCase().includes(query) || contact.phone.includes(query)
    );
  }, [contacts, audienceSearch]);

  const recipientCount = audienceType === 'all' ? contacts.length : selectedContactIds.length;
  const selectedTemplate = templates.find(t => t.id === selectedTemplateId);

  const handleLaunch = async () => {
    if (!selectedTemplateId || !campaignName.trim()) return;

    setIsSubmitting(true);
    setSubmitError(null);

    try {
      await createCampaign({
        name: campaignName.trim(),
        templateId: selectedTemplateId,
        audience: {
          type: audienceType === 'manual' ? 'manual' : 'all',
          contactIds: audienceType === 'manual' ? selectedContactIds : undefined,
        },
        schedule: {
          type: scheduleType,
          date: scheduleDate || undefined,
          time: scheduleTime || undefined,
        },
      });
      onNavigate('campaign-sent');
    } catch (err) {
      const message = err instanceof ApiClientError ? err.message : 'Failed to create campaign';
      setSubmitError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const steps = [
    { id: 1, label: 'Template Selection', icon: FileText },
    { id: 2, label: 'Audience Selection', icon: Users },
    { id: 3, label: 'Schedule & Launch', icon: Calendar },
  ];

  return (
    <div className="space-y-12">
      <header>
        <Button
          variant="ghost"
          onClick={() => onNavigate('campaigns')}
          className="flex items-center gap-2 text-primary font-bold text-sm mb-4 hover:gap-3 transition-all p-0 h-auto"
        >
          <ArrowLeft size={16} />
          Back to Campaigns
        </Button>
        <div className="flex items-center gap-4 mb-2">
          <div className="h-12 w-12 bg-primary/10 rounded-2xl flex items-center justify-center text-primary">
            <Megaphone size={24} />
          </div>
          <h1 className="text-4xl font-extrabold tracking-tight">New Broadcast Campaign</h1>
        </div>
        <p className="text-muted-foreground text-lg max-w-2xl">
          Configure and launch your WhatsApp broadcast. Reach your audience with approved templates and precise scheduling.
        </p>
      </header>

      {submitError && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{submitError}</AlertDescription>
        </Alert>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {steps.map((s) => (
          <div
            key={s.id}
            className={cn(
              'flex items-center gap-4 p-4 rounded-2xl border-2 transition-all',
              step === s.id ? 'border-primary bg-primary/5 shadow-lg shadow-primary/5' : 'border-muted bg-muted/30 opacity-60'
            )}
          >
            <div
              className={cn(
                'h-10 w-10 rounded-xl flex items-center justify-center transition-colors',
                step === s.id ? 'bg-primary text-primary-foreground' : 'bg-muted-foreground/20 text-muted-foreground'
              )}
            >
              <s.icon size={20} />
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest opacity-60">Step 0{s.id}</p>
              <p className="font-bold text-sm">{s.label}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12">
        <div className="lg:col-span-8 space-y-8">
          {step === 1 && (
            <Card className="border-none shadow-sm rounded-3xl overflow-hidden">
              <CardHeader className="p-8 border-b bg-muted/10">
                <CardTitle className="text-2xl font-bold flex items-center gap-3">
                  <FileText className="text-primary" size={24} />
                  Select Approved Template
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8 space-y-6">
                {templatesError && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{templatesError}</AlertDescription>
                  </Alert>
                )}

                <div className="relative">
                  <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" size={18} />
                  <Input
                    placeholder="Search templates..."
                    value={templateSearch}
                    onChange={(e) => setTemplateSearch(e.target.value)}
                    className="pl-12 h-14 rounded-2xl bg-muted/30 border-none focus-visible:ring-primary/20"
                  />
                </div>

                {templatesLoading ? (
                  <div className="flex items-center justify-center py-12">
                    <Loader2 className="h-6 w-6 animate-spin text-primary" />
                  </div>
                ) : filteredTemplates.length === 0 ? (
                  <div className="text-center py-12 text-muted-foreground">
                    No approved templates found. Create and approve a template first.
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {filteredTemplates.map((template) => (
                      <div
                        key={template.id}
                        onClick={() => setSelectedTemplateId(template.id)}
                        className={cn(
                          "p-6 rounded-2xl border-2 cursor-pointer transition-all group bg-muted/10",
                          selectedTemplateId === template.id
                            ? "border-primary bg-primary/5"
                            : "border-muted hover:border-primary/40"
                        )}
                      >
                        <div className="flex justify-between items-start mb-4">
                          <Badge className="bg-emerald-100 text-emerald-700 hover:bg-emerald-100 uppercase text-[10px] font-bold">{template.status}</Badge>
                          <ChevronRight className={cn(
                            "transition-colors",
                            selectedTemplateId === template.id ? "text-primary" : "text-muted-foreground group-hover:text-primary"
                          )} size={18} />
                        </div>
                        <h3 className="font-bold text-lg mb-1">{template.name}</h3>
                        <p className="text-xs text-muted-foreground">{template.category} • {template.language}</p>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          {step === 2 && (
            <Card className="border-none shadow-sm rounded-3xl overflow-hidden">
              <CardHeader className="p-8 border-b bg-muted/10">
                <CardTitle className="text-2xl font-bold flex items-center gap-3">
                  <Users className="text-primary" size={24} />
                  Target Audience
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8 space-y-6">
                {contactsError && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{contactsError}</AlertDescription>
                  </Alert>
                )}

                <div className="space-y-4">
                  <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Audience Mode</Label>
                  <Select value={audienceType} onValueChange={(value) => setAudienceType(value as 'all' | 'manual')}>
                    <SelectTrigger className="h-14 rounded-2xl bg-muted/30 border-none focus:ring-primary/20">
                      <SelectValue placeholder="Choose audience mode..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Contacts ({contacts.length})</SelectItem>
                      <SelectItem value="manual">Manual Selection</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {audienceType === 'manual' && (
                  <div className="space-y-4">
                    <div className="relative">
                      <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground/60" size={16} />
                      <Input
                        value={audienceSearch}
                        onChange={(e) => setAudienceSearch(e.target.value)}
                        placeholder="Filter contacts by name or phone..."
                        className="pl-10 h-11 rounded-xl bg-muted/30 border-none"
                      />
                    </div>

                    <div className="border rounded-2xl bg-muted/10 max-h-72 overflow-y-auto divide-y">
                      {contactsLoading ? (
                        <div className="p-8 flex items-center justify-center">
                          <Loader2 className="h-6 w-6 animate-spin text-primary" />
                        </div>
                      ) : filteredContacts.length === 0 ? (
                        <div className="p-8 text-sm text-center text-muted-foreground">No contacts found.</div>
                      ) : (
                        filteredContacts.map((contact) => {
                          const checked = selectedContactIds.includes(contact.id);
                          return (
                            <label
                              key={contact.id}
                              className="flex items-center gap-3 p-3 cursor-pointer hover:bg-muted/30 transition-colors"
                            >
                              <input
                                type="checkbox"
                                checked={checked}
                                onChange={(e) => {
                                  if (e.target.checked) {
                                    setSelectedContactIds((prev) => [...prev, contact.id]);
                                  } else {
                                    setSelectedContactIds((prev) => prev.filter((id) => id !== contact.id));
                                  }
                                }}
                              />
                              <div className="flex-1">
                                <p className="font-semibold text-sm">{contact.name}</p>
                                <p className="text-xs text-muted-foreground font-mono">{contact.phone}</p>
                              </div>
                            </label>
                          );
                        })
                      )}
                    </div>
                  </div>
                )}

                <div className="p-6 rounded-2xl bg-primary/5 border-2 border-primary/10 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                      <Users size={20} />
                    </div>
                    <div>
                      <p className="font-bold">{recipientCount.toLocaleString()} Recipients</p>
                      <p className="text-xs text-muted-foreground">Estimated delivery: {Math.max(1, Math.ceil(recipientCount / 160))} minutes</p>
                    </div>
                  </div>
                  {audienceType === 'manual' && (
                    <Button variant="ghost" className="text-primary font-bold" onClick={() => setSelectedContactIds([])}>
                      Clear Selection
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          )}

          {step === 3 && (
            <Card className="border-none shadow-sm rounded-3xl overflow-hidden">
              <CardHeader className="p-8 border-b bg-muted/10">
                <CardTitle className="text-2xl font-bold flex items-center gap-3">
                  <Calendar className="text-primary" size={24} />
                  Campaign Schedule
                </CardTitle>
              </CardHeader>
              <CardContent className="p-8 space-y-8">
                <div className="space-y-4">
                  <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Campaign Name</Label>
                  <Input
                    value={campaignName}
                    onChange={(e) => setCampaignName(e.target.value)}
                    placeholder="e.g. Summer Flash Sale"
                    className="h-14 rounded-2xl bg-muted/30 border-none focus-visible:ring-primary/20"
                  />
                </div>

                <div className="space-y-4">
                  <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Launch Type</Label>
                  <Select value={scheduleType} onValueChange={(value) => setScheduleType(value as 'immediate' | 'scheduled')}>
                    <SelectTrigger className="h-14 rounded-2xl bg-muted/30 border-none focus:ring-primary/20">
                      <SelectValue placeholder="Choose launch type..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="immediate">Send Immediately</SelectItem>
                      <SelectItem value="scheduled">Schedule for Later</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {scheduleType === 'scheduled' && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-4">
                      <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Launch Date</Label>
                      <Input
                        type="date"
                        value={scheduleDate}
                        onChange={(e) => setScheduleDate(e.target.value)}
                        className="h-14 rounded-2xl bg-muted/30 border-none focus-visible:ring-primary/20"
                      />
                    </div>
                    <div className="space-y-4">
                      <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Launch Time</Label>
                      <Input
                        type="time"
                        value={scheduleTime}
                        onChange={(e) => setScheduleTime(e.target.value)}
                        className="h-14 rounded-2xl bg-muted/30 border-none focus-visible:ring-primary/20"
                      />
                    </div>
                  </div>
                )}

                <div className="p-8 rounded-3xl bg-muted/30 border-2 border-dashed border-muted-foreground/20 text-center space-y-4">
                  <div className="h-16 w-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto text-primary">
                    <CheckCircle2 size={32} />
                  </div>
                  <div className="space-y-2">
                    <h3 className="text-xl font-bold">Ready for Orchestration</h3>
                    <p className="text-sm text-muted-foreground max-w-md mx-auto">
                      Your campaign is configured and ready to be queued for delivery. Review the summary before launching.
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          <div className="flex items-center justify-between pt-8">
            <Button
              variant="ghost"
              disabled={step === 1}
              onClick={() => setStep(s => s - 1)}
              className="h-14 px-8 font-bold rounded-xl active:scale-95"
            >
              Previous Step
            </Button>
            {step < 3 ? (
              <Button
                onClick={() => setStep(s => s + 1)}
                disabled={step === 1 && !selectedTemplateId}
                className="h-14 px-10 font-bold rounded-xl shadow-xl shadow-primary/20 flex items-center gap-2 active:scale-95"
              >
                Next Step
                <ChevronRight size={18} />
              </Button>
            ) : (
              <Button
                onClick={handleLaunch}
                disabled={isSubmitting || !campaignName.trim() || (scheduleType === 'scheduled' && (!scheduleDate || !scheduleTime))}
                className="h-14 px-10 font-bold rounded-xl shadow-xl shadow-primary/20 flex items-center gap-2 active:scale-95 bg-emerald-600 hover:bg-emerald-700"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 size={18} className="animate-spin" />
                    Launching...
                  </>
                ) : (
                  <>
                    Launch Campaign
                    <Send size={18} />
                  </>
                )}
              </Button>
            )}
          </div>
        </div>

        <div className="lg:col-span-4">
          <Card className="border-none shadow-sm rounded-3xl overflow-hidden sticky top-24">
            <CardHeader className="p-6 border-b bg-muted/10">
              <CardTitle className="text-lg font-bold">Campaign Summary</CardTitle>
            </CardHeader>
            <CardContent className="p-6 space-y-6">
              <div className="space-y-4">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Template</span>
                  <span className="font-bold">{selectedTemplate?.name || 'Not selected'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Audience</span>
                  <span className="font-bold">{step > 1 ? (audienceType === 'all' ? 'All Contacts' : 'Manual Selection') : 'Not selected'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Recipients</span>
                  <span className="font-bold">{step > 1 ? recipientCount.toLocaleString() : '-'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Schedule</span>
                  <span className="font-bold">{step === 3 ? (scheduleType === 'immediate' ? 'Immediate' : `${scheduleDate} ${scheduleTime}`) : 'Not set'}</span>
                </div>
              </div>

              <div className="pt-6 border-t">
                <div className="bg-primary/5 p-4 rounded-xl space-y-2">
                  <p className="text-[10px] font-bold uppercase tracking-widest text-primary">Estimated Cost</p>
                  <p className="text-2xl font-extrabold">${(recipientCount * 0.005).toFixed(2)}</p>
                  <p className="text-[10px] text-muted-foreground leading-tight">Based on Meta API standard rates for North America.</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
