import type { LeaderboardEntry } from '@quest/shared';

import { apiClient } from '@/shared/api';

export async function fetchLeaderboard(cityId: string, token: string | null) {
  if (!process.env.EXPO_PUBLIC_API_URL) {
    return [] as LeaderboardEntry[];
  }

  return apiClient<LeaderboardEntry[]>(`/leaderboard?cityId=${cityId}&period=week`, {
    token,
  });
}
