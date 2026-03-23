import React from 'react';
import { TrendingUp, Verified, Clock, BarChart3, Megaphone, CheckCircle2, Calendar, XCircle, ArrowRight, MoreVertical, Edit, Copy, Trash2, Eye } from 'lucide-react';
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

const CAMPAIGNS = [
  { id: 'CAMP-9283', name: 'Q3 Product Reveal', template: 'Modern_Dark_Theme_V2', date: 'Oct 12, 2023 • 09:00 AM', status: 'Sent', icon: CheckCircle2, color: 'bg-emerald-100 text-emerald-700' },
  { id: 'CAMP-9455', name: 'Summer Flash Sale', template: 'Discount_Hero_Grid', date: 'Oct 24, 2023 • 02:30 PM', status: 'Scheduled', icon: Calendar, color: 'bg-primary/10 text-primary' },
  { id: 'CAMP-8112', name: 'System Maintenance Alert', template: 'Utility_Simple_Text', date: 'Oct 05, 2023 • 11:15 AM', status: 'Canceled', icon: XCircle, color: 'bg-muted text-muted-foreground' },
  { id: 'CAMP-9002', name: 'Monthly Newsletter #42', template: 'Editorial_Layout_Rich', date: 'Sep 28, 2023 • 10:00 AM', status: 'Sent', icon: CheckCircle2, color: 'bg-emerald-100 text-emerald-700' },
];

import { PageId } from '../components/Layout';

