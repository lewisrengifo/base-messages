import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  UserPlus,
  Search,
  MoreVertical,
  ChevronLeft,
  ChevronRight,
  Loader2,
  AlertCircle,
  CheckCircle2,
  Users,
  Eye,
  Pencil,
  Trash2,
  X,
  Mail,
  Phone,
  Clock,
  User,
} from 'lucide-react';
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
import { createContact, deleteContact, getContact, listContacts, updateContact } from '@/api/contacts';
import type { Contact, ContactDetail, Pagination } from '@/api/types';

const PAGE_SIZE = 20;
const E164_PATTERN = /^\+[1-9]\d{1,14}$/;

export default function ContactsPage() {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [pagination, setPagination] = useState<Pagination | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [searchInput, setSearchInput] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
  const [page, setPage] = useState(1);

  // Create dialog state
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [createFormData, setCreateFormData] = useState({ name: '', phone: '', email: '' });

  // View dialog state
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [viewingContact, setViewingContact] = useState<ContactDetail | null>(null);
  const [viewLoading, setViewLoading] = useState(false);

  // Edit dialog state
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<Contact | null>(null);
  const [editFormData, setEditFormData] = useState({ name: '', phone: '', email: '' });
  const [updating, setUpdating] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);

  // Delete dialog state
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [deletingContact, setDeletingContact] = useState<Contact | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

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

  // Auto-clear success messages
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [success]);

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

  // Create
  const handleCreateContact = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError(null);

    const name = createFormData.name.trim();
    const phone = createFormData.phone.trim();
    const email = createFormData.email.trim();

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
      await createContact({ name, phone, email: email || undefined });
      setSuccess('Contact created successfully');
      setCreateFormData({ name: '', phone: '', email: '' });
      setIsCreateOpen(false);
      setPage(1);
      await fetchContacts();
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : 'Failed to create contact');
    } finally {
      setCreating(false);
    }
  };

  // View
  const openView = async (contact: Contact) => {
    setViewLoading(true);
    setIsViewOpen(true);
    try {
      const detail = await getContact(contact.id);
      setViewingContact(detail);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load contact details');
      setIsViewOpen(false);
    } finally {
      setViewLoading(false);
    }
  };

  // Edit
  const openEdit = (contact: Contact) => {
    setEditingContact(contact);
    setEditFormData({ name: contact.name, phone: contact.phone, email: '' });
    setEditError(null);
    setIsEditOpen(true);
  };

  const handleUpdateContact = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingContact) return;
    setEditError(null);

    const name = editFormData.name.trim();
    const phone = editFormData.phone.trim();
    const email = editFormData.email.trim();

    if (!name) {
      setEditError('Name is required');
      return;
    }
    if (!E164_PATTERN.test(phone)) {
      setEditError('Phone must be in E.164 format, for example +15550123456');
      return;
    }

    const payload: { name?: string; phone?: string; email?: string } = {};
    if (name !== editingContact.name) payload.name = name;
    if (phone !== editingContact.phone) payload.phone = phone;
    if (email) payload.email = email;

    // If nothing changed, just close
    if (Object.keys(payload).length === 0) {
      setIsEditOpen(false);
      return;
    }

    setUpdating(true);
    try {
      await updateContact(editingContact.id, payload);
      setSuccess('Contact updated successfully');
      setIsEditOpen(false);
      await fetchContacts();
    } catch (err) {
      setEditError(err instanceof Error ? err.message : 'Failed to update contact');
    } finally {
      setUpdating(false);
    }
  };

  // Delete
  const openDelete = (contact: Contact) => {
    setDeletingContact(contact);
    setDeleteError(null);
    setIsDeleteOpen(true);
  };

  const handleDeleteContact = async () => {
    if (!deletingContact) return;
    setDeleteError(null);
    setDeleting(true);
    try {
      await deleteContact(deletingContact.id);
      setSuccess('Contact deleted successfully');
      setIsDeleteOpen(false);
      await fetchContacts();
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Failed to delete contact');
    } finally {
      setDeleting(false);
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
                    value={createFormData.name}
                    onChange={(e) => setCreateFormData(prev => ({ ...prev, name: e.target.value }))}
                    placeholder="Jane Doe"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="contact-phone">Phone (E.164)</Label>
                  <Input
                    id="contact-phone"
                    value={createFormData.phone}
                    onChange={(e) => setCreateFormData(prev => ({ ...prev, phone: e.target.value }))}
                    placeholder="+15550123456"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="contact-email">Email (optional)</Label>
                  <Input
                    id="contact-email"
                    type="email"
                    value={createFormData.email}
                    onChange={(e) => setCreateFormData(prev => ({ ...prev, email: e.target.value }))}
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

      {success && (
        <Alert className="bg-emerald-50 text-emerald-800 border-emerald-200">
          <CheckCircle2 className="h-4 w-4" />
          <AlertDescription>{success}</AlertDescription>
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
                            <DropdownMenuItem onClick={() => openView(contact)}>
                              <Eye size={14} className="mr-2" />
                              View Details
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => openEdit(contact)}>
                              <Pencil size={14} className="mr-2" />
                              Edit Contact
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => openDelete(contact)}
                              className="text-destructive focus:text-destructive focus:bg-destructive/10"
                            >
                              <Trash2 size={14} className="mr-2" />
                              Delete Contact
                            </DropdownMenuItem>
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

      {/* View Details Dialog */}
      <Dialog open={isViewOpen} onOpenChange={setIsViewOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Contact Details</DialogTitle>
          </DialogHeader>
          {viewLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
          ) : viewingContact ? (
            <div className="space-y-6">
              <div className="flex items-center gap-4">
                <Avatar className={viewingContact.color}>
                  <AvatarFallback className="bg-transparent font-bold text-sm">{viewingContact.initials}</AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-bold text-lg">{viewingContact.name}</p>
                  <p className="text-sm text-muted-foreground">ID: {viewingContact.id}</p>
                </div>
              </div>
              <div className="space-y-3">
                <div className="flex items-center gap-3 text-sm">
                  <Phone size={16} className="text-muted-foreground" />
                  <span className="font-mono">{viewingContact.phone}</span>
                </div>
                {viewingContact.email && (
                  <div className="flex items-center gap-3 text-sm">
                    <Mail size={16} className="text-muted-foreground" />
                    <span>{viewingContact.email}</span>
                  </div>
                )}
                <div className="flex items-center gap-3 text-sm">
                  <Clock size={16} className="text-muted-foreground" />
                  <span>Created {new Date(viewingContact.createdAt).toLocaleString()}</span>
                </div>
              </div>
            </div>
          ) : null}
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Edit Contact</DialogTitle>
            <DialogDescription>
              Update contact information.
            </DialogDescription>
          </DialogHeader>

          {editError && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{editError}</AlertDescription>
            </Alert>
          )}

          <form onSubmit={handleUpdateContact} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="edit-name">Name</Label>
              <Input
                id="edit-name"
                value={editFormData.name}
                onChange={(e) => setEditFormData(prev => ({ ...prev, name: e.target.value }))}
                placeholder="Jane Doe"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="edit-phone">Phone (E.164)</Label>
              <Input
                id="edit-phone"
                value={editFormData.phone}
                onChange={(e) => setEditFormData(prev => ({ ...prev, phone: e.target.value }))}
                placeholder="+15550123456"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="edit-email">Email (optional)</Label>
              <Input
                id="edit-email"
                type="email"
                value={editFormData.email}
                onChange={(e) => setEditFormData(prev => ({ ...prev, email: e.target.value }))}
                placeholder="jane@example.com"
              />
            </div>
            <DialogFooter>
              <Button type="submit" disabled={updating} className="w-full sm:w-auto">
                {updating ? <Loader2 size={16} className="animate-spin" /> : 'Update Contact'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={isDeleteOpen} onOpenChange={setIsDeleteOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Delete Contact</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete <span className="font-bold">{deletingContact?.name}</span>? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>

          {deleteError && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{deleteError}</AlertDescription>
            </Alert>
          )}

          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setIsDeleteOpen(false)} disabled={deleting}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDeleteContact} disabled={deleting}>
              {deleting ? <Loader2 size={16} className="animate-spin" /> : 'Delete Contact'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
