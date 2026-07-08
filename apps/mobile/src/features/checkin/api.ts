import type { CheckIn } from '@quest/shared';
import { CheckInStatus } from '@quest/shared';

import { apiClient } from '@/shared/api';

export interface SubmitCheckInInput {
  dailyQuestId: string;
  photoUri: string;
  lat: number;
  lng: number;
  token: string;
}

export async function submitCheckIn(input: SubmitCheckInInput): Promise<CheckIn> {
  if (!process.env.EXPO_PUBLIC_API_URL) {
    return {
      id: 'mock-checkin',
      userId: 'mock-user',
      dailyQuestId: input.dailyQuestId,
      photoUrl: input.photoUri,
      lat: input.lat,
      lng: input.lng,
      status: CheckInStatus.PENDING,
      createdAt: new Date().toISOString(),
    };
  }

  const formData = new FormData();
  formData.append('dailyQuestId', input.dailyQuestId);
  formData.append('lat', String(input.lat));
  formData.append('lng', String(input.lng));
  formData.append('photo', {
    uri: input.photoUri,
    name: 'checkin.jpg',
    type: 'image/jpeg',
  } as unknown as Blob);

  const response = await fetch(`${process.env.EXPO_PUBLIC_API_URL}/check-ins`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${input.token}` },
    body: formData,
  });

  if (!response.ok) {
    throw new Error('Échec de la soumission du check-in');
  }

  return response.json() as Promise<CheckIn>;
}
