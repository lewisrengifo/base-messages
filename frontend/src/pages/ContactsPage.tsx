import React from 'react';
import { UserPlus, Upload, Search, MoreVertical, ChevronLeft, ChevronRight } from 'lucide-react';
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

const CONTACTS = [
  { id: 1, name: 'Jane Doe', phone: '+1 (555) 012-3456', initials: 'JD', color: 'bg-blue-100 text-blue-700' },
  { id: 2, name: 'Michael Ross', phone: '+44 20 7946 0958', initials: 'MR', color: 'bg-indigo-100 text-indigo-700' },
  { id: 3, name: 'Sarah Chen', phone: '+65 8123 4567', initials: 'SC', color: 'bg-sky-100 text-sky-700' },
  { id: 4, name: 'Amara Kante', phone: '+234 803 123 4567', initials: 'AK', color: 'bg-emerald-100 text-emerald-700' },
];

export default function ContactsPage() {
  return (
    <div className="space-y-12">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div className="max-w-xl">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-4 tracking-tight">Contact Management</h1>
          <p className="text-muted-foreground text-lg leading-relaxed">
            Manage your business connections and broadcast lists. Import customers from CSV to start high-volume messaging.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="secondary" className="h-12 px-6 rounded-xl font-bold gap-2">
            <UserPlus size={18} />
            <span>Add Contact</span>
          </Button>
          <Button className="h-12 px-8 rounded-xl font-bold shadow-lg shadow-primary/20 gap-2">
            <Upload size={18} />
            <span>Import Contacts</span>
          </Button>
        </div>
      </header>

      <Card className="rounded-2xl overflow-hidden border-none shadow-sm bg-card">
        <CardHeader className="p-6 flex flex-row items-center justify-between border-b bg-background/50">
          <CardTitle className="font-bold text-lg">Active Contacts</CardTitle>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground/60" size={16} />
            <Input 
              type="text" 
              placeholder="Search by name or phone..."
              className="pl-10 pr-4 h-10 bg-muted/50 border-none rounded-xl text-sm focus-visible:ring-primary/20 w-64 transition-all"
            />
          </div>
        </CardHeader>

        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/30 hover:bg-muted/30">
                <TableHead className="px-8 py-4 technical-label h-12">Name</TableHead>
                <TableHead className="px-8 py-4 technical-label h-12">Phone Number</TableHead>
                <TableHead className="px-8 py-4 technical-label text-right h-12">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {CONTACTS.map((contact) => (
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
                      <DropdownMenuTrigger render={
                        <Button variant="ghost" size="icon" className="text-muted-foreground hover:text-primary">
                          <MoreVertical size={20} />
                        </Button>
                      } />
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem>Edit Contact</DropdownMenuItem>
                        <DropdownMenuItem>View History</DropdownMenuItem>
                        <DropdownMenuItem className="text-destructive">Delete Contact</DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>

        <div className="p-6 bg-muted/10 flex items-center justify-between text-sm text-muted-foreground">
          <span className="font-medium">Showing {CONTACTS.length} of {CONTACTS.length} contacts</span>
          <div className="flex gap-2">
            <Button variant="outline" size="icon" className="h-9 w-9 rounded-lg border-muted-foreground/20 hover:bg-background transition-colors" disabled>
              <ChevronLeft size={18} />
            </Button>
            <Button variant="outline" size="icon" className="h-9 w-9 rounded-lg border-muted-foreground/20 hover:bg-background transition-colors">
              <ChevronRight size={18} />
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
}
