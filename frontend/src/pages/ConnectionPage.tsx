import React from 'react';
import { Smartphone, BadgeCheck, Key, Rocket, AlertCircle, ExternalLink, Zap, ShieldCheck } from 'lucide-react';
import { Button } from '@/src/components/ui/button';
import { Input } from '@/src/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/src/components/ui/card';
import { Textarea } from '@/src/components/ui/textarea';
import { Badge } from '@/src/components/ui/badge';
import { Label } from '@/src/components/ui/label';

export default function ConnectionPage() {
  return (
    <div className="space-y-12">
      <header className="max-w-2xl">
        <h1 className="text-4xl md:text-5xl font-extrabold mb-4 tracking-tight">Connect API</h1>
        <p className="text-muted-foreground text-lg leading-relaxed">
          Integrate your WhatsApp Business Platform credentials to enable automated messaging and campaign management.
        </p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Connection Form */}
        <Card className="lg:col-span-7 border-none shadow-sm bg-card">
          <CardHeader className="pb-8">
            <CardTitle className="text-xl font-bold">WhatsApp API Credentials</CardTitle>
            <CardDescription>Enter your Meta Business Platform identifiers below.</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-8" onSubmit={(e) => e.preventDefault()}>
              <div className="space-y-3">
                <Label htmlFor="phone-id" className="technical-label">Phone Number ID</Label>
                <div className="relative group">
                  <Input 
                    id="phone-id"
                    type="text" 
                    placeholder="e.g. 104829375028475"
                    className="bg-muted/50 border-none h-14 px-4 rounded-xl focus-visible:ring-primary"
                  />
                  <Smartphone className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground/40" size={20} />
                </div>
              </div>

              <div className="space-y-3">
                <Label htmlFor="waba-id" className="technical-label">WhatsApp Business Account ID</Label>
                <div className="relative group">
                  <Input 
                    id="waba-id"
                    type="text" 
                    placeholder="e.g. 948273615204"
                    className="bg-muted/50 border-none h-14 px-4 rounded-xl focus-visible:ring-primary"
                  />
                  <BadgeCheck className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground/40" size={20} />
                </div>
              </div>

              <div className="space-y-3">
                <Label htmlFor="token" className="technical-label">Permanent Access Token</Label>
                <div className="relative group">
                  <Textarea 
                    id="token"
                    placeholder="Enter your system user access token..."
                    rows={3}
                    className="bg-muted/50 border-none px-4 py-4 rounded-xl focus-visible:ring-primary resize-none min-h-[80px]"
                  />
                  <Key className="absolute right-4 top-6 text-muted-foreground/40" size={20} />
                </div>
              </div>

              <div className="pt-4">
                <Button className="w-full md:w-auto h-14 px-10 font-bold text-sm rounded-xl shadow-lg shadow-primary/20 active:scale-95 transition-all gap-2">
                  Verify & Connect
                  <Rocket size={18} />
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Status & Info */}
        <div className="lg:col-span-5 space-y-6">
          {/* Status Card */}
          <Card className="border-l-4 border-l-amber-400 bg-muted/30">
            <CardHeader className="flex flex-row items-start justify-between pb-4">
              <CardTitle className="text-base font-bold">Current Status</CardTitle>
              <Badge variant="outline" className="bg-amber-100 text-amber-800 border-amber-200 text-[10px] font-bold uppercase tracking-tighter">Inactive</Badge>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-4 mb-6">
                <div className="w-12 h-12 rounded-full bg-background flex items-center justify-center shadow-sm">
                  <AlertCircle className="text-amber-500" size={24} />
                </div>
                <div>
                  <p className="text-lg font-bold">Awaiting Configuration</p>
                  <p className="text-sm text-muted-foreground">Meta Graph API not linked</p>
                </div>
              </div>
              <div className="space-y-3 pt-4 border-t">
                <div className="flex items-center justify-between text-xs font-medium">
                  <span className="text-muted-foreground">Last Heartbeat</span>
                  <span className="text-foreground">Never</span>
                </div>
                <div className="flex items-center justify-between text-xs font-medium">
                  <span className="text-muted-foreground">Endpoint Connectivity</span>
                  <span className="text-destructive font-bold">Failed</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Guidance Card */}
          <Card className="bg-primary/5 border-primary/10 relative overflow-hidden group">
            <CardContent className="pt-8 relative z-10">
              <h3 className="font-bold text-primary mb-3">Where to find these?</h3>
              <p className="text-sm text-muted-foreground mb-6 leading-relaxed">
                You can find your credentials in the <span className="font-bold">WhatsApp Settings</span> section of your Meta App Dashboard.
              </p>
              <Button variant="link" className="p-0 h-auto text-primary font-bold text-sm gap-2 hover:no-underline">
                View Integration Guide
                <ExternalLink size={14} />
              </Button>
            </CardContent>
            <div className="absolute -right-8 -bottom-8 w-32 h-32 bg-primary/10 rounded-full blur-3xl group-hover:scale-110 transition-transform duration-500" />
          </Card>

          {/* Benefits Grid */}
          <div className="grid grid-cols-2 gap-4">
            <Card className="bg-muted/50 border-none">
              <CardContent className="p-4 flex flex-col gap-2">
                <Zap className="text-primary" size={20} />
                <p className="technical-label">Speed</p>
                <p className="text-xs font-semibold">Real-time webhooks</p>
              </CardContent>
            </Card>
            <Card className="bg-muted/50 border-none">
              <CardContent className="p-4 flex flex-col gap-2">
                <ShieldCheck className="text-primary" size={20} />
                <p className="technical-label">Security</p>
                <p className="text-xs font-semibold">Encrypted tokens</p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
