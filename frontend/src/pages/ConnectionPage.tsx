import React, { useState, useEffect } from 'react';
import { Smartphone, BadgeCheck, Key, Rocket, AlertCircle, ExternalLink, Zap, ShieldCheck, Loader2, CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { getConnectionStatus, saveConnection, testConnection, deleteConnection } from '@/api/connection';
import type { ConnectionStatus as ConnectionStatusType } from '@/api/types';

export default function ConnectionPage() {
  const [formData, setFormData] = useState({
    phoneNumberId: '',
    wabaId: '',
    accessToken: '',
  });
  const [status, setStatus] = useState<ConnectionStatusType | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [testing, setTesting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    fetchStatus();
  }, []);

  const fetchStatus = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getConnectionStatus();
      setStatus(response);
      // Pre-fill form if we have existing connection data
      if (response.phoneNumberId) {
        setFormData(prev => ({
          ...prev,
          phoneNumberId: response.phoneNumberId || '',
          wabaId: response.wabaId || '',
        }));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load connection status');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await saveConnection({
        phoneNumberId: formData.phoneNumberId,
        wabaId: formData.wabaId,
        accessToken: formData.accessToken,
      });
      setStatus(response);
      setSuccess('Connection saved successfully!');
      // Clear the access token from the form for security
      setFormData(prev => ({ ...prev, accessToken: '' }));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save connection');
    } finally {
      setSaving(false);
    }
  };

  const handleTest = async () => {
    setTesting(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await testConnection();
      if (response.success) {
        setSuccess(`Connection test successful! Latency: ${response.latency}ms`);
      } else {
        setError(`Connection test failed: ${response.message}`);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to test connection');
    } finally {
      setTesting(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to remove this connection? This will disconnect your WhatsApp Business API.')) {
      return;
    }

    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      await deleteConnection();
      setStatus(null);
      setFormData({ phoneNumberId: '', wabaId: '', accessToken: '' });
      setSuccess('Connection removed successfully!');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to remove connection');
    } finally {
      setSaving(false);
    }
  };

  const isActive = status?.status === 'ACTIVE';
  const isConnected = status?.endpointConnectivity === 'CONNECTED';

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-12">
      <header className="max-w-2xl">
        <h1 className="text-4xl md:text-5xl font-extrabold mb-4 tracking-tight">Connect API</h1>
        <p className="text-muted-foreground text-lg leading-relaxed">
          Integrate your WhatsApp Business Platform credentials to enable automated messaging and campaign management.
        </p>
      </header>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {success && (
        <Alert className="bg-emerald-50 text-emerald-800 border-emerald-200">
          <CheckCircle2 className="h-4 w-4" />
          <AlertDescription>{success}</AlertDescription>
        </Alert>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Connection Form */}
        <Card className="lg:col-span-7 border-none shadow-sm bg-card">
          <CardHeader className="pb-8">
            <CardTitle className="text-xl font-bold">WhatsApp API Credentials</CardTitle>
            <CardDescription>Enter your Meta Business Platform identifiers below.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-8">
              <div className="space-y-3">
                <Label htmlFor="phone-id" className="technical-label">Phone Number ID</Label>
                <div className="relative group">
                  <Input
                    id="phone-id"
                    type="text"
                    value={formData.phoneNumberId}
                    onChange={(e) => setFormData({ ...formData, phoneNumberId: e.target.value })}
                    placeholder="e.g. 104829375028475"
                    className="bg-muted/50 border-none h-14 px-4 rounded-xl focus-visible:ring-primary"
                    required
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
                    value={formData.wabaId}
                    onChange={(e) => setFormData({ ...formData, wabaId: e.target.value })}
                    placeholder="e.g. 948273615204"
                    className="bg-muted/50 border-none h-14 px-4 rounded-xl focus-visible:ring-primary"
                    required
                  />
                  <BadgeCheck className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground/40" size={20} />
                </div>
              </div>

              <div className="space-y-3">
                <Label htmlFor="token" className="technical-label">Permanent Access Token</Label>
                <div className="relative group">
                  <Textarea
                    id="token"
                    value={formData.accessToken}
                    onChange={(e) => setFormData({ ...formData, accessToken: e.target.value })}
                    placeholder="Enter your system user access token..."
                    rows={3}
                    className="bg-muted/50 border-none px-4 py-4 rounded-xl focus-visible:ring-primary resize-none min-h-[80px]"
                    required={!isActive}
                  />
                  <Key className="absolute right-4 top-6 text-muted-foreground/40" size={20} />
                </div>
                {isActive && (
                  <p className="text-xs text-muted-foreground">
                    Leave blank to keep existing token. Enter new token to update.
                  </p>
                )}
              </div>

              <div className="pt-4 flex gap-4">
                <Button
                  type="submit"
                  disabled={saving}
                  className="flex-1 h-14 px-10 font-bold text-sm rounded-xl shadow-lg shadow-primary/20 active:scale-95 transition-all gap-2"
                >
                  {saving ? (
                    <>
                      <Loader2 size={18} className="animate-spin" />
                      Saving...
                    </>
                  ) : (
                    <>
                      {isActive ? 'Update Connection' : 'Verify & Connect'}
                      <Rocket size={18} />
                    </>
                  )}
                </Button>

                {isActive && (
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleTest}
                    disabled={testing}
                    className="h-14 px-6 font-bold text-sm rounded-xl border-muted-foreground/20"
                  >
                    {testing ? (
                      <Loader2 size={18} className="animate-spin" />
                    ) : (
                      'Test'
                    )}
                  </Button>
                )}
              </div>

              {isActive && (
                <Button
                  type="button"
                  variant="ghost"
                  onClick={handleDelete}
                  disabled={saving}
                  className="w-full text-destructive hover:text-destructive hover:bg-destructive/10"
                >
                  Remove Connection
                </Button>
              )}
            </form>
          </CardContent>
        </Card>

        {/* Status & Info */}
        <div className="lg:col-span-5 space-y-6">
          {/* Status Card */}
          <Card className={cn(
            "border-l-4",
            isActive ? "border-l-emerald-400 bg-emerald-50/30" : "border-l-amber-400 bg-muted/30"
          )}>
            <CardHeader className="flex flex-row items-start justify-between pb-4">
              <CardTitle className="text-base font-bold">Current Status</CardTitle>
              <Badge
                variant="outline"
                className={cn(
                  "text-[10px] font-bold uppercase tracking-tighter",
                  isActive
                    ? "bg-emerald-100 text-emerald-800 border-emerald-200"
                    : "bg-amber-100 text-amber-800 border-amber-200"
                )}
              >
                {isActive ? 'Active' : 'Inactive'}
              </Badge>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-4 mb-6">
                <div className={cn(
                  "w-12 h-12 rounded-full flex items-center justify-center shadow-sm",
                  isActive ? "bg-emerald-100" : "bg-background"
                )}>
                  {isActive ? (
                    <CheckCircle2 className="text-emerald-500" size={24} />
                  ) : (
                    <AlertCircle className="text-amber-500" size={24} />
                  )}
                </div>
                <div>
                  <p className="text-lg font-bold">
                    {isActive ? 'Connected' : 'Awaiting Configuration'}
                  </p>
                  <p className="text-sm text-muted-foreground">
                    {isActive ? 'Meta Graph API linked' : 'Meta Graph API not linked'}
                  </p>
                </div>
              </div>
              <div className="space-y-3 pt-4 border-t">
                <div className="flex items-center justify-between text-xs font-medium">
                  <span className="text-muted-foreground">Last Heartbeat</span>
                  <span className="text-foreground">
                    {status?.lastHeartbeat
                      ? new Date(status.lastHeartbeat).toLocaleString()
                      : 'Never'}
                  </span>
                </div>
                <div className="flex items-center justify-between text-xs font-medium">
                  <span className="text-muted-foreground">Endpoint Connectivity</span>
                  <span className={cn(
                    "font-bold",
                    isConnected ? "text-emerald-600" : "text-destructive"
                  )}>
                    {isConnected ? 'Connected' : 'Failed'}
                  </span>
                </div>
                {status?.phoneNumberId && (
                  <div className="flex items-center justify-between text-xs font-medium">
                    <span className="text-muted-foreground">Phone Number ID</span>
                    <span className="text-foreground font-mono">{status.phoneNumberId}</span>
                  </div>
                )}
                {status?.wabaId && (
                  <div className="flex items-center justify-between text-xs font-medium">
                    <span className="text-muted-foreground">WABA ID</span>
                    <span className="text-foreground font-mono">{status.wabaId}</span>
                  </div>
                )}
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

// Helper function for className concatenation
function cn(...classes: (string | boolean | undefined)[]) {
  return classes.filter(Boolean).join(' ');
}
