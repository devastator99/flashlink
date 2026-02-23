import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';

interface UrlShortenerProps {
  onUrlCreated?: (shortUrl: string) => void;
}

const UrlShortener: React.FC<UrlShortenerProps> = ({ onUrlCreated }) => {
  const [longUrl, setLongUrl] = useState('');
  const [expiryDays, setExpiryDays] = useState(30);

  const shortenMutation = useMutation({
    mutationFn: async (data: { url: string; expiryDays: number }) => {
      const response = await fetch('/api/v1/shorten', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          url: data.url,
          expiryAt: data.expiryDays > 0 ? 
            new Date(Date.now() + data.expiryDays * 24 * 60 * 60 * 1000).toISOString() : 
            undefined
        }),
      });
      
      if (!response.ok) {
        throw new Error('Failed to shorten URL');
      }
      
      return response.json();
    },
    onSuccess: (data:any) => {
      setLongUrl('');
      setExpiryDays(30);
      onUrlCreated?.(data.shortUrl);
      toast.success('URL shortened successfully!');
    },
    onError: () => {
      toast.error('Failed to shorten URL. Please try again.');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!longUrl.trim()) {
      toast.error('Please enter a valid URL');
      return;
    }
    
    shortenMutation.mutate({ url: longUrl, expiryDays });
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-md p-8">
        <h2 className="text-2xl font-bold text-center mb-8 text-gray-800">
          URL Shortener
        </h2>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="url" className="block text-sm font-medium text-gray-700 mb-2">
              Enter your long URL
            </label>
            <input
              id="url"
              type="url"
              value={longUrl}
              onChange={(e) => setLongUrl(e.target.value)}
              placeholder="https://example.com/very/long/url"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>

          <div>
            <label htmlFor="expiry" className="block text-sm font-medium text-gray-700 mb-2">
              Expiry Time
            </label>
            <select
              id="expiry"
              value={expiryDays}
              onChange={(e) => setExpiryDays(Number(e.target.value))}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value={7}>7 days</option>
              <option value={30}>30 days</option>
              <option value={90}>90 days</option>
              <option value={0}>Never expires</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={shortenMutation.isPending}
            className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg hover:bg-blue-700 disabled:bg-blue-400 disabled:cursor-not-allowed transition-colors"
          >
            {shortenMutation.isPending ? 'Creating...' : 'Shorten URL'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default UrlShortener;
