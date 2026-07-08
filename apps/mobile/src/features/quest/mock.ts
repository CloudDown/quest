import type { DailyQuest } from '@quest/shared';
import { Rarity } from '@quest/shared';

/** Données mock — remplacées par l'API en phase 1 */
export const MOCK_TODAY_QUEST: DailyQuest = {
  id: 'quest-montreal-today',
  cityId: 'montreal',
  poiId: 'poi-mount-royal',
  date: new Date().toISOString().split('T')[0],
  revealedAt: new Date().toISOString(),
  notificationSentAt: new Date().toISOString(),
  poi: {
    id: 'poi-mount-royal',
    cityId: 'montreal',
    name: 'Belvédère Kondiaronk',
    description:
      'Point de vue emblématique sur Montréal depuis le Mont-Royal. Nommé en hommage au chef amérindien Kondiaronk.',
    lat: 45.5048,
    lng: -73.5878,
    rarity: Rarity.RARE,
    wikipediaUrl: 'https://fr.wikipedia.org/wiki/Mont_Royal',
  },
};
