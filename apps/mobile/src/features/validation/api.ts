import type { CheckIn } from '@quest/shared';

import { apiClient } from '@/shared/api';

export async function fetchPendingCheckIns(token: string | null) {
  if (!process.env.EXPO_PUBLIC_API_URL) {
    return [] as CheckIn[];
  }

  return apiClient<CheckIn[]>('/check-ins/pending', { token });
}

export async function validateCheckIn(checkInId: string, token: string) {
  return apiClient<void>('/validations', {
    method: 'POST',
    token,
    body: { checkInId },
  });
}
