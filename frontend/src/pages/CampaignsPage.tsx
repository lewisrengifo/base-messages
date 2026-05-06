import React, { useEffect, useState, useCallback } from 'react';
import { TrendingUp, Verified, Clock, BarChart3, Megaphone, CheckCircle2, Calendar, XCircle, ArrowRight, MoreVertical, Eye, Copy, Trash2, Loader2, AlertCircle } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { listCampaigns, cancelCampaign, deleteCampaign, duplicateCampaign } from '@/api/campaigns';
import type { Campaign, CampaignStatus } from '@/api/types';
import { PageId } from '../components/Layout';
import { ApiClientError } from '@/api/client';

const STATUS_CONFIG: Record<CampaignStatus, { icon: React.ElementType; color: string; label: string }> = {
  draft: { icon: Calendar, color: 'bg-slate-100 text-slate-700', label: 'Draft' },
  scheduled: { icon: Calendar, color: 'bg-primary/10 text-primary', label: 'Scheduled' },
  sending: { icon: Loader2, color: 'bg-amber-100 text-amber-700', label: 'Sending' },
  sent: { icon: CheckCircle2, color: 'bg-emerald-100 text-emerald-700', label: 'Sent' },
  canceled: { icon: XCircle, color: 'bg-muted text-muted-foreground', label: 'Canceled' },
  failed: { icon: XCircle, color: 'bg-red-100 text-red-700', label: 'Failed' },
};

interface CampaignsPageProps {
  onNavigate: (page: PageId) => void;
}

