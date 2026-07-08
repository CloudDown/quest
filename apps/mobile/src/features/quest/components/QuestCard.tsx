import { StyleSheet, View } from 'react-native';

import type { POI } from '@quest/shared';

import { QuestMap } from '@/features/quest/components/QuestMap';
import { RarityBadge } from '@/shared/components/RarityBadge';
import { Body, Caption, Card, Title } from '@/shared/components/ui';
import { colors, spacing } from '@/shared/theme';

interface QuestCardProps {
  poi: POI;
}

export function QuestCard({ poi }: QuestCardProps) {
  return (
    <Card style={styles.card}>
      <View style={styles.header}>
        <Title style={styles.title}>{poi.name}</Title>
        <RarityBadge rarity={poi.rarity} />
      </View>
      <Caption style={styles.subtitle}>Quest du jour</Caption>
      <Body style={styles.description}>{poi.description}</Body>
      <View style={styles.mapContainer}>
        <QuestMap poi={poi} />
      </View>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: spacing.sm,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: spacing.sm,
  },
  title: {
    flex: 1,
    fontSize: 22,
  },
  subtitle: {
    color: colors.accent,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  description: {
    color: colors.textMuted,
    lineHeight: 22,
  },
  mapContainer: {
    marginTop: spacing.sm,
    borderRadius: 12,
    overflow: 'hidden',
    height: 180,
  },
});
