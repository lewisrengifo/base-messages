import React, { useState } from 'react';
import { ArrowLeft, Megaphone, Users, FileText, Calendar, Send, CheckCircle2, Search, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { PageId } from '../components/Layout';

export default function CampaignBuilderPage({ onNavigate }: { onNavigate: (page: PageId) => void }) {
  const [step, setStep] = useState(1);

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

      {/* Progress Stepper */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {steps.map((s) => (
          <div 
            key={s.id}
            className={cn(
              "flex items-center gap-4 p-4 rounded-2xl border-2 transition-all",
              step === s.id ? "border-primary bg-primary/5 shadow-lg shadow-primary/5" : "border-muted bg-muted/30 opacity-60"
            )}
          >
            <div className={cn(
              "h-10 w-10 rounded-xl flex items-center justify-center transition-colors",
              step === s.id ? "bg-primary text-primary-foreground" : "bg-muted-foreground/20 text-muted-foreground"
            )}>
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
                <div className="relative">
                  <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground" size={18} />
                  <Input 
                    placeholder="Search templates..." 
                    className="pl-12 h-14 rounded-2xl bg-muted/30 border-none focus-visible:ring-primary/20"
                  />
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {['Order Confirmation', 'Marketing Sale', '2FA Login'].map((t) => (
                    <div 
                      key={t}
                      className="p-6 rounded-2xl border-2 border-muted hover:border-primary/40 cursor-pointer transition-all group bg-muted/10"
                    >
                      <div className="flex justify-between items-start mb-4">
                        <Badge className="bg-emerald-100 text-emerald-700 hover:bg-emerald-100 uppercase text-[10px] font-bold">Approved</Badge>
                        <ChevronRight className="text-muted-foreground group-hover:text-primary transition-colors" size={18} />
                      </div>
                      <h3 className="font-bold text-lg mb-1">{t}</h3>
                      <p className="text-xs text-muted-foreground">Utility • EN_US</p>
                    </div>
                  ))}
                </div>
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
                <div className="space-y-4">
                  <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Select Contact Group</Label>
                  <Select>
                    <SelectTrigger className="h-14 rounded-2xl bg-muted/30 border-none focus:ring-primary/20">
                      <SelectValue placeholder="Choose a segment..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Contacts (2,450)</SelectItem>
                      <SelectItem value="vip">VIP Customers (124)</SelectItem>
                      <SelectItem value="new">New Customers (450)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="p-6 rounded-2xl bg-primary/5 border-2 border-primary/10 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                      <Users size={20} />
                    </div>
                    <div>
                      <p className="font-bold">2,450 Recipients</p>
                      <p className="text-xs text-muted-foreground">Estimated delivery: 15 minutes</p>
                    </div>
                  </div>
                  <Button variant="ghost" className="text-primary font-bold">Edit Segment</Button>
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
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Launch Date</Label>
                    <Input type="date" className="h-14 rounded-2xl bg-muted/30 border-none focus-visible:ring-primary/20" />
                  </div>
                  <div className="space-y-4">
                    <Label className="text-sm font-bold uppercase tracking-widest opacity-60">Launch Time</Label>
                    <Input type="time" className="h-14 rounded-2xl bg-muted/30 border-none focus-visible:ring-primary/20" />
                  </div>
                </div>

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
                className="h-14 px-10 font-bold rounded-xl shadow-xl shadow-primary/20 flex items-center gap-2 active:scale-95"
              >
                Next Step
                <ChevronRight size={18} />
              </Button>
            ) : (
              <Button 
                onClick={() => onNavigate('campaign-sent')}
                className="h-14 px-10 font-bold rounded-xl shadow-xl shadow-primary/20 flex items-center gap-2 active:scale-95 bg-emerald-600 hover:bg-emerald-700"
              >
                Launch Campaign
                <Send size={18} />
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
                  <span className="font-bold">{step > 1 ? 'Order Confirmation' : 'Not selected'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Audience</span>
                  <span className="font-bold">{step > 2 ? 'All Contacts' : 'Not selected'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Recipients</span>
                  <span className="font-bold">{step > 2 ? '2,450' : '-'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Schedule</span>
                  <span className="font-bold">{step === 3 ? 'Immediate' : 'Not set'}</span>
                </div>
              </div>
              
              <div className="pt-6 border-t">
                <div className="bg-primary/5 p-4 rounded-xl space-y-2">
                  <p className="text-[10px] font-bold uppercase tracking-widest text-primary">Estimated Cost</p>
                  <p className="text-2xl font-extrabold">$12.25</p>
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
