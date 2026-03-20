import React from 'react';
import { Search, ShoppingBag, Tag, Lock, MessageSquare, Eye, Zap, MoreVertical, Edit3, Settings2, RefreshCw, Plus, BarChart3, AlertCircle } from 'lucide-react';
import { cn } from '@/src/lib/utils';
import { Button } from '@/src/components/ui/button';
import { Input } from '@/src/components/ui/input';
import { Badge } from '@/src/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/src/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/src/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/src/components/ui/select';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/src/components/ui/dropdown-menu';

const TEMPLATES = [
  {
    id: 1,
    name: 'Order Confirmation',
    category: 'Utility',
    lang: 'EN_US',
    status: 'APPROVED',
    content: '"Hello {{1}}, thank you for your order #{{2}}. We are preparing it for shipment and will notify you soon!"',
    views: '12.4k',
    conversion: '98%',
    icon: ShoppingBag,
    iconColor: 'bg-secondary-container text-secondary',
  },
  {
    id: 2,
    name: 'Marketing Sale',
    category: 'Marketing',
    lang: 'MULTI',
    status: 'PENDING',
    content: '"Flash Sale! Use code {{1}} to get 40% OFF on all sapphire collections. Valid until {{2}}."',
    views: '-',
    conversion: '-',
    icon: Tag,
    iconColor: 'bg-amber-100 text-amber-700',
    time: 'Submitted 2h ago'
  },
  {
    id: 3,
    name: '2FA Login',
    category: 'Authentication',
    lang: 'GLOBAL',
    status: 'APPROVED',
    content: '"{{1}} is your verification code. For security, do not share this code with anyone."',
    views: '145k',
    conversion: '100%',
    icon: Lock,
    iconColor: 'bg-primary-container/20 text-primary',
  },
  {
    id: 4,
    name: 'Product Feedback',
    category: 'Utility',
    lang: 'EN_GB',
    status: 'REJECTED',
    content: '"How was your experience today? Rate us here {{1}} and get a special coupon."',
    views: '-',
    conversion: '-',
    icon: MessageSquare,
    iconColor: 'bg-error/10 text-error',
    error: 'Policy violation detected'
  },
];

const PERFORMANCE_DATA = [
  { name: 'Order Confirmation', category: 'Utility', openRate: '92.4%', ctr: '14.1%', lastSent: 'May 24, 2024' },
  { name: 'Welcome Onboard', category: 'Marketing', openRate: '88.2%', ctr: '22.5%', lastSent: 'May 23, 2024' },
  { name: 'Password Reset', category: 'Auth', openRate: '99.1%', ctr: '0.0%', lastSent: 'May 24, 2024' },
];

import { PageId } from '../components/Layout';

