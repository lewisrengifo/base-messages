import React, { useState, useEffect } from 'react';
import { ArrowLeft, CheckCircle2, Settings, Info, Send, Video, Phone, Smile, Paperclip, Camera, Mic, AlertTriangle, Loader2, XCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { createTemplate, updateTemplate, getTemplate } from '@/api/templates';
import type { CreateTemplateRequest, TemplateCategory, UpdateTemplateRequest } from '@/api/types';
import { PageId } from '../components/Layout';
import { ApiClientError } from '@/api/client';

interface TemplateBuilderPageProps {
  onNavigate: (page: PageId) => void;
  onTemplateSubmitted: (templateName: string) => void;
  editTemplateId?: number | null;
}

export default function TemplateBuilderPage({ onNavigate, onTemplateSubmitted, editTemplateId }: TemplateBuilderPageProps) {
  const isEditMode = !!editTemplateId;
  const [formData, setFormData] = useState<CreateTemplateRequest>({
    name: '',
    category: 'Marketing',
    content: '',
    language: 'en_US',
    variables: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [preview, setPreview] = useState('');
  const [fetchingTemplate, setFetchingTemplate] = useState(isEditMode);

  // Load template data when in edit mode
  useEffect(() => {
    if (isEditMode && editTemplateId) {
      setFetchingTemplate(true);
      getTemplate(editTemplateId)
        .then((template) => {
          setFormData({
            name: template.name,
            category: template.category,
            content: template.content,
            language: template.language.toLowerCase(),
            variables: template.variables?.map((v) => ({ example: v.example })) || [],
          });
          // Generate preview
          let previewText = template.content;
          template.variables?.forEach((v, index) => {
            const placeholder = `{{${index + 1}}}`;
            previewText = previewText.replace(
              new RegExp(placeholder.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'),
              v.example || `[Value ${index + 1}]`
            );
          });
          setPreview(previewText);
        })
        .catch((err) => {
          setError(err instanceof Error ? err.message : 'Failed to load template');
        })
        .finally(() => {
          setFetchingTemplate(false);
        });
    }
  }, [isEditMode, editTemplateId]);

  const extractMetaErrorMessage = (err: unknown): string => {
    if (err instanceof ApiClientError && err.apiError) {
      const message = err.apiError.message || 'Unknown error';
      if (err.apiError.details && typeof err.apiError.details === 'object') {
        const details = err.apiError.details as Record<string, unknown>;
        if (details.metaError) {
          try {
            const metaError = JSON.parse(details.metaError as string);
            if (metaError.error && metaError.error.error_user_msg) {
              return metaError.error.error_user_msg;
            }
            if (metaError.error && metaError.error.message) {
              return metaError.error.message;
            }
          } catch {
            return details.metaError as string;
          }
        }
      }
      return message;
    }
    if (err instanceof Error) {
      return err.message;
    }
    return 'An unexpected error occurred';
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      if (isEditMode && editTemplateId) {
        const updateData: UpdateTemplateRequest = {
          name: formData.name,
          content: formData.content,
          variables: formData.variables,
        };
        await updateTemplate(editTemplateId, updateData);
        onNavigate('templates');
      } else {
        const createdTemplate = await createTemplate(formData);
        onTemplateSubmitted(createdTemplate.name || formData.name);
        onNavigate('submission-sent');
      }
    } catch (err) {
      setError(extractMetaErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleContentChange = (content: string) => {
    // Update preview by replacing variables with example values
    let previewText = content;
    const variableMatches = content.match(/\{\{(\d+)\}\}/g) || [];
    const uniqueVars = [...new Set(variableMatches)];

    // Build variables array from detected placeholders
    const variables = uniqueVars.map((match, index) => ({
      example: `[Variable ${index + 1}]`,
    }));

    setFormData(prev => ({ ...prev, content, variables }));

    // Generate preview
    uniqueVars.forEach((match, index) => {
      previewText = previewText.replace(
        new RegExp(match.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'),
        `[Value ${index + 1}]`
      );
    });
    setPreview(previewText);
  };

  const insertVariable = () => {
    const currentVars = formData.content.match(/\{\{(\d+)\}\}/g) || [];
    const nextVarNum = currentVars.length + 1;
    const newContent = formData.content + `{{${nextVarNum}}}`;
    handleContentChange(newContent);
  };

  if (fetchingTemplate) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="flex flex-col lg:flex-row gap-12">
      {/* Left Panel: Configuration */}
      <div className="flex-1 max-w-2xl space-y-10">
        <header>
          <Button
            variant="ghost"
            onClick={() => onNavigate('templates')}
            className="flex items-center gap-2 text-primary font-bold text-sm mb-4 hover:gap-3 transition-all p-0 h-auto"
          >
            <ArrowLeft size={16} />
            Back to Templates
          </Button>
          <h1 className="text-4xl font-extrabold mb-2">
            {isEditMode ? 'Edit Template' : 'Create New Template'}
          </h1>
          <p className="text-muted-foreground text-lg">
            {isEditMode
              ? 'Update your message template details before resubmitting.'
              : 'Design and configure your message template for Meta API delivery.'}
          </p>
        </header>

        {error && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Identity Section */}
          <Card className="bg-muted/30 border-none rounded-[2rem] p-4">
            <CardContent className="pt-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="space-y-3">
                  <Label className="technical-label">Template Name</Label>
                  <Input
                    type="text"
                    value={formData.name}
                    onChange={(e) => {
                      const raw = e.target.value;
                      const formatted = raw.toLowerCase().replace(/[^a-z0-9_]/g, '_').replace(/_+/g, '_');
                      setFormData({ ...formData, name: formatted });
                    }}
                    placeholder="e.g. welcome_series_q4"
                    className="bg-background border-none h-12 px-4 rounded-xl font-bold focus-visible:ring-primary"
                    required
                  />
                  <p className="text-[11px] text-muted-foreground font-medium">
                    Lowercase letters, numbers, and underscores only. Spaces will be converted to underscores.
                  </p>
                </div>
                <div className="space-y-3">
                  <Label className="technical-label">Category</Label>
                  <Select
                    value={formData.category}
                    onValueChange={(value) => setFormData({ ...formData, category: value as TemplateCategory })}
                    disabled={isEditMode}
                  >
                    <SelectTrigger className="bg-background border-none h-12 px-4 rounded-xl font-bold focus:ring-primary">
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Marketing">Marketing</SelectItem>
                      <SelectItem value="Utility">Utility</SelectItem>
                      <SelectItem value="Authentication">Authentication</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Content Section */}
          <Card className="bg-muted/30 border-none rounded-[2rem] p-4">
            <CardHeader className="flex flex-row items-center justify-between pb-4">
              <Label className="technical-label">Message Content</Label>
              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={insertVariable}
                  className="h-7 px-3 text-[10px] font-bold rounded-lg"
                >
                  Add Variable
                </Button>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              <Textarea
                rows={8}
                value={formData.content}
                onChange={(e) => handleContentChange(e.target.value)}
                placeholder="Enter your message content here..."
                className="bg-background border-none rounded-2xl p-6 text-sm leading-relaxed focus-visible:ring-primary resize-none font-medium min-h-[200px]"
                required
              />
              <div className="flex items-start gap-3 p-4 bg-primary/5 rounded-2xl border border-primary/10">
                <Info className="text-primary mt-0.5" size={18} />
                <p className="text-xs text-muted-foreground leading-relaxed">
                  Use <code className="bg-primary/10 px-1.5 py-0.5 rounded text-primary font-bold">{'{{1}}'}</code>,
                  <code className="bg-primary/10 px-1.5 py-0.5 rounded text-primary font-bold">{'{{2}}'}</code>
                  for dynamic variables. These will be replaced when sending messages.
                </p>
              </div>
            </CardContent>
          </Card>

          <div className="flex items-center justify-end gap-4 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => onNavigate('templates')}
              className="h-14 px-8 font-bold rounded-xl active:scale-95"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={loading}
              className="h-14 px-10 font-bold rounded-xl shadow-xl shadow-primary/20 flex items-center gap-2 active:scale-95"
            >
              {loading ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  {isEditMode ? 'Saving...' : 'Submitting...'}
                </>
              ) : (
                <>
                  {isEditMode ? 'Save Changes' : 'Submit for Meta Approval'}
                  <Send size={18} />
                </>
              )}
            </Button>
          </div>
        </form>
      </div>

      {/* Right Panel: Preview */}
      <div className="lg:w-[360px] flex flex-col items-center">
        <div className="sticky top-24 w-full space-y-8">
          <div className="text-center">
            <span className="technical-label">Live Device Preview</span>
          </div>

          {/* Phone Mockup */}
          <div className="relative mx-auto w-full max-w-[320px] aspect-[9/18.5] bg-slate-900 rounded-[3rem] border-[8px] border-slate-900 shadow-2xl overflow-hidden">
            {/* Notch */}
            <div className="absolute top-0 inset-x-0 h-6 bg-slate-900 flex justify-center items-end pb-1 z-20">
              <div className="w-20 h-3 bg-black rounded-full" />
            </div>

            {/* Content */}
            <div className="h-full bg-[#e5ddd5] flex flex-col pt-6">
              {/* WhatsApp Header */}
              <div className="bg-[#075e54] text-white p-3 pt-4 flex items-center gap-3">
                <ArrowLeft size={20} />
                <div className="w-8 h-8 rounded-full bg-slate-300 overflow-hidden border border-white/20">
                  <img
                    src="https://images.unsplash.com/photo-1614850523296-d8c1af93d400?auto=format&fit=crop&w=100&h=100&q=80"
                    alt="Business Logo"
                    className="w-full h-full object-cover"
                    referrerPolicy="no-referrer"
                  />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-bold truncate">Sapphire Logic</p>
                  <p className="text-[9px] opacity-80">Online</p>
                </div>
                <div className="flex gap-3">
                  <Video size={18} />
                  <Phone size={18} />
                </div>
              </div>

              {/* Chat Area */}
              <div className="flex-1 p-4 space-y-4 overflow-y-auto">
                <div className="flex justify-center">
                  <span className="bg-[#d1eaef] text-[10px] px-3 py-1 rounded-lg text-slate-600 font-bold uppercase tracking-wider">Today</span>
                </div>

                <div className="bg-white p-3 rounded-2xl rounded-tl-none shadow-sm max-w-[90%] relative">
                  <p className="text-[11px] text-slate-800 leading-relaxed whitespace-pre-wrap">
                    {preview || 'Your message preview will appear here...'}
                  </p>
                  <div className="flex justify-end items-center gap-1 mt-1 opacity-40">
                    <span className="text-[8px]">{new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                    <CheckCircle2 size={10} />
                  </div>
                </div>
              </div>

              {/* Chat Input */}
              <div className="p-2 flex items-center gap-2">
                <div className="flex-1 bg-white rounded-full flex items-center px-4 py-2 shadow-sm">
                  <Smile size={18} className="text-slate-400" />
                  <span className="flex-1 ml-2 text-xs text-slate-400">Message</span>
                  <Paperclip size={18} className="text-slate-400 rotate-45" />
                  <Camera size={18} className="text-slate-400 ml-2" />
                </div>
                <div className="w-10 h-10 bg-[#075e54] text-white rounded-full flex items-center justify-center shadow-md">
                  <Mic size={20} />
                </div>
              </div>
            </div>
          </div>

          {/* Compliance Check */}
          <Card className="bg-muted/30 border-none rounded-3xl p-6 space-y-4">
            <CardHeader className="p-0 pb-2">
              <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Meta Compliance Check</CardTitle>
            </CardHeader>
            <CardContent className="p-0 space-y-3">
              <div className="flex items-center gap-3 text-[11px] font-bold text-foreground">
                <CheckCircle2 size={14} className="text-emerald-500" />
                Opt-out mechanism present
              </div>
              <div className="flex items-center gap-3 text-[11px] font-bold text-foreground">
                <CheckCircle2 size={14} className="text-emerald-500" />
                No prohibited content detected
              </div>
              <div className="flex items-center gap-3 text-[11px] font-bold text-foreground">
                {formData.content.length > 0 ? (
                  <CheckCircle2 size={14} className="text-emerald-500" />
                ) : (
                  <AlertTriangle size={14} className="text-amber-500" />
                )}
                Content provided
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
