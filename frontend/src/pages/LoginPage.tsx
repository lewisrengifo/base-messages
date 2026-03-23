import React, { useState, useEffect } from 'react';
import { LayoutDashboard, Mail, Lock, ArrowRight, Loader2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { motion } from 'motion/react';
import { useAuth } from '@/context/AuthContext';

interface LoginPageProps {
  onLogin: () => void;
}

export default function LoginPage({ onLogin }: LoginPageProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, isLoading, error, clearError, isAuthenticated } = useAuth();

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      onLogin();
    }
  }, [isAuthenticated, onLogin]);

  // Clear error when user types
  useEffect(() => {
    if (error) {
      clearError();
    }
  }, [email, password, clearError, error]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      return;
    }

    try {
      await login(email, password);
      // onLogin will be called by the useEffect when isAuthenticated changes
    } catch {
      // Error is already handled by AuthContext
      // We just need to prevent the form from submitting
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-muted/30 p-4 relative overflow-hidden">
      {/* Background Accents */}
      <div className="absolute top-0 left-0 w-full h-full -z-10 overflow-hidden">
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/5 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-primary/5 rounded-full blur-[120px]" />
      </div>

      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
        className="w-full max-w-md"
      >
        <div className="flex flex-col items-center mb-8">
          <div className="h-16 w-16 bg-primary rounded-2xl flex items-center justify-center shadow-2xl shadow-primary/20 mb-4">
            <LayoutDashboard className="text-primary-foreground" size={32} />
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight font-headline">Base</h1>
          <p className="text-muted-foreground font-medium mt-1">Enterprise Messaging Platform</p>
        </div>

        <Card className="border-none shadow-2xl shadow-black/5 rounded-[2rem] overflow-hidden">
          <CardHeader className="p-8 pb-4 text-center">
            <CardTitle className="text-2xl font-bold">Welcome Back</CardTitle>
            <CardDescription className="text-base">Enter your credentials to access your dashboard</CardDescription>
          </CardHeader>
          <CardContent className="p-8 pt-4">
            {/* Error Alert */}
            {error && (
              <Alert variant="destructive" className="mb-6 rounded-xl">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}
            
            <form onSubmit={handleLogin} className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="email" className="text-xs font-bold uppercase tracking-widest opacity-60 ml-1">Email Address</Label>
                <div className="relative group">
                  <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground group-focus-within:text-primary transition-colors" size={18} />
                  <Input 
                    id="email" 
                    type="email" 
                    placeholder="name@company.com" 
                    required 
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    disabled={isLoading}
                    className="h-14 pl-12 rounded-2xl bg-muted/50 border-none focus-visible:ring-primary/20 transition-all"
                  />
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex items-center justify-between ml-1">
                  <Label htmlFor="password" className="text-xs font-bold uppercase tracking-widest opacity-60">Password</Label>
                  <Button variant="link" className="p-0 h-auto text-xs font-bold text-primary hover:no-underline">Forgot password?</Button>
                </div>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground group-focus-within:text-primary transition-colors" size={18} />
                  <Input 
                    id="password" 
                    type="password" 
                    placeholder="••••••••" 
                    required 
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    disabled={isLoading}
                    className="h-14 pl-12 rounded-2xl bg-muted/50 border-none focus-visible:ring-primary/20 transition-all"
                  />
                </div>
              </div>
              <Button 
                type="submit" 
                disabled={isLoading || !email || !password}
                className="w-full h-14 rounded-2xl font-bold text-lg shadow-xl shadow-primary/20 active:scale-[0.98] transition-all flex items-center justify-center gap-2"
              >
                {isLoading ? (
                  <Loader2 className="animate-spin" size={20} />
                ) : (
                  <>
                    Sign In
                    <ArrowRight size={20} />
                  </>
                )}
              </Button>
            </form>
          </CardContent>
          <CardFooter className="p-8 pt-0 flex justify-center border-t bg-muted/10">
            <p className="text-sm text-muted-foreground">
              Don't have an account? <Button variant="link" className="p-0 h-auto font-bold text-primary hover:no-underline">Contact Sales</Button>
            </p>
          </CardFooter>
        </Card>

        <div className="mt-8 flex justify-center gap-6">
          <Button variant="ghost" className="text-xs font-bold text-muted-foreground hover:text-primary">Privacy Policy</Button>
          <Button variant="ghost" className="text-xs font-bold text-muted-foreground hover:text-primary">Terms of Service</Button>
        </div>
      </motion.div>
    </div>
  );
}
