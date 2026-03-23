import React from 'react';
import { 
  LayoutDashboard, 
  Bot as Hub, 
  FileText, 
  Megaphone, 
  HelpCircle, 
  LogOut, 
  Bell, 
  Settings,
  PlusCircle,
  User,
  Menu,
  Loader2
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { motion } from 'motion/react';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Sheet, SheetContent, SheetTrigger, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { useAuth } from '@/context/AuthContext';

import { 
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu';

export type PageId = 'login' | 'connection' | 'contacts' | 'templates' | 'campaigns' | 'template-builder' | 'campaign-builder' | 'submission-sent' | 'campaign-sent' | 'campaign-analytics';

const NOTIFICATIONS = [
  { id: 1, title: 'Campaign Approved', description: 'Your "Q3 Product Reveal" template has been approved by Meta.', time: '2h ago', unread: true },
  { id: 2, title: 'Low Balance Warning', description: 'Your API credit balance is below $50. Please recharge.', time: '5h ago', unread: true },
  { id: 3, title: 'New Contact List', description: 'Import of "Summer Leads 2024" completed successfully.', time: '1d ago', unread: false },
  { id: 4, title: 'System Update', description: 'Sapphire API v2.4 is now live with enhanced analytics.', time: '2d ago', unread: false },
];

interface LayoutProps {
  children: React.ReactNode;
  activePage: PageId;
  onNavigate: (page: PageId) => void;
}

export default function Layout({ children, activePage, onNavigate }: LayoutProps) {
  const { user, logout, isLoading } = useAuth();

  const navItems = [
    { id: 'campaigns', label: 'Campaigns', icon: Megaphone },
    { id: 'templates', label: 'Templates', icon: FileText },
    { id: 'contacts', label: 'Contacts', icon: User },
    { id: 'connection', label: 'Connection', icon: Hub },
  ] as const;

  const getActiveNavId = (pageId: PageId): string | null => {
    if (pageId === 'template-builder' || pageId === 'submission-sent') return 'templates';
    if (pageId === 'campaign-builder' || pageId === 'campaign-sent' || pageId === 'campaign-analytics') return 'campaigns';
    return pageId;
  };

  const activeNavId = getActiveNavId(activePage);

  const handleLogout = async () => {
    try {
      await logout();
      // The AuthContext will clear the user, and the LoginGate in App will show login page
      window.location.reload(); // Simple way to reset app state
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  const SidebarContent = () => (
    <div className="flex flex-col h-full gap-2">
      <div className="mb-6 px-2 py-4">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 bg-primary rounded-xl flex items-center justify-center shadow-lg shadow-primary/20">
            <LayoutDashboard className="text-primary-foreground" size={20} />
          </div>
          <div>
            <h3 className="font-headline font-bold text-sm leading-none">Business Manager</h3>
            <p className="technical-label mt-1">Verified Account</p>
          </div>
        </div>
      </div>

      <ScrollArea className="flex-1 -mx-2 px-2">
        <div className="flex flex-col gap-1">
          {navItems.map((item) => (
            <Button
              key={item.id}
              variant={activeNavId === item.id ? "secondary" : "ghost"}
              onClick={() => onNavigate(item.id as PageId)}
              className={cn(
                "justify-start gap-3 px-4 py-6 rounded-xl font-headline text-sm font-semibold transition-all duration-200",
                activeNavId === item.id && "shadow-sm translate-x-1 text-primary"
              )}
            >
              <item.icon size={20} />
              <span>{item.label}</span>
            </Button>
          ))}
        </div>
      </ScrollArea>

      <div className="mt-auto flex flex-col gap-2 pt-4">
        <Separator className="mb-4" />
        <Button 
          onClick={() => onNavigate('template-builder')}
          className="w-full py-6 rounded-xl font-bold text-sm gap-2 shadow-lg shadow-primary/20 active:scale-95 transition-transform mb-4"
        >
          <PlusCircle size={18} />
          New Template
        </Button>
        <Button variant="ghost" className="justify-start gap-3 px-4 py-6 text-muted-foreground font-headline text-sm font-semibold">
          <HelpCircle size={20} />
          <span>Help Center</span>
        </Button>
        <Button 
          variant="ghost" 
          onClick={handleLogout}
          disabled={isLoading}
          className="justify-start gap-3 px-4 py-6 text-destructive font-headline text-sm font-semibold hover:bg-destructive/10 hover:text-destructive"
        >
          {isLoading ? (
            <Loader2 size={20} className="animate-spin" />
          ) : (
            <LogOut size={20} />
          )}
          <span>{isLoading ? 'Logging out...' : 'Logout'}</span>
        </Button>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      {/* Top Navigation Bar */}
      <nav className="fixed top-0 w-full z-50 glass-panel h-16 flex items-center justify-between px-6 border-b">
        <div className="flex items-center gap-4">
          <Sheet>
            <SheetTrigger render={<Button variant="ghost" size="icon" className="lg:hidden" />}>
              <Menu size={20} />
            </SheetTrigger>
            <SheetContent side="left" className="w-64 p-4">
              <SheetHeader className="text-left mb-4">
                <SheetTitle className="text-xl font-bold tracking-tighter text-primary font-headline">Base</SheetTitle>
              </SheetHeader>
              <SidebarContent />
            </SheetContent>
          </Sheet>
          <span className="text-xl font-bold tracking-tighter text-primary font-headline">Base</span>
        </div>
        <div className="flex items-center gap-4">
          <DropdownMenu>
            <DropdownMenuTrigger render={
              <Button variant="ghost" size="icon" className="text-muted-foreground relative">
                <Bell size={20} />
                {NOTIFICATIONS.some(n => n.unread) && (
                  <span className="absolute top-2 right-2 w-2 h-2 bg-primary rounded-full border-2 border-background" />
                )}
              </Button>
            } />
            <DropdownMenuContent align="end" className="w-80 rounded-2xl p-2 shadow-2xl border-none">
              <div className="p-4 flex items-center justify-between">
                <h3 className="font-bold text-sm">Notifications</h3>
                <Button variant="link" className="p-0 h-auto text-xs font-bold text-primary">Mark all as read</Button>
              </div>
              <DropdownMenuSeparator className="bg-muted/50" />
              <ScrollArea className="h-[320px]">
                <div className="p-2 space-y-1">
                  {NOTIFICATIONS.map((notif) => (
                    <DropdownMenuItem key={notif.id} className="flex flex-col items-start gap-1 p-3 rounded-xl cursor-pointer focus:bg-muted/50">
                      <div className="flex items-center justify-between w-full">
                        <span className={cn("text-sm font-bold", notif.unread && "text-primary")}>{notif.title}</span>
                        <span className="text-[10px] font-medium text-muted-foreground">{notif.time}</span>
                      </div>
                      <p className="text-xs text-muted-foreground leading-relaxed line-clamp-2">{notif.description}</p>
                    </DropdownMenuItem>
                  ))}
                </div>
              </ScrollArea>
              <DropdownMenuSeparator className="bg-muted/50" />
              <div className="p-2">
                <Button variant="ghost" className="w-full text-xs font-bold text-muted-foreground hover:text-primary">View all notifications</Button>
              </div>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button variant="ghost" size="icon" className="text-muted-foreground">
            <Settings size={20} />
          </Button>
          <Avatar className="h-8 w-8 border">
            <AvatarImage 
              src={user?.avatar || "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=facearea&facepad=2&w=256&h=256&q=80"}
              alt={user?.name || "User Profile"}
              referrerPolicy="no-referrer"
            />
            <AvatarFallback>{user?.name?.split(' ').map(n => n[0]).join('') || 'U'}</AvatarFallback>
          </Avatar>
        </div>
      </nav>

      <div className="flex flex-1 pt-16">
        {/* Sidebar Navigation (Desktop) */}
        <aside className="h-[calc(100vh-4rem)] w-64 fixed left-0 bg-muted/30 flex flex-col p-4 gap-2 hidden lg:flex border-r">
          <SidebarContent />
        </aside>

        {/* Main Content Area */}
        <main className="flex-1 lg:ml-64 min-h-full">
          <motion.div
            key={activePage}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
            className="max-w-7xl mx-auto px-4 md:px-8 py-12"
          >
            {children}
          </motion.div>
        </main>
      </div>
    </div>
  );
}
