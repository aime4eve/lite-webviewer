import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// Create the app store with Zustand
export const useAppStore = create(
  persist(
    (set) => ({
      // File tree state
      files: [],
      setFiles: (files) => set({ files }),
      
      // Selected file state
      selectedFile: null,
      setSelectedFile: (filePath) => set({ selectedFile: filePath }),
      
      // Loading state
      loading: false,
      setLoading: (loading) => set({ loading }),
      
      // Error state
      error: null,
      setError: (error) => set({ error }),
      
      // Clear error
      clearError: () => set({ error: null }),
    }),
    {
      name: 'nexus-lite-storage', // LocalStorage key
      partialize: (state) => ({
        files: state.files, // Only persist files to localStorage
      }),
    }
  )
);