export default function CampaignsPage({ onNavigate }: { onNavigate: (page: PageId) => void }) {
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
            <p className="text-4xl font-extrabold">124,802</p>
            <div className="mt-4 flex items-center gap-2 text-xs font-bold text-secondary">
              <TrendingUp size={14} />
              12% increase this month
            </div>
          </CardContent>
        </Card>
        <Card className="p-8 rounded-2xl border-none shadow-sm bg-card">
          <CardHeader className="p-0 mb-1">
            <CardTitle className="technical-label">Average Open Rate</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <p className="text-4xl font-extrabold">64.2%</p>
            <div className="mt-4 flex items-center gap-2 text-xs font-bold text-secondary">
              <Verified size={14} />
              Exceeding industry benchmark
            </div>
          </CardContent>
        </Card>
        <Card className="p-8 rounded-2xl border-none shadow-sm bg-card">
          <CardHeader className="p-0 mb-1">
            <CardTitle className="technical-label">Active Scheduled</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <p className="text-4xl font-extrabold">14</p>
            <div className="mt-4 flex items-center gap-2 text-xs font-bold text-muted-foreground">
              <Clock size={14} />
              Next: Summer Launch (2h)
            </div>
          </CardContent>
        </Card>
      </div>

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
              {CAMPAIGNS.map((campaign) => (
                <TableRow key={campaign.id} className="hover:bg-muted/20 transition-colors group border-b">
                  <TableCell className="px-8 py-5">
                    <div className="flex flex-col">
                      <span className="font-bold text-foreground">{campaign.name}</span>
                      <span className="text-xs text-muted-foreground font-medium">{campaign.id}</span>
                    </div>
                  </TableCell>
                  <TableCell className="px-8 py-5 text-sm font-medium text-muted-foreground">{campaign.template}</TableCell>
                  <TableCell className="px-8 py-5 text-sm text-muted-foreground">{campaign.date}</TableCell>
                  <TableCell className="px-8 py-5">
                    <div className="flex justify-center">
                      <Badge variant="secondary" className={cn(
                        "px-3 py-1 rounded-full text-[10px] font-bold flex items-center gap-1.5 border-none",
                        campaign.color
                      )}>
                        <campaign.icon size={12} />
                        {campaign.status}
                      </Badge>
                    </div>
                  </TableCell>
                  <TableCell className="px-8 py-5 text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger render={
                        <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-primary">
                          <MoreVertical size={20} />
                        </Button>
                      } />
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
                        {campaign.status === 'Scheduled' && (
                          <DropdownMenuItem className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer">
                            <Edit size={16} className="text-muted-foreground" />
                            <span className="font-bold text-xs">Edit Schedule</span>
                          </DropdownMenuItem>
                        )}
                        <DropdownMenuItem className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer">
                          <Copy size={16} className="text-muted-foreground" />
                          <span className="font-bold text-xs">Duplicate</span>
                        </DropdownMenuItem>
                        <div className="h-px bg-muted my-1" />
                        {campaign.status === 'Scheduled' && (
                          <DropdownMenuItem className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer text-amber-600 focus:text-amber-600 focus:bg-amber-50">
                            <XCircle size={16} />
                            <span className="font-bold text-xs">Cancel Campaign</span>
                          </DropdownMenuItem>
                        )}
                        <DropdownMenuItem className="gap-2 px-3 py-2.5 rounded-lg cursor-pointer text-destructive focus:text-destructive focus:bg-destructive/10">
                          <Trash2 size={16} />
                          <span className="font-bold text-xs">Delete Record</span>
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
        <div className="px-8 py-4 bg-muted/30 flex items-center justify-between text-sm">
          <span className="font-medium text-muted-foreground">Showing 4 of 128 campaigns</span>
          <Pagination className="w-auto mx-0">
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious href="#" className="h-9 px-4 rounded-lg bg-background border border-muted-foreground/20 hover:bg-muted transition-colors" />
              </PaginationItem>
              <PaginationItem>
                <PaginationLink href="#" isActive className="h-9 w-9 rounded-lg bg-primary text-white font-bold">1</PaginationLink>
              </PaginationItem>
              <PaginationItem>
                <PaginationLink href="#" className="h-9 w-9 rounded-lg bg-background border border-muted-foreground/20 hover:bg-muted transition-colors">2</PaginationLink>
              </PaginationItem>
              <PaginationItem>
                <PaginationNext href="#" className="h-9 px-4 rounded-lg bg-background border border-muted-foreground/20 hover:bg-muted transition-colors" />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      </Card>

      {/* Performance Ledger */}
      <section className="mt-16">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">Template Performance Ledger</h2>
            <p className="text-muted-foreground text-sm mt-1">Efficiency metrics by design structure.</p>
          </div>
          <Button variant="link" className="text-primary font-bold text-sm hover:underline flex items-center gap-1 group p-0 h-auto">
            View All Templates
            <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
          </Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <Card className="p-8 rounded-3xl flex items-start gap-8 border-none border-l-8 border-primary shadow-sm bg-muted/10">
            <div className="w-20 h-24 bg-muted rounded-xl flex-shrink-0 overflow-hidden shadow-inner">
              <img 
                src="https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=200&h=240&q=80" 
                alt="Template Preview" 
                className="w-full h-full object-cover opacity-80"
                referrerPolicy="no-referrer"
              />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-bold text-lg">Modern_Dark_Theme_V2</h3>
                <Badge variant="secondary" className="text-[10px] font-bold text-secondary bg-secondary/10 px-2.5 py-1 rounded-full uppercase tracking-widest border-none">High Conversion</Badge>
              </div>
              <div className="grid grid-cols-3 gap-6">
                <div>
                  <p className="technical-label mb-1">CTR</p>
                  <p className="text-lg font-extrabold">18.4%</p>
                </div>
                <div>
                  <p className="technical-label mb-1">Bounces</p>
                  <p className="text-lg font-extrabold">0.4%</p>
                </div>
                <div>
                  <p className="technical-label mb-1">Sent</p>
                  <p className="text-lg font-extrabold">42k</p>
                </div>
              </div>
            </div>
          </Card>

          <Card className="p-8 rounded-3xl flex items-start gap-8 border-none border-l-8 border-muted-foreground/20 shadow-sm bg-muted/10">
            <div className="w-20 h-24 bg-muted rounded-xl flex-shrink-0 overflow-hidden shadow-inner">
              <img 
                src="https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?auto=format&fit=crop&w=200&h=240&q=80" 
                alt="Template Preview" 
                className="w-full h-full object-cover opacity-80"
                referrerPolicy="no-referrer"
              />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-bold text-lg">Editorial_Layout_Rich</h3>
                <Badge variant="secondary" className="text-[10px] font-bold text-muted-foreground bg-muted px-2.5 py-1 rounded-full uppercase tracking-widest border-none">Standard</Badge>
              </div>
              <div className="grid grid-cols-3 gap-6">
                <div>
                  <p className="technical-label mb-1">CTR</p>
                  <p className="text-lg font-extrabold">12.1%</p>
                </div>
                <div>
                  <p className="technical-label mb-1">Bounces</p>
                  <p className="text-lg font-extrabold">1.2%</p>
                </div>
                <div>
                  <p className="technical-label mb-1">Sent</p>
                  <p className="text-lg font-extrabold">15k</p>
                </div>
              </div>
            </div>
          </Card>
        </div>
      </section>
    </div>
  );
}
