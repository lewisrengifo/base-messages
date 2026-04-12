import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { UserPlus, Search, MoreVertical, ChevronLeft, ChevronRight, Loader2, AlertCircle, CheckCircle2, Users } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { createContact, listContacts } from '@/api/contacts';
import type { Contact, Pagination } from '@/api/types';

const PAGE_SIZE = 20;
const E164_PATTERN = /^\+[1-9]\d{1,14}$/;

export default function ContactsPage() {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [pagination, setPagination] = useState<Pagination | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [searchInput, setSearchInput] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
  const [page, setPage] = useState(1);

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [createSuccess, setCreateSuccess] = useState<string | null>(null);
  const [formData, setFormData] = useState({ name: '', phone: '', email: '' });

  const fetchContacts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await listContacts({
        page,
        limit: PAGE_SIZE,
        search: activeSearch || undefined,
      });
      setContacts(response.data);
      setPagination(response.pagination);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load contacts');
    } finally {
      setLoading(false);
    }
  }, [page, activeSearch]);

  useEffect(() => {
    fetchContacts();
  }, [fetchContacts]);

  const canGoPrev = pagination?.hasPrev ?? page > 1;
  const canGoNext = pagination?.hasNext ?? false;

  const resultsLabel = useMemo(() => {
    const start = contacts.length === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
    const end = (page - 1) * PAGE_SIZE + contacts.length;
    const total = pagination?.total ?? end;
    return `Showing ${start}-${end} of ${total} contacts`;
  }, [contacts.length, page, pagination?.total]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    setActiveSearch(searchInput.trim());
  };

  const handleCreateContact = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError(null);
    setCreateSuccess(null);

    const name = formData.name.trim();
    const phone = formData.phone.trim();
    const email = formData.email.trim();

    if (!name) {
      setCreateError('Name is required');
      return;
    }
    if (!E164_PATTERN.test(phone)) {
      setCreateError('Phone must be in E.164 format, for example +15550123456');
      return;
    }

    setCreating(true);
    try {
      await createContact({
        name,
        phone,
        email: email || undefined,
      });
      setCreateSuccess('Contact created successfully');
      setFormData({ name: '', phone: '', email: '' });
      setIsCreateOpen(false);
      setPage(1);
      await fetchContacts();
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : 'Failed to create contact');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="space-y-12">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div className="max-w-xl">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-4 tracking-tight">Contact Management</h1>
          <p className="text-muted-foreground text-lg leading-relaxed">
            Build and manage your campaign audience. Add recipients with validated phone numbers and keep your outreach list clean.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
            <DialogTrigger
              render={
                <Button variant="secondary" className="h-12 px-6 rounded-xl font-bold gap-2">
                  <UserPlus size={18} />
                  <span>Add Contact</span>
                </Button>
              }
            />
            <DialogContent className="sm:max-w-md">
              <DialogHeader>
                <DialogTitle>Create Contact</DialogTitle>
                <DialogDescription>
                  Add a recipient using E.164 phone format.
                </DialogDescription>
              </DialogHeader>

              {createError && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{createError}</AlertDescription>
                </Alert>
              )}

              <form onSubmit={handleCreateContact} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="contact-name">Name</Label>
                  <Input
                    id="contact-name"
                    value={formData.name}
                    onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                    placeholder="Jane Doe"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="contact-phone">Phone (E.164)</Label>
                  <Input
                    id="contact-phone"
                    value={formData.phone}
                    onChange={(e) => setFormData(prev => ({ ...prev, phone: e.target.value }))}
                    placeholder="+15550123456"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="contact-email">Email (optional)</Label>
                  <Input
                    id="contact-email"
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
                    placeholder="jane@example.com"
                  />
                </div>
                <DialogFooter>
                  <Button type="submit" disabled={creating} className="w-full sm:w-auto">
                    {creating ? <Loader2 size={16} className="animate-spin" /> : 'Create Contact'}
                  </Button>
                </DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
        </div>
      </header>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {createSuccess && (
        <Alert className="bg-emerald-50 text-emerald-800 border-emerald-200">
          <CheckCircle2 className="h-4 w-4" />
          <AlertDescription>{createSuccess}</AlertDescription>
        </Alert>
      )}

      <Card className="rounded-2xl overflow-hidden border-none shadow-sm bg-card">
        <CardHeader className="p-6 flex flex-row items-center justify-between border-b bg-background/50">
          <CardTitle className="font-bold text-lg flex items-center gap-2">
            <Users size={18} className="text-primary" />
            Active Contacts
          </CardTitle>
          <form onSubmit={handleSearchSubmit} className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground/60" size={16} />
            <Input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Search by name or phone..."
              className="pl-10 pr-4 h-10 bg-muted/50 border-none rounded-xl text-sm focus-visible:ring-primary/20 w-64 transition-all"
            />
          </form>
        </CardHeader>

        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center min-h-[260px]">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow className="bg-muted/30 hover:bg-muted/30">
                  <TableHead className="px-8 py-4 technical-label h-12">Name</TableHead>
                  <TableHead className="px-8 py-4 technical-label h-12">Phone Number</TableHead>
                  <TableHead className="px-8 py-4 technical-label text-right h-12">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {contacts.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3} className="px-8 py-12 text-center text-muted-foreground">
                      No contacts found.
                    </TableCell>
                  </TableRow>
                ) : (
                  contacts.map((contact) => (
                    <TableRow key={contact.id} className="hover:bg-muted/20 transition-colors group border-b">
                      <TableCell className="px-8 py-5">
                        <div className="flex items-center gap-3">
                          <Avatar className={contact.color}>
                            <AvatarFallback className="bg-transparent font-bold text-xs">{contact.initials}</AvatarFallback>
                          </Avatar>
                          <span className="font-bold text-foreground">{contact.name}</span>
                        </div>
                      </TableCell>
                      <TableCell className="px-8 py-5 font-mono text-sm text-muted-foreground">{contact.phone}</TableCell>
                      <TableCell className="px-8 py-5 text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger
                            render={
                              <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-primary">
                                <MoreVertical size={20} />
                              </Button>
                            }
                          />
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem>View Details</DropdownMenuItem>
                            <DropdownMenuItem disabled>Edit Contact (Phase 3)</DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>

        <div className="p-6 bg-muted/10 flex items-center justify-between text-sm text-muted-foreground">
          <span className="font-medium">{resultsLabel}</span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="icon"
              className="h-9 w-9 rounded-lg border-muted-foreground/20 hover:bg-background transition-colors"
              disabled={!canGoPrev || loading}
              onClick={() => setPage((prev) => Math.max(1, prev - 1))}
            >
              <ChevronLeft size={18} />
            </Button>
            <Button
              variant="outline"
              size="icon"
              className="h-9 w-9 rounded-lg border-muted-foreground/20 hover:bg-background transition-colors"
              disabled={!canGoNext || loading}
              onClick={() => setPage((prev) => prev + 1)}
            >
              <ChevronRight size={18} />
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
}
