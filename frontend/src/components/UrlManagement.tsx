import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';

interface UrlMapping {
  shortCode: string;
  longUrl: string;
  redirectCount: number;
  createdAt: string;
  expiryAt?: string;
}

const UrlManagement: React.FC = () => {
  const [urls, setUrls] = useState<UrlMapping[]>([]);
  
  const queryClient = useQueryClient();

  const { data, isLoading, error } = useQuery({
    queryKey: ['userUrls'],
    queryFn: async () => {
      const response = await fetch('/api/v1/urls');
      if (!response.ok) {
        throw new Error('Failed to fetch URLs');
      }
      return response.json();
    },
  });

  React.useEffect(() => {
    if (data) {
      setUrls(data);
    }
  }, [data]);

  const deleteMutation = useMutation({
    mutationFn: async (shortCode: string) => {
      const response = await fetch(`/api/v1/urls/${shortCode}`, {
        method: 'DELETE',
      });
      
      if (!response.ok) {
        throw new Error('Failed to delete URL');
      }
      
      return response.json();
    },
    onSuccess: (_, variables: string) => {
      setUrls(urls.filter(url => url.shortCode !== variables));
      queryClient.invalidateQueries({ queryKey: ['userUrls'] });
      toast.success('URL deleted successfully!');
    },
    onError: () => {
      toast.error('Failed to delete URL. Please try again.');
    },
  });

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      toast.success('URL copied to clipboard!');
    } catch (err) {
      toast.error('Failed to copy URL');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading URLs...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg text-red-600">Error loading URLs</div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Manage Your URLs</h2>
        
        {urls.length === 0 ? (
          <div className="bg-white p-8 rounded-lg shadow-md text-center">
            <div className="text-gray-600">
              You haven't created any URLs yet. 
              <a href="/" className="text-blue-600 hover:text-blue-800">
                Create your first URL
              </a>
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Short Code
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Long URL
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Clicks
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Created
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Expires
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {urls.map((url, index) => (
                    <tr key={url.shortCode} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        <div className="font-mono">{url.shortCode}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        <div className="max-w-xs truncate" title={url.longUrl}>
                          {url.longUrl}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {url.redirectCount.toLocaleString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(url.createdAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {url.expiryAt ? formatDate(url.expiryAt) : 'Never'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => copyToClipboard(`${window.location.origin}/${url.shortCode}`)}
                            className="text-blue-600 hover:text-blue-800 text-sm"
                          >
                            Copy
                          </button>
                          <button
                            onClick={() => {
                              if (confirm('Are you sure you want to delete this URL?')) {
                                deleteMutation.mutate(url.shortCode);
                              }
                            }}
                            className="text-red-600 hover:text-red-800 text-sm"
                            disabled={deleteMutation.isPending}
                          >
                            {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default UrlManagement;