export default function TemplatesPage({ onNavigate }: { onNavigate: (page: PageId) => void }) {
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

      {/* Filters */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-4">
        <div className="md:col-span-6 flex items-center bg-muted/50 rounded-xl px-4">
          <Search className="text-muted-foreground/60 mr-3" size={18} />
          <Input 
            type="text" 
            placeholder="Search templates by name or keyword..."
            className="bg-transparent border-none focus-visible:ring-0 h-12 text-sm font-medium p-0"
          />
        </div>
        <div className="md:col-span-2">
          <Select defaultValue="all">
            <SelectTrigger className="h-12 bg-muted/50 border-none rounded-xl font-bold text-muted-foreground focus:ring-0">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="approved">Approved</SelectItem>
              <SelectItem value="pending">Pending</SelectItem>
              <SelectItem value="rejected">Rejected</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="md:col-span-2">
          <Select defaultValue="all">
            <SelectTrigger className="h-12 bg-muted/50 border-none rounded-xl font-bold text-muted-foreground focus:ring-0">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Category</SelectItem>
              <SelectItem value="marketing">Marketing</SelectItem>
              <SelectItem value="utility">Utility</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="md:col-span-2">
          <Button variant="outline" className="h-12 w-full rounded-xl font-bold text-sm gap-2 border-muted-foreground/20 hover:bg-muted/30">
            <Settings2 size={18} />
            More Filters
          </Button>
        </div>
      </div>

      {/* Templates Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {TEMPLATES.map((template) => (
          <Card key={template.id} className="group border-none shadow-sm hover:shadow-2xl hover:shadow-primary/5 transition-all flex flex-col h-full relative overflow-hidden rounded-2xl">
            <CardHeader className="p-6 pb-4">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className={cn("h-10 w-10 rounded-xl flex items-center justify-center", template.iconColor)}>
                    <template.icon size={20} />
                  </div>
                  <div>
                    <CardTitle className="font-bold text-foreground group-hover:text-primary transition-colors text-base">{template.name}</CardTitle>
                    <p className="technical-label">{template.category} • {template.lang}</p>
                  </div>
                </div>
                <Badge variant={
                  template.status === 'APPROVED' ? "default" :
                  template.status === 'PENDING' ? "secondary" :
                  "destructive"
                } className={cn(
                  "rounded-full text-[10px] font-bold px-2.5 py-0.5",
                  template.status === 'APPROVED' && "bg-emerald-100 text-emerald-700 hover:bg-emerald-100",
                  template.status === 'PENDING' && "bg-amber-100 text-amber-700 hover:bg-amber-100"
                )}>
                  {template.status}
                </Badge>
              </div>
            </CardHeader>
            
            <CardContent className="p-6 pt-0 flex flex-col flex-grow">
              <div className={cn(
                "bg-muted/30 rounded-xl p-4 mb-6 flex-grow border-l-4",
                template.status === 'REJECTED' ? "border-destructive/20" : "border-primary/20"
              )}>
                <p className="text-xs font-medium text-muted-foreground leading-relaxed italic">
                  {template.content}
                </p>
              </div>

              <div className="flex items-center justify-between pt-4 border-t">
                {template.error ? (
                  <div className="flex items-center gap-2 text-destructive">
                    <AlertCircle size={14} />
                    <span className="text-[11px] font-bold">{template.error}</span>
                  </div>
                ) : (
                  <div className="flex items-center gap-4 text-muted-foreground">
                    {template.time ? (
                      <div className="flex items-center gap-1">
                        <RefreshCw size={12} className="animate-spin-slow" />
                        <span className="text-[11px] font-bold italic">{template.time}</span>
                      </div>
                    ) : (
                      <>
                        <div className="flex items-center gap-1">
                          <Eye size={14} />
                          <span className="text-[11px] font-bold">{template.views}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Zap size={14} />
                          <span className="text-[11px] font-bold">{template.conversion}</span>
                        </div>
                      </>
                    )}
                  </div>
                )}
                <DropdownMenu>
                  <DropdownMenuTrigger render={
                    <Button variant="ghost" size="icon" className="text-primary hover:bg-primary/10">
                      {template.status === 'REJECTED' ? <RefreshCw size={18} /> : 
                       template.status === 'PENDING' ? <Edit3 size={18} /> : 
                       <MoreVertical size={18} />}
                    </Button>
                  } />
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem>View Details</DropdownMenuItem>
                    <DropdownMenuItem>Edit Template</DropdownMenuItem>
                    <DropdownMenuItem>Duplicate</DropdownMenuItem>
                    <DropdownMenuItem className="text-destructive">Archive</DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </CardContent>
          </Card>
        ))}

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

      {/* Performance Ledger */}
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
              {PERFORMANCE_DATA.map((row, i) => (
                <TableRow key={i} className="hover:bg-muted/20 transition-colors border-b">
                  <TableCell className="px-8 py-5 font-bold text-sm">{row.name}</TableCell>
                  <TableCell className="px-8 py-5 text-sm text-muted-foreground">{row.category}</TableCell>
                  <TableCell className="px-8 py-5 text-sm font-mono">{row.openRate}</TableCell>
                  <TableCell className="px-8 py-5 text-sm font-mono">{row.ctr}</TableCell>
                  <TableCell className="px-8 py-5 text-sm text-right font-medium text-muted-foreground">{row.lastSent}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      </section>
    </div>
  );
}
