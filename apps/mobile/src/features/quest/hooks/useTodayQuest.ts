import { useQuery } from '@tanstack/react-query';

import { fetchTodayQuest, getTodayQuestQueryKey, useQuestQueryParams } from './api';

export function useTodayQuest() {
  const { cityId, token } = useQuestQueryParams();

  return useQuery({
    queryKey: getTodayQuestQueryKey(cityId),
    queryFn: () => fetchTodayQuest(cityId!, token),
    enabled: Boolean(cityId),
  });
}
