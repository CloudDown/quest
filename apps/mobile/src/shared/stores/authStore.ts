import { create } from 'zustand';

import type { City, User } from '@quest/shared';

interface AuthState {
  user: User | null;
  city: City | null;
  accessToken: string | null;
  isOnboarded: boolean;
  setSession: (user: User, token: string) => void;
  setCity: (city: City) => void;
  completeOnboarding: () => void;
  signOut: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  city: null,
  accessToken: null,
  isOnboarded: false,
  setSession: (user, accessToken) => set({ user, accessToken }),
  setCity: (city) => set({ city }),
  completeOnboarding: () => set({ isOnboarded: true }),
  signOut: () =>
    set({ user: null, city: null, accessToken: null, isOnboarded: false }),
}));
