import React, { useState, useEffect, useCallback } from 'react';
import { Search, ShoppingBag, Tag, Lock, MessageSquare, Eye, Zap, MoreVertical, Edit3, Settings2, RefreshCw, Plus, BarChart3, AlertCircle, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { listTemplates, deleteTemplate } from '@/api/templates';
import type { Template, TemplateCategory, TemplateStatus } from '@/api/types';
import { PageId } from '../components/Layout';

const CATEGORY_ICONS: Record<string, React.ElementType> = {
  Marketing: Tag,
  Utility: ShoppingBag,
  Authentication: Lock,
  // Fallback for uppercase values from backend
  MARKETING: Tag,
  UTILITY: ShoppingBag,
  AUTHENTICATION: Lock,
};

const CATEGORY_COLORS: Record<string, string> = {
  Marketing: 'bg-amber-100 text-amber-700',
  Utility: 'bg-secondary-container text-secondary',
  Authentication: 'bg-primary-container/20 text-primary',
  // Fallback for uppercase values from backend
  MARKETING: 'bg-amber-100 text-amber-700',
  UTILITY: 'bg-secondary-container text-secondary',
  AUTHENTICATION: 'bg-primary-container/20 text-primary',
};

const STATUS_VARIANTS: Record<string, string> = {
  APPROVED: 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100',
  PENDING: 'bg-amber-100 text-amber-700 hover:bg-amber-100',
  REJECTED: 'bg-red-100 text-red-700 hover:bg-red-100',
  DRAFT: 'bg-slate-100 text-slate-700 hover:bg-slate-100',
};

interface TemplatesPageProps {
  onNavigate: (page: PageId) => void;
}

export default function TemplatesPage({ onNavigate }: TemplatesPageProps) {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<TemplateStatus | 'all'>('all');
  const [categoryFilter, setCategoryFilter] = useState<TemplateCategory | 'all'>('all');

  const fetchTemplates = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await listTemplates({
        status: statusFilter === 'all' ? undefined : statusFilter,
        category: categoryFilter === 'all' ? undefined : categoryFilter,
        search: searchQuery || undefined,
        limit: 50,
      });
      setTemplates(response.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load templates');
    } finally {
      setLoading(false);
    }
  }, [searchQuery, statusFilter, categoryFilter]);

  useEffect(() => {
    fetchTemplates();
  }, [fetchTemplates]);

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this template?')) return;
    try {
      await deleteTemplate(id);
      await fetchTemplates();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete template');
    }
  };

  const handleRefresh = () => {
    fetchTemplates();
  };

  return (
    <div className="space-y-12">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div className="max-w-2xl">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-4 tracking-tight">Template Management</h1>
          <p className="text-muted-foreground text-lg leading-relaxed">
            Streamline your WhatsApp communications. Craft, test, and deploy approved message templates for global engagement.
          </p>
        </div>
        <Button
          onClick={() => onNavigate('template-builder')}
          className="h-14 px-8 rounded-xl font-bold text-base shadow-xl shadow-primary/20 gap-3 active:scale-95 transition-all"
        >
          <Plus size={20} />
          New Template
        </Button>
      </header>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Filters */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-4">
        <div className="md:col-span-6 flex items-center bg-muted/50 rounded-xl px-4">
          <Search className="text-muted-foreground/60 mr-3" size={18} />
          <Input
            type="text"
            placeholder="Search templates by name or keyword..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="bg-transparent border-none focus-visible:ring-0 h-12 text-sm font-medium p-0"
          />
        </div>
        <div className="md:col-span-2">
          <Select value={statusFilter} onValueChange={(value) => setStatusFilter(value as TemplateStatus | 'all')}>
            <SelectTrigger className="h-12 bg-muted/50 border-none rounded-xl font-bold text-muted-foreground focus:ring-0">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="APPROVED">Approved</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="REJECTED">Rejected</SelectItem>
              <SelectItem value="DRAFT">Draft</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="md:col-span-2">
          <Select value={categoryFilter} onValueChange={(value) => setCategoryFilter(value as TemplateCategory | 'all')}>
            <SelectTrigger className="h-12 bg-muted/50 border-none rounded-xl font-bold text-muted-foreground focus:ring-0">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Categories</SelectItem>
              <SelectItem value="Marketing">Marketing</SelectItem>
              <SelectItem value="Utility">Utility</SelectItem>
              <SelectItem value="Authentication">Authentication</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="md:col-span-2">
          <Button
            variant="outline"
            onClick={handleRefresh}
            disabled={loading}
            className="h-12 w-full rounded-xl font-bold text-sm gap-2 border-muted-foreground/20 hover:bg-muted/30"
          >
            {loading ? <Loader2 size={18} className="animate-spin" /> : <RefreshCw size={18} />}
            Refresh
          </Button>
        </div>
      </div>

      {/* Templates Grid */}
      {loading ? (
        <div className="flex items-center justify-center min-h-[400px]">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {templates.map((template) => {
            const Icon = CATEGORY_ICONS[template.category] || MessageSquare;
            const iconColor = CATEGORY_COLORS[template.category] || 'bg-muted text-muted-foreground';
            const statusVariant = STATUS_VARIANTS[template.status] || '';

            return (
              <Card key={template.id} className="group border-none shadow-sm hover:shadow-2xl hover:shadow-primary/5 transition-all flex flex-col h-full relative overflow-hidden rounded-2xl">
                <CardHeader className="p-6 pb-4">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className={cn("h-10 w-10 rounded-xl flex items-center justify-center", iconColor)}>
                        <Icon size={20} />
                      </div>
                      <div>
                        <CardTitle className="font-bold text-foreground group-hover:text-primary transition-colors text-base">{template.name}</CardTitle>
                        <p className="technical-label">{template.category} • {template.language}</p>
                      </div>
                    </div>
                    <Badge
                      className={cn("rounded-full text-[10px] font-bold px-2.5 py-0.5", statusVariant)}
                    >
                      {template.status}
                    </Badge>
                  </div>
                </CardHeader>

                <CardContent className="p-6 pt-0 flex flex-col flex-grow">
                  <div className={cn(
                    "bg-muted/30 rounded-xl p-4 mb-6 flex-grow border-l-4",
                    template.status === 'REJECTED' ? "border-destructive/20" : "border-primary/20"
                  )}>
                    <p className="text-xs font-medium text-muted-foreground leading-relaxed italic line-clamp-4">
                      {template.content}
                    </p>
                  </div>

                  <div className="flex items-center justify-between pt-4 border-t">
                    <div className="flex items-center gap-4 text-muted-foreground">
                      {template.status === 'PENDING' ? (
                        <div className="flex items-center gap-1">
                          <RefreshCw size={12} className="animate-spin-slow" />
                          <span className="text-[11px] font-bold italic">Awaiting approval</span>
                        </div>
                      ) : template.status === 'REJECTED' ? (
                        <div className="flex items-center gap-2 text-destructive">
                          <AlertCircle size={14} />
                          <span className="text-[11px] font-bold">Policy violation</span>
                        </div>
                      ) : (
                        <>
                          <div className="flex items-center gap-1">
                            <Eye size={14} />
                            <span className="text-[11px] font-bold">-</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Zap size={14} />
                            <span className="text-[11px] font-bold">-</span>
                          </div>
                        </>
                      )}
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="text-primary hover:bg-primary/10">
                          {template.status === 'REJECTED' ? <RefreshCw size={18} /> :
                           template.status === 'PENDING' ? <Edit3 size={18} /> :
                           <MoreVertical size={18} />}
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => onNavigate('template-builder')}>
                          View Details
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => onNavigate('template-builder')}>
                          Edit Template
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleDelete(template.id)} className="text-destructive">
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </CardContent>
              </Card>
            );
          })}

          {/* Create New Card */}
          <Card
            onClick={() => onNavigate('template-builder')}
            className="group cursor-pointer bg-primary/5 rounded-2xl p-6 border-2 border-dashed border-primary/20 hover:border-primary/40 flex flex-col items-center justify-center gap-4 transition-all h-full"
          >
            <div className="h-14 w-14 rounded-full bg-background flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
              <Plus className="text-primary" size={28} />
            </div>
            <div className="text-center">
              <p className="font-headline font-extrabold text-primary">Create Template</p>
              <p className="technical-label mt-1">Unlimited Potential</p>
            </div>
          </Card>
        </div>
      )}

      {/* Performance Ledger - Static for now, will be populated in Phase 3 */}
      <section className="mt-20">
        <h3 className="text-lg font-bold mb-6 flex items-center gap-2">
          <BarChart3 className="text-primary" size={20} />
          Template Performance Ledger
        </h3>
        <Card className="overflow-hidden rounded-2xl border-none shadow-sm bg-muted/10">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/50 hover:bg-muted/50">
                <TableHead className="px-8 py-4 technical-label h-12">Template Name</TableHead>
                <TableHead className="px-8 py-4 technical-label h-12">Category</TableHead>
                <TableHead className="px-8 py-4 technical-label h-12">Open Rate</TableHead>
                <TableHead className="px-8 py-4 technical-label h-12">CTR</TableHead>
                <TableHead className="px-8 py-4 technical-label text-right h-12">Last Sent</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {templates.slice(0, 5).map((template) => (
                <TableRow key={template.id} className="hover:bg-muted/20 transition-colors border-b">
                  <TableCell className="px-8 py-5 font-bold text-sm">{template.name}</TableCell>
                  <TableCell className="px-8 py-5 text-sm text-muted-foreground">{template.category}</TableCell>
                  <TableCell className="px-8 py-5 text-sm font-mono">-</TableCell>
                  <TableCell className="px-8 py-5 text-sm font-mono">-</TableCell>
                  <TableCell className="px-8 py-5 text-sm text-right font-medium text-muted-foreground">Never</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      </section>
    </div>
  );
}
