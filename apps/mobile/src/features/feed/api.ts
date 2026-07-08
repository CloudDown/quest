import type { CheckIn } from '@quest/shared';

import { apiClient } from '@/shared/api';

export async function fetchTodayFeed(cityId: string, token: string | null) {
  if (!process.env.EXPO_PUBLIC_API_URL) {
    return [] as CheckIn[];
  }

  return apiClient<CheckIn[]>(`/feed/today?cityId=${cityId}`, { token });
}
