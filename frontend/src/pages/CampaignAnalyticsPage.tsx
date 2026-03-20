import React from 'react';
import { 
  ArrowLeft, 
  BarChart3, 
  Users, 
  MousePointer2, 
  Mail, 
  AlertCircle, 
  TrendingUp, 
  Clock, 
  Calendar,
  ChevronRight,
  Download,
  Share2
} from 'lucide-react';
import { cn } from '@/src/lib/utils';
import { Button } from '@/src/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/src/components/ui/card';
import { Badge } from '@/src/components/ui/badge';
import { Separator } from '@/src/components/ui/separator';
import { 
  ResponsiveContainer, 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  BarChart, 
  Bar, 
  Cell,
  PieChart,
  Pie
} from 'recharts';
import { PageId } from '../components/Layout';

const DELIVERY_DATA = [
  { time: '09:00', sent: 2400, delivered: 2380, opened: 1200 },
  { time: '10:00', sent: 4500, delivered: 4450, opened: 2800 },
  { time: '11:00', sent: 8200, delivered: 8100, opened: 5400 },
  { time: '12:00', sent: 12000, delivered: 11800, opened: 7200 },
  { time: '13:00', sent: 15600, delivered: 15300, opened: 9800 },
  { time: '14:00', sent: 18900, delivered: 18500, opened: 12400 },
  { time: '15:00', sent: 22400, delivered: 22000, opened: 14800 },
];

const DEVICE_DATA = [
  { name: 'Mobile', value: 74, color: '#4285F4' },
  { name: 'Desktop', value: 22, color: '#34A853' },
  { name: 'Tablet', value: 4, color: '#FBBC05' },
];

const ENGAGEMENT_DATA = [
  { name: 'Opened', value: 64.2, color: 'hsl(var(--primary))' },
  { name: 'Clicked', value: 18.4, color: '#10b981' },
  { name: 'Bounced', value: 0.4, color: '#ef4444' },
];