export default function CampaignsPage({ onNavigate }: CampaignsPageProps) {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [total, setTotal] = useState(0);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const fetchCampaigns = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await listCampaigns({ page, limit: 20 });
      setCampaigns(response.data);
      setTotalPages(response.pagination.totalPages);
      setTotal(response.pagination.total);
    } catch (err) {
      const message = err instanceof ApiClientError ? err.message : 'Failed to load campaigns';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchCampaigns();
  }, [fetchCampaigns]);

  const handleCancel = async (id: string) => {
    setActionLoading(id);
    try {
      await cancelCampaign(id);
      await fetchCampaigns();
    } catch (err) {
      const message = err instanceof ApiClientError ? err.message : 'Failed to cancel campaign';
      setError(message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this campaign?')) return;
    setActionLoading(id);
    try {
      await deleteCampaign(id);
      await fetchCampaigns();
    } catch (err) {
      const message = err instanceof ApiClientError ? err.message : 'Failed to delete campaign';
      setError(message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleDuplicate = async (id: string) => {
    setActionLoading(id);
    try {
      await duplicateCampaign(id);
      await fetchCampaigns();
    } catch (err) {
      const message = err instanceof ApiClientError ? err.message : 'Failed to duplicate campaign';
      setError(message);
    } finally {
      setActionLoading(null);
    }
  };

  const sentCount = campaigns.filter(c => c.status === 'sent').length;
  const scheduledCount = campaigns.filter(c => c.status === 'scheduled').length;

  return (
    <div className="space-y-12">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div className="max-w-2xl">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-4 tracking-tight">Campaign History</h1>
          <p className="text-muted-foreground text-lg leading-relaxed">
            Track and manage your high-performance broadcasts. Review real-time delivery metrics and optimize your message orchestration.
          </p>
        </div>
        <Button
          onClick={() => onNavigate('campaign-builder')}
          className="h-14 px-8 rounded-xl font-bold shadow-lg shadow-primary/20 gap-2 hover:opacity-90 transition-all active:scale-95"
        >
          <Megaphone size={20} />
          New Campaign
        </Button>
      </header>

      {/* Stats Bento */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="p-8 rounded-2xl border-none shadow-sm bg-card">
          <CardHeader className="p-0 mb-1">
            <CardTitle className="technical-label">Total Sent</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <p className="text-4xl font-extrabold">{sentCount}</p>
            <div className="mt-4 flex items-center gap-2 text-xs font-bold text-secondary">
              <TrendingUp size={14} />
              Campaigns delivered
            </div>
          </CardContent>
        </Card>
        <Card className="p-8 rounded-2xl border-none shadow-sm bg-card">
          <CardHeader className="p-0 mb-1">
            <CardTitle className="technical-label">Average Open Rate</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <p className="text-4xl font-extrabold">--</p>
            <div className="mt-4 flex items-center gap-2 text-xs font-bold text-secondary">
              <Verified size={14} />
              Analytics coming soon
            </div>
          </CardContent>
        </Card>
        <Card className="p-8 rounded-2xl border-none shadow-sm bg-card">
          <CardHeader className="p-0 mb-1">
            <CardTitle className="technical-label">Active Scheduled</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <p className="text-4xl font-extrabold">{scheduledCount}</p>
            <div className="mt-4 flex items-center gap-2 text-xs font-bold text-muted-foreground">
              <Clock size={14} />
              Pending delivery
            </div>
          </CardContent>
        </Card>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Campaign Table */}
      <Card className="rounded-2xl overflow-hidden border-none shadow-sm bg-card">
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50 hover:bg-muted/50">
                <TableHead className="px-8 py-4 technical-label h-12">Campaign Name</TableHead>
                <TableHead className="px-8 py-4 technical-label h-12">Template Used</TableHead>
                <TableHead className="px-8 py-4 technical-label h-12">Scheduled Date</TableHead>
                <TableHead className="px-8 py-4 technical-label text-center h-12">Status</TableHead>
                <TableHead className="px-8 py-4 technical-label text-right h-12">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={5} className="px-8 py-12 text-center">
                    <Loader2 className="h-6 w-6 animate-spin text-primary mx-auto" />
                  </TableCell>
                </TableRow>
              ) : campaigns.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="px-8 py-12 text-center text-muted-foreground">
                    No campaigns found. Create your first campaign to get started.
                  </TableCell>
                </TableRow>
              ) : (
                campaigns.map((campaign) => {
                  const config = STATUS_CONFIG[campaign.status] || STATUS_CONFIG.draft;
                  const Icon = config.icon;
                  return (
                    <TableRow key={campaign.id} className="hover:bg-muted/20 transition-colors group border-b">
                      <TableCell className="px-8 py-5">
                        <div className="flex flex-col">
                          <span className="font-bold text-foreground">{campaign.name}</span>
                          <span className="text-xs text-muted-foreground font-medium">{campaign.id}</span>
                        </div>
                      </TableCell>
                      <TableCell className="px-8 py-5 text-sm font-medium text-muted-foreground">{campaign.templateName || '-'}</TableCell>
                      <TableCell className="px-8 py-5 text-sm text-muted-foreground">
                        {campaign.scheduledDate ? new Date(campaign.scheduledDate).toLocaleString() : '-'}
                      </TableCell>
                      <TableCell className="px-8 py-5">
                        <div className="flex justify-center">
                          <Badge variant="secondary" className={cn(
                            "px-3 py-1 rounded-full text-[10px] font-bold flex items-center gap-1.5 border-none",
                            config.color
                          )}>
                            <Icon size={12} />
                            {config.label}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell className="px-8 py-5 text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-primary">
                              <MoreVertical size={20} />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end" className="w-48 rounded-xl p-2">
                            <DropdownMenuItem
                              onClick={() => onNavigate('campaign-analytics')}
                              className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer"
                            >
                              <BarChart3 size={16} className="text-primary" />
                              <span className="font-bold text-xs">View Analytics</span>
                            </DropdownMenuItem>
                            <DropdownMenuItem className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer">
                              <Eye size={16} className="text-muted-foreground" />
                              <span className="font-bold text-xs">Preview Message</span>
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => handleDuplicate(campaign.id)}
                              className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer"
                              disabled={actionLoading === campaign.id}
                            >
                              <Copy size={16} className="text-muted-foreground" />
                              <span className="font-bold text-xs">Duplicate</span>
                            </DropdownMenuItem>
                            <div className="h-px bg-muted my-1" />
                            {(campaign.status === 'scheduled' || campaign.status === 'draft') && (
                              <DropdownMenuItem
                                onClick={() => handleCancel(campaign.id)}
                                className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer text-amber-600 focus:text-amber-600 focus:bg-amber-50"
                                disabled={actionLoading === campaign.id}
                              >
                                <XCircle size={16} />
                                <span className="font-bold text-xs">Cancel Campaign</span>
                              </DropdownMenuItem>
                            )}
                            <DropdownMenuItem
                              onClick={() => handleDelete(campaign.id)}
                              className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer text-destructive focus:text-destructive focus:bg-destructive/10"
                              disabled={actionLoading === campaign.id}
                            >
                              <Trash2 size={16} />
                              <span className="font-bold text-xs">Delete Record</span>
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </CardContent>
        {!loading && totalPages > 1 && (
          <div className="px-8 py-4 bg-muted/30 flex items-center justify-between text-sm">
            <span className="font-medium text-muted-foreground">Showing {campaigns.length} of {total} campaigns</span>
            <Pagination className="w-auto mx-0">
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    onClick={() => setPage(p => Math.max(1, p - 1))}
                    className="h-9 px-4 rounded-lg bg-background border border-muted-foreground/20 hover:bg-muted transition-colors cursor-pointer"
                  />
                </PaginationItem>
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => i + 1).map(p => (
                  <PaginationItem key={p}>
                    <PaginationLink
                      onClick={() => setPage(p)}
                      isActive={p === page}
                      className="h-9 w-9 rounded-lg bg-background border border-muted-foreground/20 hover:bg-muted transition-colors cursor-pointer"
                    >
                      {p}
                    </PaginationLink>
                  </PaginationItem>
                ))}
                <PaginationItem>
                  <PaginationNext
                    onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                    className="h-9 px-4 rounded-lg bg-background border border-muted-foreground/20 hover:bg-muted transition-colors cursor-pointer"
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}
      </Card>
    </div>
  );
}
