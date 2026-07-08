import type { DailyQuest } from '@quest/shared';

import { apiClient } from '@/shared/api';
import { useAuthStore } from '@/shared/stores/authStore';

import { MOCK_TODAY_QUEST } from './mock';

export async function fetchTodayQuest(cityId: string, token?: string | null) {
  if (!process.env.EXPO_PUBLIC_API_URL) {
    return MOCK_TODAY_QUEST;
  }

  return apiClient<DailyQuest>(`/quests/today?cityId=${cityId}`, { token });
}

export function getTodayQuestQueryKey(cityId: string | undefined) {
  return ['quest', 'today', cityId] as const;
}

export function useQuestQueryParams() {
  const cityId = useAuthStore((s) => s.city?.id);
  const token = useAuthStore((s) => s.accessToken);
  return { cityId, token };
}