export default function CampaignAnalyticsPage({ onNavigate }: { onNavigate: (page: PageId) => void }) {
  return (
    <div className="space-y-12">
      {/* Header */}
      <header className="flex flex-col md:flex-row md:items-start justify-between gap-6">
        <div className="space-y-4">
          <Button 
            variant="ghost" 
            size="sm" 
            onClick={() => onNavigate('campaigns')}
            className="p-0 h-auto text-muted-foreground hover:text-primary font-bold flex items-center gap-2 group"
          >
            <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" />
            Back to Campaigns
          </Button>
          <div className="flex items-center gap-4">
            <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight">Q3 Product Reveal</h1>
            <Badge className="bg-emerald-100 text-emerald-700 border-none px-3 py-1 rounded-full font-bold uppercase tracking-widest text-[10px]">Sent</Badge>
          </div>
          <div className="flex flex-wrap items-center gap-6 text-sm text-muted-foreground font-medium">
            <div className="flex items-center gap-2">
              <Calendar size={16} />
              Launched Oct 12, 2023
            </div>
            <div className="flex items-center gap-2">
              <Clock size={16} />
              Duration: 6h 12m
            </div>
            <div className="flex items-center gap-2">
              <Users size={16} />
              Audience: 22,400 recipients
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" className="rounded-xl font-bold gap-2">
            <Share2 size={18} />
            Share Report
          </Button>
          <Button className="rounded-xl font-bold gap-2 shadow-lg shadow-primary/20">
            <Download size={18} />
            Export Data
          </Button>
        </div>
      </header>

      {/* Key Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard 
          label="Total Sent" 
          value="22,400" 
          trend="+100%" 
          icon={Mail} 
          color="text-primary"
        />
        <MetricCard 
          label="Open Rate" 
          value="64.2%" 
          trend="+4.2%" 
          icon={Users} 
          color="text-emerald-600"
        />
        <MetricCard 
          label="Click-Through" 
          value="18.4%" 
          trend="+2.1%" 
          icon={MousePointer2} 
          color="text-blue-600"
        />
        <MetricCard 
          label="Bounce Rate" 
          value="0.4%" 
          trend="-0.1%" 
          icon={AlertCircle} 
          color="text-destructive"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Delivery Timeline Chart */}
        <Card className="lg:col-span-2 rounded-3xl border-none shadow-sm bg-card overflow-hidden">
          <CardHeader className="p-8 pb-0">
            <CardTitle className="text-xl font-bold">Delivery Performance</CardTitle>
            <CardDescription className="text-sm">Real-time message orchestration metrics over the launch window.</CardDescription>
          </CardHeader>
          <CardContent className="p-8 pt-6">
            <div className="h-[350px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={DELIVERY_DATA}>
                  <defs>
                    <linearGradient id="colorSent" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.1}/>
                      <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="hsl(var(--muted))" />
                  <XAxis 
                    dataKey="time" 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fontSize: 12, fontWeight: 600, fill: 'hsl(var(--muted-foreground))' }}
                    dy={10}
                  />
                  <YAxis 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fontSize: 12, fontWeight: 600, fill: 'hsl(var(--muted-foreground))' }}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: 'hsl(var(--card))', 
                      borderRadius: '16px', 
                      border: 'none', 
                      boxShadow: '0 10px 30px -10px rgba(0,0,0,0.1)' 
                    }} 
                  />
                  <Area 
                    type="monotone" 
                    dataKey="sent" 
                    stroke="hsl(var(--primary))" 
                    strokeWidth={3}
                    fillOpacity={1} 
                    fill="url(#colorSent)" 
                  />
                  <Area 
                    type="monotone" 
                    dataKey="opened" 
                    stroke="#10b981" 
                    strokeWidth={3}
                    fillOpacity={0}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Engagement Breakdown */}
        <Card className="rounded-3xl border-none shadow-sm bg-card overflow-hidden">
          <CardHeader className="p-8 pb-0">
            <CardTitle className="text-xl font-bold">Engagement Mix</CardTitle>
            <CardDescription className="text-sm">Distribution of recipient interactions.</CardDescription>
          </CardHeader>
          <CardContent className="p-8 pt-6 flex flex-col items-center">
            <div className="h-[250px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={ENGAGEMENT_DATA}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={8}
                    dataKey="value"
                  >
                    {ENGAGEMENT_DATA.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="w-full space-y-4 mt-4">
              {ENGAGEMENT_DATA.map((item) => (
                <div key={item.name} className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                    <span className="text-sm font-bold">{item.name}</span>
                  </div>
                  <span className="text-sm font-extrabold">{item.value}%</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Device Distribution */}
        <Card className="rounded-3xl border-none shadow-sm bg-card overflow-hidden">
          <CardHeader className="p-8 pb-0">
            <CardTitle className="text-xl font-bold">Device Distribution</CardTitle>
            <CardDescription className="text-sm">Where your audience is viewing your message.</CardDescription>
          </CardHeader>
          <CardContent className="p-8 pt-6">
            <div className="space-y-6">
              {DEVICE_DATA.map((device) => (
                <div key={device.name} className="space-y-2">
                  <div className="flex items-center justify-between text-sm font-bold">
                    <span>{device.name}</span>
                    <span>{device.value}%</span>
                  </div>
                  <div className="h-2 w-full bg-muted rounded-full overflow-hidden">
                    <div 
                      className="h-full rounded-full transition-all duration-1000" 
                      style={{ width: `${device.value}%`, backgroundColor: device.color }} 
                    />
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Top Links */}
        <Card className="rounded-3xl border-none shadow-sm bg-card overflow-hidden">
          <CardHeader className="p-8 pb-0">
            <CardTitle className="text-xl font-bold">Top Performing Links</CardTitle>
            <CardDescription className="text-sm">Highest click-through URLs in this campaign.</CardDescription>
          </CardHeader>
          <CardContent className="p-8 pt-6">
            <div className="space-y-4">
              {[
                { label: 'View Product Collection', clicks: 2402, ctr: '10.7%' },
                { label: 'Pre-order Now', clicks: 1892, ctr: '8.4%' },
                { label: 'Technical Specifications', clicks: 420, ctr: '1.8%' },
              ].map((link, i) => (
                <div key={i} className="flex items-center justify-between p-4 rounded-2xl bg-muted/30 hover:bg-muted/50 transition-colors cursor-pointer group">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-lg bg-background flex items-center justify-center text-primary font-bold text-xs">
                      {i + 1}
                    </div>
                    <span className="text-sm font-bold group-hover:text-primary transition-colors">{link.label}</span>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-extrabold">{link.clicks}</p>
                    <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">{link.ctr} CTR</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function MetricCard({ label, value, trend, icon: Icon, color }: { 
  label: string, 
  value: string, 
  trend: string, 
  icon: any, 
  color: string 
}) {
  return (
    <Card className="p-6 rounded-3xl border-none shadow-sm bg-card flex flex-col justify-between h-full">
      <div className="flex items-center justify-between mb-4">
        <div className={cn("w-12 h-12 rounded-2xl bg-muted flex items-center justify-center", color)}>
          <Icon size={24} />
        </div>
        <Badge variant="secondary" className={cn(
          "text-[10px] font-bold px-2 py-0.5 rounded-full border-none",
          trend.startsWith('+') ? "text-emerald-600 bg-emerald-50" : "text-destructive bg-destructive/10"
        )}>
          {trend}
        </Badge>
      </div>
      <div>
        <p className="technical-label mb-1">{label}</p>
        <p className="text-3xl font-extrabold tracking-tight">{value}</p>
      </div>
    </Card>
  );
}
